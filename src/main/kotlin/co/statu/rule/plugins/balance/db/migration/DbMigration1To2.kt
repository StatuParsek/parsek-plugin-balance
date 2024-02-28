package co.statu.rule.plugins.balance.db.migration

import co.statu.rule.database.DatabaseMigration
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await

class DbMigration1To2(
    override val FROM_SCHEME_VERSION: Int = 1,
    override val SCHEME_VERSION: Int = 2,
    override val SCHEME_VERSION_INFO: String = "Add balance usage table"
) : DatabaseMigration() {
    override val handlers: List<suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit> = listOf(
        createBalanceUsageTable()
    )

    private fun createBalanceUsageTable(): suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit =
        { jdbcPool: JDBCPool, tablePrefix: String ->
            jdbcPool
                .query(
                    """
                        CREATE TABLE IF NOT EXISTS `${tablePrefix}balance_usage` (
                            `id` UUID NOT NULL,
                            `userId` UUID NOT NULL,
                            `amount` Double DEFAULT 0.0,
                            `currentAmount` Double DEFAULT 0.0,
                            `consumedBy` String NOT NULL,
                            `date` Int64 NOT NULL
                        ) ENGINE = MergeTree() order by `date`;
                """.trimIndent()
                )
                .execute()
                .await()
        }
}