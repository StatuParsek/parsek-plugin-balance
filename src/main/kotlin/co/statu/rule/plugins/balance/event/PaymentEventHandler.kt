package co.statu.rule.plugins.balance.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.rule.plugins.balance.BalancePaymentListener
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.payment.PaymentSystem
import co.statu.rule.plugins.payment.event.PaymentEventListener

@EventListener
class PaymentEventHandler(private val balancePlugin: BalancePlugin) : PaymentEventListener {
    private val balancePaymentListener by lazy {
        balancePlugin.pluginBeanContext.getBean(BalancePaymentListener::class.java)
    }

    override fun onPaymentSystemInit(paymentSystem: PaymentSystem) {
        paymentSystem.register(balancePaymentListener)
    }
}