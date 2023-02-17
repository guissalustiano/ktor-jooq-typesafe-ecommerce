package br.com.redosul.plugins

import br.com.redosul.generated.tables.Category
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.InsertResultStep
import org.jooq.Record
import org.jooq.Result
import org.jooq.ResultQuery
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl
import javax.sql.DataSource

fun Application.configureDatabases(): DSLContext {
    val dataSource = createDataSource()
    val dslContext = createDSLContext(dataSource)

    return dslContext
}

private fun createDataSource(): HikariDataSource {
    val driver = "org.postgresql.Driver"
    val url = System.getenv("DB_URL")
    val user = System.getenv("DB_USER")
    val pass = System.getenv("DB_PASSWORD")

    return HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        username = user
        password = pass
        maximumPoolSize = 3
        isAutoCommit = true
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })
}

private fun createDSLContext(dataSource: DataSource): DSLContext {
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
suspend fun <R : Record?> ResultQuery<in R>.awaitFirstOrNullInto(table: Table<R>) = this.awaitFirstOrNull()?.into(table)
suspend fun <R : Record?> ResultQuery<in R>.awaitFirstInto(table: Table<R>) = this.awaitFirst().into(table)

suspend fun <R : Record?> ResultQuery<R>.await(): Result<R> = fetchAsync().await()