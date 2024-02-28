package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.event.RouterEventListener
import co.statu.parsek.model.Route
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.route.GetBalanceAPI
import co.statu.rule.plugins.balance.route.GetBalanceUsageAPI

class RouterEventHandler : RouterEventListener {
    override fun onInitRouteList(routes: MutableList<Route>) {
        val databaseManager = BalancePlugin.databaseManager
        val authProvider = BalancePlugin.authProvider

        routes.addAll(
            listOf(
                GetBalanceAPI(databaseManager, authProvider),
                GetBalanceUsageAPI(databaseManager, authProvider)
            )
        )
    }
}