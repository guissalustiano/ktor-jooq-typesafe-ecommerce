package br.com.redosul.plugins

import io.ktor.server.application.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.InsertSetMoreStep
import org.jooq.InsertSetStep
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.SQLDialect
import org.jooq.TableField
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import reactor.core.publisher.Flux
import java.time.Duration

data class DatabaseConfig(
    val url: String,
    val username: String,
    val password: String,
    val maxIdleTime: Duration = Duration.ofMillis(1000),
    val maxSize: Int = 20
)

fun Application.getEnvDatabaseConfig() = DatabaseConfig(
    url = "r2dbc:" + (System.getenv("DB_URL") ?: "postgresql://localhost:65432/redosul"),
    username = System.getenv("DB_USER") ?: "postgres",
    password = System.getenv("DB_PASSWORD") ?: "password",
)

fun configureDatabases(config: DatabaseConfig): DSLContext {
    val dataSource = createDataSource(config)
    return createDSLContext(dataSource)
}

private fun createDataSource(config: DatabaseConfig): ConnectionPool {
    val connectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions
            .parse(config.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, config.username)
            .option(ConnectionFactoryOptions.PASSWORD, config.password)
            .build()
    )

    val configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(Duration.ofMillis(1000))
        .maxSize(20)
        .build()

    return ConnectionPool(configuration);
}

private fun createDSLContext(dataSource: ConnectionPool): DSLContext {
    val settings = Settings()
        .withRenderMapping(
            RenderMapping()
                .withSchemata(
                    MappedSchema().withInput("public")
                        .withOutput("public")
                )
        )

    return DSL.using(dataSource, SQLDialect.POSTGRES, settings)
}

// async utils
suspend inline fun <R : Record> ResultQuery<R>.await(): List<R> = Flux.from(this).collectList().awaitSingle()