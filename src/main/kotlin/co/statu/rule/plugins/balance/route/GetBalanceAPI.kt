package co.statu.rule.plugins.balance.route

import co.statu.parsek.annotation.Endpoint
import co.statu.parsek.model.Path
import co.statu.parsek.model.Result
import co.statu.parsek.model.RouteType
import co.statu.parsek.model.Successful
import co.statu.rule.auth.api.LoggedInApi
import co.statu.rule.auth.provider.AuthProvider
import co.statu.rule.database.DatabaseManager
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.db.dao.BalanceDao
import co.statu.rule.plugins.balance.db.impl.BalanceDaoImpl
import io.vertx.ext.web.RoutingContext
import io.vertx.json.schema.SchemaParser

@Endpoint
class GetBalanceAPI(
    private val balancePlugin: BalancePlugin
) : LoggedInApi() {
    private val databaseManager by lazy {
        balancePlugin.pluginBeanContext.getBean(DatabaseManager::class.java)
    }

    private val authProvider by lazy {
        balancePlugin.pluginBeanContext.getBean(AuthProvider::class.java)
    }

    private val balanceDao: BalanceDao = BalanceDaoImpl()

    override val paths = listOf(Path("/balance", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser) = null

    override suspend fun handle(context: RoutingContext): Result {
        val userId = authProvider.getUserIdFromRoutingContext(context)

        val jdbcPool = databaseManager.getConnectionPool()

        val balance = balanceDao.byUserId(userId, jdbcPool)

        return Successful(balance)
    }
}