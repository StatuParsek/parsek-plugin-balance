package co.statu.rule.plugins.balance.event

import co.statu.rule.plugins.balance.BalancePaymentListener
import co.statu.rule.plugins.payment.PaymentSystem
import co.statu.rule.plugins.payment.event.PaymentEventListener

class PaymentEventHandler : PaymentEventListener {
    override fun onPaymentSystemInit(paymentSystem: PaymentSystem) {
        paymentSystem.register(BalancePaymentListener())
    }
}