package co.statu.rule.plugins.balance.db.dao

import co.statu.rule.database.Dao
import co.statu.rule.plugins.balance.db.model.BalanceRegisterGift
import io.vertx.jdbcclient.JDBCPool
import java.util.*

abstract class BalanceRegisterGiftDao : Dao<BalanceRegisterGift>(BalanceRegisterGift::class) {
    abstract suspend fun add(
        balanceRegisterGift: BalanceRegisterGift,
        jdbcPool: JDBCPool
    ): UUID

    abstract suspend fun existsByEmail(
        email: String,
        jdbcPool: JDBCPool
    ): Boolean
}