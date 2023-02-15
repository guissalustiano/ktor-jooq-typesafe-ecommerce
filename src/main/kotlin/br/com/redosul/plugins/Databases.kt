package br.com.redosul.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.*
import io.ktor.server.application.*
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.TableField
import org.jooq.UpdateSetMoreStep
import org.jooq.UpdateSetStep
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
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