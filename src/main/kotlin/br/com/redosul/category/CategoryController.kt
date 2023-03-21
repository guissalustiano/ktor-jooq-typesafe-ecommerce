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
 * GET    - List: () -> CategoryResponseTree
 * POST   - Create: CategoryCreatePayload -> Unit
 * PUT    - Create/Update: CategoryCreatePayload -> Unit
 */
class CategoryResource {
    @Resource("{slug}")
    /*
     * GET    - Find by id: CategorySlug -> CategoryResponse
     * DELETE - Delete: CategorySlug -> Unit?
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

        put<CategoryResource> {_ ->
            val payload = call.receive<CategoryCreatePayload>()
            service.createOrUpdate(payload).let {
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


