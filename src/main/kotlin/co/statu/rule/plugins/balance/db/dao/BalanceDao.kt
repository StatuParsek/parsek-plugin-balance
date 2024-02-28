package co.statu.rule.plugins.balance.db.dao

import co.statu.rule.database.Dao
import co.statu.rule.plugins.balance.db.model.Balance
import io.vertx.jdbcclient.JDBCPool
import java.util.*

abstract class BalanceDao : Dao<Balance>(Balance::class) {
    abstract suspend fun add(balance: Balance, jdbcPool: JDBCPool): UUID

    abstract suspend fun byUserId(
        userId: UUID,
        jdbcPool: JDBCPool
    ): Balance?

    abstract suspend fun addAmountByUserId(
        userId: UUID,
        amount: Double,
        jdbcPool: JDBCPool
    )

    abstract suspend fun subtractAmountByUserId(
        userId: UUID,
        amount: Double,
        jdbcPool: JDBCPool
    )
}