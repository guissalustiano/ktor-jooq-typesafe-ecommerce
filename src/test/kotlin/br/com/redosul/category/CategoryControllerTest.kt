package br.com.redosul.category;

import br.com.redosul.baseConfig
import br.com.redosul.configureSerialization
import br.com.redosul.setJsonBody
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CategoryControllerTest : FunSpec({
    val categoryService = mockk<CategoryService>()

    fun ApplicationTestBuilder.setup(): HttpClient {
        application {
            baseConfig()
            categoryRoutes(categoryService)
        }
        return createClient {
            configureSerialization()
        }
    }

    context("GET /categories") {
        test("should return 200") {
            testApplication {
                val client = setup()
                coEvery { categoryService.findAll() } returns (0..5).map { CategoryFaker.treeResponse() }

                client.get("/categories").apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText().shouldNotBeEmpty()
                }

                coVerify { categoryService.findAll() }
            }
        }
    }



    context("POST /categories") {
        test("should return 201") {
            testApplication {
                val client = setup()
            coEvery { categoryService.create(any()) } returns CategoryFaker.response()

            val payload = CategoryFaker.createPayload()
            client.post("/categories") {
                setJsonBody(payload)
            }.apply {
                status shouldBe HttpStatusCode.Created
            }

            coVerify { categoryService.create(payload) }
        }}
    }

    context("DELETE /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
            val id = CategoryId()
            coEvery { categoryService.deleteById(id) } returns CategoryFaker.response().copy(id = id)

            client.delete("/categories/${id.value}").apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText().shouldNotBeEmpty()
            }

            coVerify { categoryService.deleteById(id) }
        }}

        test("should return 404 when not found") {
            testApplication {
                val client = setup()
            val id = CategoryId()
            coEvery { categoryService.deleteById(id) } returns null

            client.delete("/categories/${id.value}").apply {
                status shouldBe HttpStatusCode.NotFound
            }

            coVerify { categoryService.deleteById(id) }
        }}
    }

    context("GET /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
            val id = CategoryId()
            coEvery { categoryService.findById(id) } returns CategoryFaker.response().copy(id = id)

            client.get("/categories/${id.value}").apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText().shouldNotBeEmpty()
            }

            coVerify { categoryService.findById(id) }
        }}

        test("should return 404 when not found") {
            testApplication {
                val client = setup()
            val id = CategoryId()
            coEvery { categoryService.findById(id) } returns null

            client.get("/categories/${id.value}").apply {
                status shouldBe HttpStatusCode.NotFound
            }

            coVerify { categoryService.findById(id) }
        }}
    }

    context("POST /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
            val id = CategoryId()
            val payload = CategoryFaker.createPayload()
            coEvery { categoryService.updateById(id, any()) } returns CategoryFaker.response().copy(id = id)

            client.post("/categories/${id.value}") {
                setJsonBody(payload)
            }.apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText().shouldNotBeEmpty()
            }

            coVerify { categoryService.updateById(id, any()) }
        }}

        test("should return 404") {
            testApplication {
                val client = setup()
            val id = CategoryId()
            val payload = CategoryFaker.createPayload()
            coEvery { categoryService.updateById(id, any()) } returns null

            client.post("/categories/${id.value}") {
                setJsonBody(payload)
            }.apply {
                status shouldBe HttpStatusCode.NotFound
            }

            coVerify { categoryService.updateById(id, any()) }
        }}
    }
})