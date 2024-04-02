package co.statu.rule.plugins.balance

import co.statu.parsek.api.ParsekPlugin
import co.statu.rule.database.Dao
import co.statu.rule.database.DatabaseMigration
import co.statu.rule.database.api.DatabaseHelper
import co.statu.rule.plugins.balance.db.impl.BalanceDaoImpl
import co.statu.rule.plugins.balance.db.impl.BalanceRegisterGiftDaoImpl
import co.statu.rule.plugins.balance.db.impl.BalanceUsageDaoImpl
import co.statu.rule.plugins.balance.db.migration.DbMigration1To2
import co.statu.rule.plugins.balance.db.migration.DbMigration2To3
import co.statu.rule.plugins.balance.db.migration.DbMigration3To4

class BalancePlugin : ParsekPlugin(), DatabaseHelper {
    override val tables: List<Dao<*>> by lazy {
        listOf<Dao<*>>(
            BalanceDaoImpl(),
            BalanceUsageDaoImpl(),
            BalanceRegisterGiftDaoImpl()
        )
    }

    override val migrations: List<DatabaseMigration> by lazy {
        listOf(
            DbMigration1To2(),
            DbMigration2To3(),
            DbMigration3To4()
        )
    }
}