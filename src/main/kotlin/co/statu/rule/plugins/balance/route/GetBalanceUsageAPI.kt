package co.statu.rule.plugins.balance.route

import co.statu.parsek.annotation.Endpoint
import co.statu.parsek.error.PageNotFound
import co.statu.parsek.model.Path
import co.statu.parsek.model.Result
import co.statu.parsek.model.RouteType
import co.statu.parsek.model.Successful
import co.statu.rule.auth.api.LoggedInApi
import co.statu.rule.auth.provider.AuthProvider
import co.statu.rule.database.DatabaseManager
import co.statu.rule.plugins.balance.BalancePlugin
import co.statu.rule.plugins.balance.db.dao.BalanceUsageDao
import co.statu.rule.plugins.balance.db.impl.BalanceUsageDaoImpl
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas

@Endpoint
class GetBalanceUsageAPI(
    private val balancePlugin: BalancePlugin
) : LoggedInApi() {
    private val databaseManager by lazy {
        balancePlugin.pluginBeanContext.getBean(DatabaseManager::class.java)
    }

    private val authProvider by lazy {
        balancePlugin.pluginBeanContext.getBean(AuthProvider::class.java)
    }

    override val paths = listOf(Path("/balance/usage", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .queryParameter(Parameters.optionalParam("page", Schemas.numberSchema()))
            .build()

    private val balanceUsageDao: BalanceUsageDao = BalanceUsageDaoImpl()

    override suspend fun handle(context: RoutingContext): Result {
        val parameters = getParameters(context)

        val page = parameters.queryParameter("page")?.long ?: 1

        val userId = authProvider.getUserIdFromRoutingContext(context)

        val jdbcPool = databaseManager.getConnectionPool()

        val count = balanceUsageDao.countById(userId, jdbcPool)

        var totalPage = kotlin.math.ceil(count.toDouble() / 10).toLong()

        if (totalPage < 1) {
            totalPage = 1
        }

        if (page > totalPage || page < 1) {
            throw PageNotFound()
        }

        val balanceUsages = balanceUsageDao.byUserId(userId, page, jdbcPool)

        return Successful(
            balanceUsages,
            mapOf(
                "totalCount" to count,
                "totalPage" to totalPage
            )
        )
    }
}