package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.event.PluginEventListener
import co.statu.rule.auth.db.model.User

interface BalanceEventListener : PluginEventListener {
    suspend fun verifyAmount(user: User, amount: Long)

    suspend fun setPriceOfAmount(user: User, amount: Long, price: Long): Long
}