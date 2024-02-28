package co.statu.rule.plugins.balance.db.dao

import co.statu.rule.database.Dao
import co.statu.rule.plugins.balance.db.model.BalanceUsage
import io.vertx.jdbcclient.JDBCPool
import java.util.*

abstract class BalanceUsageDao : Dao<BalanceUsage>(BalanceUsage::class) {
    abstract suspend fun add(
        balanceUsage: BalanceUsage,
        jdbcPool: JDBCPool
    ): UUID

    abstract suspend fun byUserId(
        userId: UUID,
        page: Long,
        jdbcPool: JDBCPool
    ): List<BalanceUsage>

    abstract suspend fun countById(
        userId: UUID,
        jdbcPool: JDBCPool
    ): Long
}