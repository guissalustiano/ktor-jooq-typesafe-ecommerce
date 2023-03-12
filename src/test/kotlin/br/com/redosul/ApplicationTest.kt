package br.com.redosul

import br.com.redosul.plugins.configureRouting
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.Test

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
