package co.statu.rule.plugins.balance.db.impl

import co.statu.parsek.api.ParsekPlugin
import co.statu.rule.auth.util.StringUtil
import co.statu.rule.plugins.balance.db.dao.BalanceRegisterGiftDao
import co.statu.rule.plugins.balance.db.model.BalanceRegisterGift
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import java.util.*

class BalanceRegisterGiftDaoImpl : BalanceRegisterGiftDao() {

    override suspend fun init(jdbcPool: JDBCPool, plugin: ParsekPlugin) {
        jdbcPool
            .query(
                """
                        CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                            `id` UUID NOT NULL,
                            `email` String NOT NULL,
                            `createdAt` Int64 NOT NULL
                        ) ENGINE = MergeTree() order by `createdAt`;
                        """
            )
            .execute()
            .await()

        val rows = jdbcPool.query("SELECT `email` FROM `${getTablePrefix()}user`").execute().await()

        rows.forEach { row ->
            val email = StringUtil.extractOriginalEmail(row.getString(0).lowercase())

            if (!existsByEmail(email, jdbcPool)) {
                add(BalanceRegisterGift(email = email), jdbcPool)
            }
        }
    }

    override suspend fun add(balanceRegisterGift: BalanceRegisterGift, jdbcPool: JDBCPool): UUID {
        val query = "INSERT INTO `${getTablePrefix() + tableName}` (${fields.toTableQuery()}) VALUES (?, ?, ?)"

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

        return balanceRegisterGift.id
    }

    override suspend fun existsByEmail(email: String, jdbcPool: JDBCPool): Boolean {
        val query = "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` where `email` = ?"

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