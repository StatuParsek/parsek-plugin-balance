package co.statu.rule.plugins.balance

import co.statu.parsek.error.BadRequest
import co.statu.parsek.model.Successful
import co.statu.parsek.util.TextUtil.compileInline
import co.statu.rule.auth.db.model.User
import co.statu.rule.database.Dao.Companion.get
import co.statu.rule.plugins.balance.db.dao.BalanceDao
import co.statu.rule.plugins.balance.event.BalanceEventListener
import co.statu.rule.plugins.payment.api.Checkout
import co.statu.rule.plugins.payment.api.TypeListener
import co.statu.rule.plugins.payment.db.model.Purchase
import co.statu.rule.plugins.payment.util.TextUtil.toCurrencyFormat

class BalancePaymentListener : TypeListener {
    private val balanceEventHandlers by lazy {
        BalancePlugin.INSTANCE.context.pluginEventManager.getEventHandlers<BalanceEventListener>()
    }

    private val balanceConfig by lazy {
        BalancePlugin.pluginConfigManager.config
    }

    private val compiledDescription by lazy {
        balanceConfig.description.compileInline()
    }

    private val balanceDao by lazy {
        get<BalanceDao>(BalancePlugin.tables)
    }

    private val databaseManager by lazy {
        BalancePlugin.databaseManager
    }

    private val jdbcPool by lazy {
        databaseManager.getConnectionPool()
    }

    override suspend fun onHandleCheckout(user: User, value: String, checkout: Checkout): Successful {
        validateValue(value)

        val amount = value.toLong()
        var price = 0L

        balanceEventHandlers.forEach {
            it.verifyAmount(user, amount)
            price = it.setPriceOfAmount(user, amount, price)
        }

        if (price == 0L) {
            price = amount * 100
        }

        val purchaseId = checkout.savePurchase(amount, price)

        val title = balanceConfig.title
        val formattedDescription = compiledDescription.apply(
            mapOf(
                "amount" to amount.toFloat(),
                "price" to price.toCurrencyFormat()
            )
        )

        val paymentMethodIntegration = checkout.methodIntegration

        return paymentMethodIntegration.sendCheckoutRequest(
            user,
            purchaseId,
            amount,
            price,
            title,
            formattedDescription,
            checkout
        )
    }

    override suspend fun onOrderCreated(purchase: Purchase) {
        balanceDao.addAmountByUserId(purchase.userId, purchase.amount.toDouble(), jdbcPool)
    }

    override suspend fun onOrderRefunded(purchase: Purchase, externalOrderId: Long) {
        balanceDao.subtractAmountByUserId(purchase.userId, purchase.amount.toDouble(), jdbcPool)
    }

    private fun validateValue(value: String) {
        if (value.toLongOrNull() == null) {
            throw BadRequest()
        }
    }
}