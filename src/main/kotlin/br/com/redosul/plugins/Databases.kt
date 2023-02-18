package br.com.redosul.plugins

import io.ktor.server.application.*
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Result
import org.jooq.ResultQuery
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import reactor.core.publisher.Flux

fun Application.configureDatabases(): DSLContext {
    val dataSource = createDataSource()
    return createDSLContext(dataSource)
}

private fun createDataSource(): ConnectionFactory {
    val url = System.getenv("DB_URL") ?: "r2dbc:postgresql://localhost:65432/redosul"
    val username = System.getenv("DB_USER") ?: "postgres"
    val password = System.getenv("DB_PASSWORD") ?: "password"

    return ConnectionFactories.get(
        ConnectionFactoryOptions
            .parse(url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, username)
            .option(ConnectionFactoryOptions.PASSWORD, password)
            .build()
    )
}

private fun createDSLContext(dataSource: ConnectionFactory): DSLContext {
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

interface Id {
    val value: Long
}

// async utils
suspend fun <R : Record> ResultQuery<in R>.awaitFirstOrNullInto(table: Table<R>): R? = this.awaitFirstOrNull()?.into(table)
suspend fun <R : Record> ResultQuery<in R>.awaitFirstInto(table: Table<R>): R = this.awaitFirst().into(table)

suspend fun <R : Record> ResultQuery<R>.await(): Iterable<R> = Flux.from(this).collectList().awaitSingle()
suspend fun <R : Record> ResultQuery<in R>.awaitInto(table: Table<R>): Iterable<R> = this.await().map { it.into(table) }