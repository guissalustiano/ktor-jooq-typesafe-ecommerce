package br.com.redosul

import br.com.redosul.plugins.configureRouting
import br.com.redosul.plugins.configureSerialization
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText() shouldBe "Hello World!"
        }
    }
}


fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

inline fun <reified T> HttpRequestBuilder.setJsonBody(body: T) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

fun Application.baseConfig() {
    configureSerialization()
    configureRouting()
}