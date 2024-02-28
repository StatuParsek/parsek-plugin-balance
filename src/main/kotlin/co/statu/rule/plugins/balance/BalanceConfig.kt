package co.statu.rule.plugins.balance

import co.statu.parsek.api.config.PluginConfig

data class BalanceConfig(
    val title: String = "Token Buy",
    val description: String = "Buying {{amount}} of token.",
    val registerGiftAmount: Double = 10.0
) : PluginConfig()