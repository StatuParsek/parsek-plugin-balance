package co.statu.rule.plugins.balance.db.impl

import co.statu.parsek.api.ParsekPlugin
import co.statu.rule.plugins.balance.db.dao.BalanceUsageDao
import co.statu.rule.plugins.balance.db.model.BalanceUsage
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import java.util.*

class BalanceUsageDaoImpl : BalanceUsageDao() {

    override suspend fun init(jdbcPool: JDBCPool, plugin: ParsekPlugin) {
        jdbcPool
            .query(
                """
                        CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                            `id` UUID NOT NULL,
                            `userId` UUID NOT NULL,
                            `amount` Double DEFAULT 0.0,
                            `currentAmount` Double DEFAULT 0.0,
                            `consumedBy` String NOT NULL,
                            `date` Int64 NOT NULL
                        ) ENGINE = MergeTree() order by `date`;
                        """
            )
            .execute()
            .await()
    }

    override suspend fun add(balanceUsage: BalanceUsage, jdbcPool: JDBCPool): UUID {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (${fields.toTableQuery()}) " +
                    "VALUES (?, ?, ?, ?, ?, ?)"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    balanceUsage.id,
                    balanceUsage.userId,
                    balanceUsage.amount,
                    balanceUsage.currentAmount,
                    balanceUsage.consumedBy,
                    balanceUsage.date
                )
            )
            .await()

        return balanceUsage.id
    }

    override suspend fun countById(
        userId: UUID,
        jdbcPool: JDBCPool
    ): Long {
        val query =
            "SELECT COUNT(*) FROM `${getTablePrefix() + tableName}` WHERE userId = ?"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(Tuple.of(userId))
            .await()

        return rows.toList()[0].getLong(0)
    }

    override suspend fun byUserId(
        userId: UUID,
        page: Long,
        jdbcPool: JDBCPool
    ): List<BalanceUsage> {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `userId` = ? LIMIT 10 ${if (page == 1L) "" else "OFFSET ${(page - 1) * 10}"}"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(Tuple.of(userId))
            .await()

        return rows.toEntities()
    }
}