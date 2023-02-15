package br.com.redosul.product

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

@Resource("/products")
class Products() {
    @Resource("{id}")
    class Id(val parent: Products = Products(), val id: Long)
}

@Serializable
data class ProductResponse(val id: Long, val name: String)

@Serializable
data class ProductCreatePayload(val name: String)

@Serializable
// data class ProductUpdatePayload(val name: Undefinable<String>)
data class ProductUpdatePayload(val name: String?)

fun Application.product() {
    routing {
        get<Products> {
            call.respond(listOf(
                ProductResponse(1, "Product 1"),
                ProductResponse(2, "Product 2"),
                ProductResponse(3, "Product 3")
            ))
        }

        post<Products> {
            val payload = call.receive<ProductCreatePayload>()

            val product = ProductResponse(1, payload.name)

            call.response.status(HttpStatusCode.Created)
            call.respond(product)
        }

        get<Products.Id> {
            val product = ProductResponse(it.id, "Product ${it.id}")

            call.respond(product)
        }

        patch<Products.Id> {
            val payload = call.receive<ProductUpdatePayload>()

            val product = ProductResponse(it.id, payload.name ?: "Product ${it.id}")

            call.response.status(HttpStatusCode.Created)
            call.respond(product)
        }

        delete<Products.Id> {
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}