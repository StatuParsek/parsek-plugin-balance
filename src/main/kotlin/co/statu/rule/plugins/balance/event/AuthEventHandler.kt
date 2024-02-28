package co.statu.rule.plugins.balance.event

import co.statu.rule.auth.db.model.User
import co.statu.rule.auth.event.AuthEventListener
import co.statu.rule.auth.provider.AuthProvider
import co.statu.rule.auth.util.StringUtil
import co.statu.rule.database.Dao.Companion.get
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.db.dao.BalanceDao
import co.statu.rule.plugins.balance.db.dao.BalanceRegisterGiftDao
import co.statu.rule.plugins.balance.db.model.Balance
import co.statu.rule.plugins.balance.db.model.BalanceRegisterGift
import co.statu.rule.plugins.payment.db.dao.PurchaseDao
import co.statu.rule.plugins.payment.db.model.Purchase
import co.statu.rule.plugins.payment.db.model.PurchaseStatus
import io.vertx.core.json.JsonObject

class AuthEventHandler : AuthEventListener {

    private val balanceDao by lazy {
        get<BalanceDao>(BalancePlugin.tables)
    }

    private val pluginConfigManager by lazy {
        BalancePlugin.pluginConfigManager
    }

    private val config by lazy {
        pluginConfigManager.config
    }

    private val purchaseDao by lazy {
        get<PurchaseDao>(BalancePlugin.externalTables)
    }

    private val balanceRegisterGiftDao by lazy {
        get<BalanceRegisterGiftDao>(BalancePlugin.tables)
    }

    override suspend fun onReady(authProvider: AuthProvider) {
        BalancePlugin.authProvider = authProvider
    }

    override suspend fun onRegistrationComplete(user: User) {
        val jdbcPool = BalancePlugin.databaseManager.getConnectionPool()
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