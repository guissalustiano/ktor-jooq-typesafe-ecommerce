package br.com.redosul.user

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
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class UserControllerTest : FunSpec({
    val userService = mockk<UserService>()

    fun ApplicationTestBuilder.setup(): HttpClient {
        application {
            baseConfig()
            userRoutes(userService)
        }
        return createClient {
            configureSerialization()
        }
    }

    context("GET /users") {
        test("should return 200") {
            testApplication {
                val client = setup()
                coEvery { userService.findAll() } returns (0..5).map { UserFaker.response() }

                client.get("/users").apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText().shouldNotBeEmpty()
                }
            }
        }
    }

    context("POST /users") {
        test("should return 201") {
            testApplication {
                val client = setup()
                coEvery { userService.create(any()) } returns Unit

                val payload = UserFaker.Default.createPayload
                client.post("/users") {
                    setJsonBody(payload)
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                coVerify { userService.create(payload) }
            }
        }
    }

    context("DELETE /categories/:id") {
        test("should return 200") {
            testApplication {
                val client = setup()
                val slug = UserFaker.Default.createPayload.slug
                coEvery { userService.deleteBySlug(slug) } returns Unit

                client.delete("/users/${slug.slug}").apply {
                    status shouldBe HttpStatusCode.NoContent
                }

                coVerify { userService.deleteBySlug(slug) }
            }
        }

        test("should return 404") {
            testApplication {
                val client = setup()
                val slug = UserFaker.Default.createPayload.slug
                coEvery { userService.deleteBySlug(slug) } returns null

                client.delete("/users/${slug.slug}").apply {
                    status shouldBe HttpStatusCode.NotFound
                }

                coVerify { userService.deleteBySlug(slug) }
            }
        }
    }

    context("GET /users/:slug") {
        test("should return 200") {
            testApplication {
                val client = setup()
                val slug = UserFaker.Default.createPayload.slug
                coEvery { userService.findBySlug(slug) } returns UserFaker.Default.response

                client.get("/users/${slug.slug}").apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText().shouldNotBeEmpty()
                }

                coVerify { userService.findBySlug(slug) }
            }
        }

        test("should return 404") {
            testApplication {
                val client = setup()
                val slug = UserFaker.Default.createPayload.slug
                coEvery { userService.findBySlug(slug) } returns null

                client.get("/users/${slug.slug}").apply {
                    status shouldBe HttpStatusCode.NotFound
                }

                coVerify { userService.findBySlug(slug) }
            }
        }
    }

    context("PUT /users/:slug") {
        test("should return 200") {
            testApplication {
                val client = setup()
                val payload = UserFaker.Default.createPayload
                coEvery { userService.createOrUpdate(any()) } returns Unit

                client.put("/users") {
                    setJsonBody(payload)
                }.apply {
                    status shouldBe HttpStatusCode.NoContent
                    bodyAsText().shouldNotBeEmpty()
                }

                coVerify { userService.createOrUpdate(payload) }
            }
        }
    }
})
