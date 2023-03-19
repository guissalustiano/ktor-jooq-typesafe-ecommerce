package br.com.redosul.category

import br.com.redosul.plugins.Undefined
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.*
import org.jooq.*
import org.jooq.impl.*

@Resource("/categories")
/*
 * GET - List: () -> CategoryTreeDto
 * POST - Create: CategoryDto -> CategoryDto
 */
class CategoryResource {
    @Resource("{slug}")
    /*
     * GET - Find by id: CategoryId -> CategoryDto
     * POST - Update: (CategoryId, CategoryDto) -> CategoryDto
     * DELETE - Delete: CategoryId -> CategoryDto
     */
    class Id(val parent: CategoryResource, val slug: CategorySlug)
}

fun Application.categoryRoutes(service: CategoryService) {
    routing {
        get<CategoryResource> {_ ->
            service.findAll().let{
                call.respond(it)
            }
        }

        get<CategoryResource.Id> {resource ->
            service.findBySlug(resource.slug)?.let{
                call.respond(it)
            }
        }

        post<CategoryResource> {_ ->
            val payload = call.receive<CategoryCreatePayload>()
            service.create(payload).let{
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        // TODO: use PATCH
        post<CategoryResource.Id> {resource ->
            val payload = call.receive<CategoryCreatePayload>()
            service.updateBySlug(resource.slug, payload.toUpdatePayload())?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }

        delete<CategoryResource.Id> {resource ->
            service.deleteBySlug(resource.slug)?.let {
                call.response.status(HttpStatusCode.NoContent)
                call.respond(it)
            }
        }
    }
}

private fun CategoryCreatePayload.toUpdatePayload() = CategoryUpdatePayload(
    parentSlug = Undefined.Defined(parentSlug),
    name = Undefined.Defined(name),
    slug = Undefined.Defined(slug),
    description = Undefined.Defined(description),
)


