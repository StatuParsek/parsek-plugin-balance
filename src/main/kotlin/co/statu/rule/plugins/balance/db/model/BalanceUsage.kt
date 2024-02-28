package co.statu.rule.plugins.balance.db.model

import co.statu.rule.database.DBEntity
import java.util.*

data class BalanceUsage(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val amount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val consumedBy: String,
    val date: Long = System.currentTimeMillis()
) : DBEntity()