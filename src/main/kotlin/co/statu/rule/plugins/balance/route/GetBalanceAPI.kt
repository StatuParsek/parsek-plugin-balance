package co.statu.rule.plugins.balance.route

import co.statu.parsek.model.Path
import co.statu.parsek.model.Result
import co.statu.parsek.model.RouteType
import co.statu.parsek.model.Successful
import co.statu.rule.auth.api.LoggedInApi
import co.statu.rule.auth.provider.AuthProvider
import co.statu.rule.database.Dao.Companion.get
import co.statu.rule.database.DatabaseManager
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.db.dao.BalanceDao
import io.vertx.ext.web.RoutingContext
import io.vertx.json.schema.SchemaParser

class GetBalanceAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : LoggedInApi() {
    override val paths = listOf(Path("/balance", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser) = null

    private val balanceDao by lazy {
        get<BalanceDao>(BalancePlugin.tables)
    }

    override suspend fun handle(context: RoutingContext): Result {
        val userId = authProvider.getUserIdFromRoutingContext(context)

        val jdbcPool = databaseManager.getConnectionPool()

        val balance = balanceDao.byUserId(userId, jdbcPool)

        return Successful(balance)
    }
}