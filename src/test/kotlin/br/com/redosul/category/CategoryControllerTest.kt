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
                coEvery { categoryService.create(any()) } returns Unit

                val payload = CategoryFaker.createPayload()
                client.post("/categories") {
                    setJsonBody(payload)
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                coVerify { categoryService.create(payload) }
            }
        }
    }

    context("DELETE /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
                val slug = CategoryFaker.response().slug
                coEvery { categoryService.deleteBySlug(slug) } returns Unit

                client.delete("/categories/${slug.slug}").apply {
                    status shouldBe HttpStatusCode.NoContent
                    bodyAsText().shouldNotBeEmpty()
                }

                coVerify { categoryService.deleteBySlug(slug) }
            }
        }

        test("should return 404 when not found") {
            testApplication {
                val client = setup()
                val slug = CategoryFaker.response().slug
                coEvery { categoryService.deleteBySlug(slug) } returns null

                client.delete("/categories/${slug.slug}").apply {
                    status shouldBe HttpStatusCode.NotFound
                }

                coVerify { categoryService.deleteBySlug(slug) }
            }
        }
    }

    context("GET /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
                val fake = CategoryFaker.response()
                coEvery { categoryService.findBySlug(fake.slug) } returns fake

                client.get("/categories/${fake.slug.slug}").apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText().shouldNotBeEmpty()
                }

                coVerify { categoryService.findBySlug(fake.slug) }
            }
        }

        test("should return 404 when not found") {
            testApplication {
                val client = setup()
                val fake = CategoryFaker.response()
                coEvery { categoryService.findBySlug(fake.slug) } returns null

                client.get("/categories/${fake.slug.slug}").apply {
                    status shouldBe HttpStatusCode.NotFound
                }

                coVerify { categoryService.findBySlug(fake.slug) }
            }
        }
    }

    context("POST /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
                val fake = CategoryFaker.createPayload()
                coEvery { categoryService.updateBySlug(fake.slug, any()) } returns Unit

                client.post("/categories/${fake.slug.slug}") {
                    setJsonBody(fake)
                }.apply {
                    status shouldBe HttpStatusCode.NoContent
                    bodyAsText().shouldNotBeEmpty()
                }

                coVerify { categoryService.updateBySlug(fake.slug, any()) }
            }
        }

        test("should return 404") {
            testApplication {
                val client = setup()
                val fake = CategoryFaker.createPayload()
                coEvery { categoryService.updateBySlug(fake.slug, any()) } returns null

                client.post("/categories/${fake.slug.slug}") {
                    setJsonBody(fake)
                }.apply {
                    status shouldBe HttpStatusCode.NotFound
                }

                coVerify { categoryService.updateBySlug(fake.slug, any()) }
            }
        }
    }
})