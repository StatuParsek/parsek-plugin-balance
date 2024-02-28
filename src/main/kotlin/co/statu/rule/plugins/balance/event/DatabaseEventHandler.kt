package co.statu.rule.plugins.balance.event

import co.statu.rule.database.DatabaseManager
import co.statu.rule.database.event.DatabaseEventListener
import co.statu.rule.plugins.balance.BalancePlugin

class DatabaseEventHandler : DatabaseEventListener {
    override suspend fun onReady(databaseManager: DatabaseManager) {
        databaseManager.migrateNewPluginId("balance", BalancePlugin.INSTANCE.context.pluginId, BalancePlugin.INSTANCE)

        databaseManager.initialize(BalancePlugin.INSTANCE, BalancePlugin.tables, BalancePlugin.migrations)

        BalancePlugin.databaseManager = databaseManager
    }
}