package co.statu.rule.plugins.balance

import co.statu.parsek.api.ParsekPlugin
import co.statu.parsek.api.PluginContext
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.auth.provider.AuthProvider
import co.statu.rule.database.Dao
import co.statu.rule.database.DatabaseManager
import co.statu.rule.plugins.balance.db.impl.BalanceDaoImpl
import co.statu.rule.plugins.balance.db.impl.BalanceRegisterGiftDaoImpl
import co.statu.rule.plugins.balance.db.impl.BalanceUsageDaoImpl
import co.statu.rule.plugins.balance.db.migration.DbMigration1To2
import co.statu.rule.plugins.balance.db.migration.DbMigration2To3
import co.statu.rule.plugins.balance.db.migration.DbMigration3To4
import co.statu.rule.plugins.balance.event.*
import co.statu.rule.plugins.payment.db.impl.PurchaseDaoImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BalancePlugin(pluginContext: PluginContext) : ParsekPlugin(pluginContext) {
    companion object {
        internal val logger: Logger = LoggerFactory.getLogger(BalancePlugin::class.java)

        internal lateinit var pluginConfigManager: PluginConfigManager<BalanceConfig>

        internal lateinit var INSTANCE: BalancePlugin

        internal lateinit var databaseManager: DatabaseManager

        internal lateinit var authProvider: AuthProvider

        internal val tables by lazy {
            mutableListOf<Dao<*>>(
                BalanceDaoImpl(),
                BalanceUsageDaoImpl(),
                BalanceRegisterGiftDaoImpl()
            )
        }

        internal val externalTables by lazy {
            mutableListOf(
                PurchaseDaoImpl()
            )
        }

        internal val migrations by lazy {
            listOf(
                DbMigration1To2(),
                DbMigration2To3(),
                DbMigration3To4()
            )
        }
    }

    init {
        INSTANCE = this

        logger.info("Initialized instance")

        context.pluginEventManager.register(this, DatabaseEventHandler())
        context.pluginEventManager.register(this, AuthEventHandler())
        context.pluginEventManager.register(this, RouterEventHandler())
        context.pluginEventManager.register(this, PaymentEventHandler())
        context.pluginEventManager.register(this, ParsekEventHandler())

        logger.info("Registered events")
    }
}