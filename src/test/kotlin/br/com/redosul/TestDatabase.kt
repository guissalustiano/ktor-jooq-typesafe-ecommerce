package br.com.redosul

import br.com.redosul.plugins.DatabaseConfig
import br.com.redosul.plugins.configureDatabases
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.testcontainers.containers.PostgreSQLContainer


private val <SELF : PostgreSQLContainer<SELF>?> PostgreSQLContainer<SELF>.r2dbcUrl: String
    get() = jdbcUrl.replace("jdbc:", "r2dbc:")

private object TestDatabase {
    private val containerDb by lazy {
        PostgreSQLContainer<Nothing>("postgres:15").apply {
            startupAttempts = 1
            withReuse(true)

            start()

            Flyway.configure()
                .dataSource(jdbcUrl, username, password).load().run {
                    migrate()
                }
        }
    }

    private val containerDbConfig
        get() = DatabaseConfig(
            url = containerDb.r2dbcUrl,
            username = containerDb.username,
            password = containerDb.password,
        )

    val dsl: DSLContext by lazy { configureDatabases(containerDbConfig) }
}

fun testDatabaseDsl() = TestDatabase.dsl