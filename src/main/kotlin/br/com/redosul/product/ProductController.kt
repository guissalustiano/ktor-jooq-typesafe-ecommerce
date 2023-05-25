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
    @Resource("{slug}")
    class Id(val parent: ProductsResource = ProductsResource(), val slug: ProductSlug)

    @Resource("variants")
    class Variants(val parent: ProductsResource = ProductsResource()) {
        @Resource("{slug}")
        class Id(val parent: Variants = Variants(), val slug: ProductVariantSlug)
    }

    @Resource("images")
    class Images(val parent: ProductsResource = ProductsResource()) {
        @Resource("{slug}")
        class Id(val parent: Images = Images(), val slug: ProductImageSlug)
    }
}

fun Application.productRoutes(service: ProductService) {
    routing {
        get<ProductsResource> {resource ->
            service.findAll(resource.categoryId).let {
                call.respond(it)
            }
        }

        get<ProductsResource.Id> { resource ->
            service.findBySlug(resource.slug)?.let {
                call.respond(it)
            }
        }

        post<ProductsResource> {_ ->
            val payload = call.receive<ProductCreatePayload>()
            service.create(payload).let {
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        post<ProductsResource.Variants> {_ ->
            val payload = call.receive<ProductVariantCreatePayload>()
            service.create(payload).let {
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        post<ProductsResource.Images> {_ ->
            val payload = call.receive<ProductImageCreatePayload>()
            service.create(payload).let {
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        put<ProductsResource> {resource ->
            val payload = call.receive<ProductCreatePayload>()
            service.createOrUpdate(payload)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }

        put<ProductsResource.Variants> {resource ->
            val payload = call.receive<ProductVariantCreatePayload>()
            service.createOrUpdate(payload)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }

        put<ProductsResource.Images> {resource ->
            val payload = call.receive<ProductImageCreatePayload>()
            service.createOrUpdate(payload)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }

        delete<ProductsResource.Id> {resource ->
            service.deleteBySlug(resource.slug)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }

        delete<ProductsResource.Variants.Id> {resource ->
            service.deleteBySlug(resource.slug)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }

        delete<ProductsResource.Images.Id> {resource ->
            service.deleteBySlug(resource.slug)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }
    }
}


