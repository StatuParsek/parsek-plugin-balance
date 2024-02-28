package co.statu.rule.plugins.balance.db.model

import co.statu.rule.database.DBEntity
import java.util.*

data class BalanceRegisterGift(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
) : DBEntity()