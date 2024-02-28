package co.statu.rule.plugins.balance.db.impl

import co.statu.parsek.api.ParsekPlugin
import co.statu.rule.plugins.balance.db.dao.BalanceDao
import co.statu.rule.plugins.balance.db.model.Balance
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import java.util.*

class BalanceDaoImpl : BalanceDao() {

    override suspend fun init(jdbcPool: JDBCPool, plugin: ParsekPlugin) {
        jdbcPool
            .query(
                """
                        CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                            `id` UUID NOT NULL,
                            `userId` UUID NOT NULL,
                            `amount` Double DEFAULT 0.0,
                            `referenceAmount` Double DEFAULT 0.0,
                            `totalAmount` Double DEFAULT 0.0,
                            `createdAt` Int64 NOT NULL,
                            `updatedAt` Int64 NOT NULL
                        ) ENGINE = MergeTree() order by `createdAt`;
                        """
            )
            .execute()
            .await()

        val rows = jdbcPool.query(
            "SELECT `id` FROM `${getTablePrefix()}user`"
        ).execute().await()

        rows.forEach { row ->
            val userId = row.getUUID(0)

            add(Balance(userId = userId), jdbcPool)
        }
    }

    override suspend fun add(balance: Balance, jdbcPool: JDBCPool): UUID {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (${fields.toTableQuery()}) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    balance.id,
                    balance.userId,
                    balance.amount,
                    balance.referenceAmount,
                    balance.totalAmount,
                    balance.createdAt,
                    balance.updatedAt
                )
            )
            .await()

        return balance.id
    }

    override suspend fun byUserId(
        userId: UUID,
        jdbcPool: JDBCPool
    ): Balance? {
        val query =
            "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `userId` = ?"

        val rows: RowSet<Row> = jdbcPool
            .preparedQuery(query)
            .execute(Tuple.of(userId))
            .await()

        if (rows.size() == 0) {
            return null
        }

        val row = rows.toList()[0]

        return row.toEntity()
    }

    override suspend fun addAmountByUserId(userId: UUID, amount: Double, jdbcPool: JDBCPool) {
        val query =
            "ALTER TABLE `${getTablePrefix() + tableName}` UPDATE `referenceAmount` = `amount` + ?, `amount` = `amount` + ?, `totalAmount` = `totalAmount` + ?, `updatedAt` = ? WHERE `userId` = ?;"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    amount,
                    amount,
                    amount,
                    System.currentTimeMillis(),
                    userId
                )
            )
            .await()
    }

    override suspend fun subtractAmountByUserId(userId: UUID, amount: Double, jdbcPool: JDBCPool) {
        val query =
            "ALTER TABLE `${getTablePrefix() + tableName}` UPDATE `amount` = if(`amount` - ? < 0.0, 0.0, `amount` - ?) WHERE `userId` = ?;"

        jdbcPool
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    amount,
                    amount,
                    userId
                )
            )
            .await()
    }
}