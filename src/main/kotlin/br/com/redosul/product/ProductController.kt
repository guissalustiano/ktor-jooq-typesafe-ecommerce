package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.pojos.Product
import br.com.redosul.generated.tables.pojos.ProductVariant
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing

@Resource("/products")
class ProductsResource(val categoryId: CategoryId? = null) {
    @Resource("{id}")
    class Id(val parent: ProductsResource = ProductsResource(), val id: ProductId)
}

fun Application.product(service: ProductService) {
    routing {
        get<ProductsResource> {resource ->
            val records = service.findAll(resource.categoryId)
            call.respond(records.map { it.toResponse() })
        }

        get<ProductsResource.Id> { resource ->
            val record = service.findById(resource.id)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(record.toResponse())
        }

        post<ProductsResource> {_ ->
            val payload = call.receive<ProductSetPayload>()

            val record = service.create(payload)

            call.response.status(HttpStatusCode.Created)
            call.respond(record.toResponse())
        }

        post<ProductsResource.Id> {resource ->
            val payload = call.receive<ProductSetPayload>()
            val record = service.updateById(resource.id, payload)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            call.respond(record.toResponse())
        }

        delete<ProductsResource.Id> {resource ->
            val record = service.deleteById(resource.id)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(record.toResponse())
        }
    }
}


