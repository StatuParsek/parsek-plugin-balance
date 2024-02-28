package co.statu.rule.plugins.balance.db.migration

import co.statu.rule.auth.util.StringUtil
import co.statu.rule.database.DatabaseMigration
import co.statu.rule.plugins.balance.db.model.BalanceRegisterGift
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class DbMigration3To4(
    override val FROM_SCHEME_VERSION: Int = 3,
    override val SCHEME_VERSION: Int = 4,
    override val SCHEME_VERSION_INFO: String = "Add balance register gift table"
) : DatabaseMigration() {
    override val handlers: List<suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit> = listOf(
        createBalanceRegisterGiftTable()
    )

    private fun createBalanceRegisterGiftTable(): suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit =
        { jdbcPool: JDBCPool, tablePrefix: String ->
            jdbcPool
                .query(
                    """
                        CREATE TABLE IF NOT EXISTS `${tablePrefix}balance_register_gift` (
                            `id` UUID NOT NULL,
                            `email` String NOT NULL,
                            `createdAt` Int64 NOT NULL
                        ) ENGINE = MergeTree() order by `createdAt`;
                """.trimIndent()
                )
                .execute()
                .await()

            val rows = jdbcPool.query("SELECT `email` FROM `${tablePrefix}user`").execute().await()

            rows
                .map {
                    StringUtil.extractOriginalEmail(it.getString(0).lowercase())
                }
                .forEach { email ->
                    if (!existsByEmail(tablePrefix, email, jdbcPool)) {
                        val balanceRegisterGift = BalanceRegisterGift(email = email)

                        val query =
                            "INSERT INTO `${tablePrefix}balance_register_gift` (`id`, `email`, `createdAt`) VALUES (?, ?, ?)"

                        jdbcPool
                            .preparedQuery(query)
                            .execute(
                                Tuple.of(
                                    balanceRegisterGift.id,
                                    balanceRegisterGift.email,
                                    balanceRegisterGift.createdAt,
                                )
                            )
                            .await()
                    }
                }
        }

    private suspend fun existsByEmail(tablePrefix: String, email: String, jdbcPool: JDBCPool): Boolean {
        val query = "SELECT COUNT(`id`) FROM `${tablePrefix}balance_register_gift` where `email` = ?"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    email
                )
            )
            .await()

        return rows.toList()[0].getLong(0) == 1L
    }
}