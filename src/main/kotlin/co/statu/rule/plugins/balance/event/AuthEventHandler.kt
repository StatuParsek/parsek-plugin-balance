package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.auth.db.model.User
import co.statu.rule.auth.event.AuthEventListener
import co.statu.rule.auth.util.StringUtil
import co.statu.rule.database.DatabaseManager
import co.statu.rule.plugins.balance.BalanceConfig
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.db.dao.BalanceDao
import co.statu.rule.plugins.balance.db.dao.BalanceRegisterGiftDao
import co.statu.rule.plugins.balance.db.impl.BalanceDaoImpl
import co.statu.rule.plugins.balance.db.impl.BalanceRegisterGiftDaoImpl
import co.statu.rule.plugins.balance.db.model.Balance
import co.statu.rule.plugins.balance.db.model.BalanceRegisterGift
import co.statu.rule.plugins.payment.db.dao.PurchaseDao
import co.statu.rule.plugins.payment.db.impl.PurchaseDaoImpl
import co.statu.rule.plugins.payment.db.model.Purchase
import co.statu.rule.plugins.payment.db.model.PurchaseStatus
import io.vertx.core.json.JsonObject

@EventListener
class AuthEventHandler(private val balancePlugin: BalancePlugin) : AuthEventListener {

    private val pluginConfigManager by lazy {
        balancePlugin.pluginBeanContext.getBean(PluginConfigManager::class.java) as PluginConfigManager<BalanceConfig>
    }

    private val databaseManager by lazy {
        balancePlugin.pluginBeanContext.getBean(DatabaseManager::class.java)
    }

    private val config by lazy {
        pluginConfigManager.config
    }

    private val balanceDao: BalanceDao = BalanceDaoImpl()

    private val purchaseDao: PurchaseDao = PurchaseDaoImpl()

    private val balanceRegisterGiftDao: BalanceRegisterGiftDao = BalanceRegisterGiftDaoImpl()

    override suspend fun onRegistrationComplete(user: User) {
        val jdbcPool = databaseManager.getConnectionPool()
        val email = StringUtil.extractOriginalEmail(user.email.lowercase())

        val registerGiftExists = balanceRegisterGiftDao.existsByEmail(email, jdbcPool)

        if (registerGiftExists) {
            return
        }

        balanceRegisterGiftDao.add(BalanceRegisterGift(email = email), jdbcPool)
        balanceDao.add(
            Balance(
                userId = user.id,
                amount = config.registerGiftAmount,
                totalAmount = config.registerGiftAmount,
                referenceAmount = config.registerGiftAmount
            ), jdbcPool
        )

        val purchase = Purchase(
            userId = user.id,
            amount = config.registerGiftAmount.toLong(),
            type = "REGISTER_GIFT",
            price = 0,
            status = PurchaseStatus.SUCCESS,
            billDetail = JsonObject(),
            expiresAt = -1
        )

        purchaseDao.add(purchase, jdbcPool)
    }
}