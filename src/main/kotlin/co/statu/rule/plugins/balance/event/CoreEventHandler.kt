package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.parsek.api.event.CoreEventListener
import co.statu.parsek.config.ConfigManager
import co.statu.rule.plugins.balance.BalanceConfig
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.config.migration.ConfigMigration1to2
import org.slf4j.Logger

@EventListener
class CoreEventHandler(private val balancePlugin: BalancePlugin, private val logger: Logger) : CoreEventListener {
    override suspend fun onConfigManagerReady(configManager: ConfigManager) {
        val pluginConfigManager = PluginConfigManager(
            configManager,
            balancePlugin,
            BalanceConfig::class.java,
            listOf(ConfigMigration1to2()),
            listOf("balance")
        )

        balancePlugin.pluginBeanContext.beanFactory.registerSingleton(
            pluginConfigManager.javaClass.name,
            pluginConfigManager
        )

        logger.info("Initialized plugin config")
    }
}