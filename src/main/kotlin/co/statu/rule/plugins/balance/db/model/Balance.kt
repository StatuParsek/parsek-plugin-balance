package co.statu.rule.plugins.balance.db.model

import co.statu.rule.database.DBEntity
import java.util.*

data class Balance(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    var amount: Double = 0.0,
    var referenceAmount: Double = 0.0,
    var totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : DBEntity()