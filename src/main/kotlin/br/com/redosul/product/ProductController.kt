package br.com.redosul.product

import br.com.redosul.category.CategoryId
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
            service.findAll(resource.categoryId).let {
                call.respond(it)
            }
        }

        get<ProductsResource.Id> { resource ->
            service.findById(resource.id)?.let {
                call.respond(it)
            }
        }

        post<ProductsResource> {_ ->
            val payload = call.receive<ProductDto>()
            service.create(payload).let {
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        post<ProductsResource.Id> {resource ->
            val payload = call.receive<ProductDto>()
            service.updateById(resource.id, payload)?.let {
                call.respond(it)
            }
        }

        delete<ProductsResource.Id> {resource ->
            service.deleteById(resource.id)?.let {
                call.respond(it)
            }
        }
    }
}


