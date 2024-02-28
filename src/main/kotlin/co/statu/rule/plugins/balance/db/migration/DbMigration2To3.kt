package co.statu.rule.plugins.balance.db.migration

import co.statu.rule.database.DatabaseMigration
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.await

class DbMigration2To3(
    override val FROM_SCHEME_VERSION: Int = 2,
    override val SCHEME_VERSION: Int = 3,
    override val SCHEME_VERSION_INFO: String = "Add referenceAmount and totalAmount columns to balance table"
) : DatabaseMigration() {
    override val handlers: List<suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit> = listOf(
        addReferenceAmountColumnToBalanceTable(),
        addTotalAmountColumnToBalanceTable()
    )

    private fun addReferenceAmountColumnToBalanceTable(): suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit =
        { jdbcPool: JDBCPool, tablePrefix: String ->
            jdbcPool
                .query("ALTER TABLE `${tablePrefix}balance` ADD COLUMN `referenceAmount` Double DEFAULT 0.0;")
                .execute()
                .await()
        }

    private fun addTotalAmountColumnToBalanceTable(): suspend (jdbcPool: JDBCPool, tablePrefix: String) -> Unit =
        { jdbcPool: JDBCPool, tablePrefix: String ->
            jdbcPool
                .query("ALTER TABLE `${tablePrefix}balance` ADD COLUMN `totalAmount` Double DEFAULT 0.0;")
                .execute()
                .await()
        }
}