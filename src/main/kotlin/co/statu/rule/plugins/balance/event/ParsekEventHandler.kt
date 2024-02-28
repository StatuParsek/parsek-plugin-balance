package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.config.PluginConfigManager
import co.statu.parsek.api.event.ParsekEventListener
import co.statu.parsek.config.ConfigManager
import co.statu.rule.plugins.balance.BalanceConfig
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.BalancePlugin.Companion.logger
import co.statu.rule.plugins.balance.config.migration.ConfigMigration1to2

class ParsekEventHandler : ParsekEventListener {
    override suspend fun onConfigManagerReady(configManager: ConfigManager) {
        BalancePlugin.pluginConfigManager = PluginConfigManager(
            configManager,
            BalancePlugin.INSTANCE,
            BalanceConfig::class.java,
            logger,
            listOf(ConfigMigration1to2()),
            listOf("balance")
        )

        logger.info("Initialized plugin config")
    }
}