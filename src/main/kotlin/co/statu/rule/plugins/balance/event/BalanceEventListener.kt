package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.PluginEvent
import co.statu.rule.auth.db.model.User

interface BalanceEventListener : PluginEvent {
    suspend fun verifyAmount(user: User, amount: Long)

    suspend fun setPriceOfAmount(user: User, amount: Long, price: Long): Long
}