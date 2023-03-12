package br.com.redosul.category;

import br.com.redosul.configureSerialization
import br.com.redosul.plugins.configureRouting
import br.com.redosul.plugins.configureSerialization
import br.com.redosul.setJsonBody
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test

class CategoryControllerTest {
    private val categoryService = mockk<CategoryService>()

    @Test
    fun testGetCategories() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }
        coEvery { categoryService.findAll() } returns (0..5).map { CategoryFaker.categoryTree() }


        client.get("/categories").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText().shouldNotBeEmpty()
        }

        coVerify { categoryService.findAll() }
    }

    @Test
    fun testPostCategories() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }
        val client = createClient {
            configureSerialization()
        }
        coEvery { categoryService.create(any()) } returns CategoryFaker.category()

        val payload = CategoryFaker.category()
        client.post("/categories"){
            setJsonBody(payload)
        }.apply {
            status shouldBe HttpStatusCode.Created
        }

        coVerify { categoryService.create(payload) }
    }

    @Test
    fun testDeleteCategoriesId() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }
        val id = CategoryId()
        coEvery { categoryService.deleteById(id) } returns CategoryFaker.category().copy(id = id)

        client.delete("/categories/${id.value}").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText().shouldNotBeEmpty()
        }

        coVerify { categoryService.deleteById(id) }
    }

    @Test
    fun testDeleteCategoriesIdNull() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }
        val id = CategoryId()
        coEvery { categoryService.deleteById(id) } returns null

        client.delete("/categories/${id.value}").apply {
            status shouldBe HttpStatusCode.NotFound
        }

        coVerify { categoryService.deleteById(id) }
    }

    @Test
    fun testGetCategoriesId() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }

        val id = CategoryId()
        coEvery { categoryService.findById(id) } returns CategoryFaker.category().copy(id = id)

        client.get("/categories/${id.value}").apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText().shouldNotBeEmpty()
        }

        coVerify { categoryService.findById(id) }
    }

    @Test
    fun testGetCategoriesIdNull() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }

        val id = CategoryId()
        coEvery { categoryService.findById(id) } returns null

        client.get("/categories/${id.value}").apply {
            status shouldBe HttpStatusCode.NotFound
        }

        coVerify { categoryService.findById(id) }
    }

    @Test
    fun testPostCategoriesId() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }
        val client = createClient {
            configureSerialization()
        }

        val id = CategoryId()
        val payload = CategoryFaker.category().copy(id = id)
        coEvery { categoryService.updateById(id, payload) } returns CategoryFaker.category().copy(id = id)

        client.post("/categories/${id.value}"){
            setJsonBody(payload)
        }.apply {
            status shouldBe HttpStatusCode.OK
            bodyAsText().shouldNotBeEmpty()
        }

        coVerify { categoryService.updateById(id, payload) }
    }

    @Test
    fun testPostCategoriesIdNull() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            categoryRoutes(categoryService)
        }
        val client = createClient {
            configureSerialization()
        }

        val id = CategoryId()
        val payload = CategoryFaker.category().copy(id = id)
        coEvery { categoryService.updateById(id, payload) } returns null

        client.post("/categories/${id.value}"){
            setJsonBody(payload)
        }.apply {
            status shouldBe HttpStatusCode.NotFound
        }

        coVerify { categoryService.updateById(id, payload) }
    }
}
