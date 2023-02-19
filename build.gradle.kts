import org.jooq.meta.jaxb.Logging

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version : String by project
val postgres_r2dbc_version : String by project
val h2_version : String by project
val flyway_version: String by project
val hikaricp_version: String by project
val arrow_version : String by project
val kotest_version : String by project
val jooq_version : String by project
val kotlin_couroutines_version : String by project
val r2dbc_pool_version: String by project
val kotlinx_datetime_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
    id("org.flywaydb.flyway") version "9.14.1"
    id("nu.studer.jooq") version "8.1"
}

group = "br.com.redosul"
version = "0.0.1"
application {
    mainClass.set("br.com.redosul.ApplicationKt")

    val isDevelopment: Boolean = true // project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_couroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlin_couroutines_version")
    implementation("org.jooq:jooq-kotlin:$jooq_version")
    implementation("org.jooq:jooq-kotlin-coroutines:$jooq_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_datetime_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.2.3")
    jooqGenerator("org.postgresql:postgresql:$postgres_version")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("org.postgresql:r2dbc-postgresql:$postgres_r2dbc_version")
    implementation("io.r2dbc:r2dbc-pool:$r2dbc_pool_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-property:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}


flyway {
    url = "jdbc:" + System.getenv("DB_URL")
    user = System.getenv("DB_USER")
    password = System.getenv("DB_PASSWORD")

    cleanOnValidationError = true
    cleanDisabled = false
}

jooq {
    version.set(jooq_version)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                logging = Logging.INFO
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:" + System.getenv("DB_URL")
                    user = System.getenv("DB_USER")
                    password = System.getenv("DB_PASSWORD")
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        excludes = "flyway_schema_history"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isRelations = true
                        isPojosAsKotlinDataClasses = true
                    }
                    target.apply {
                        packageName = "br.com.redosul.generated"
                    }
                }
            }
        }
    }
}
