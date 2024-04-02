package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.rule.database.DatabaseManager
import co.statu.rule.database.event.DatabaseEventListener
import co.statu.rule.plugins.balance.BalancePlugin

@EventListener
class DatabaseEventHandler(private val balancePlugin: BalancePlugin) : DatabaseEventListener {
    override suspend fun onReady(databaseManager: DatabaseManager) {
        databaseManager.migrateNewPluginId("balance", balancePlugin.pluginId, balancePlugin)

        databaseManager.initialize(balancePlugin, balancePlugin)
    }
}