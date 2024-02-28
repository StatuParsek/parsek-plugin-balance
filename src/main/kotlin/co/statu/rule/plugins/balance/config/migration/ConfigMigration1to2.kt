package co.statu.rule.plugins.balance.config.migration

import co.statu.parsek.api.config.PluginConfigMigration
import io.vertx.core.json.JsonObject

class ConfigMigration1to2(
    override val FROM_VERSION: Int = 1,
    override val VERSION: Int = 2,
    override val VERSION_INFO: String = "Add registerGiftAmount"
) : PluginConfigMigration() {
    override fun migrate(config: JsonObject) {
        config.put("registerGiftAmount", 10.0)
    }
}