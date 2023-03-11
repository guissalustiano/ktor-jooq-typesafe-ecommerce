package br.com.redosul.category

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
    @Resource("{id}")
    /*
     * GET - Find by id: CategoryId -> CategoryDto
     * POST - Update: (CategoryId, CategoryDto) -> CategoryDto
     * DELETE - Delete: CategoryId -> CategoryDto
     */
    class Id(val parent: CategoryResource, val id: CategoryId)
}

fun Application.categoryRoutes(service: CategoryService) {
    routing {
        get<CategoryResource> {_ ->
            service.findAll().let{
                call.respond(it)
            }
        }

        get<CategoryResource.Id> {resource ->
            service.findById(resource.id)?.let{
                call.respond(it)
            }
        }

        post<CategoryResource> {_ ->
            val payload = call.receive<CategoryDto>()
            service.create(payload).let{
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        post<CategoryResource.Id> {resource ->
            val payload = call.receive<CategoryDto>()
            service.updateById(resource.id, payload)?.let {
                call.respond(it)
            }
        }

        delete<CategoryResource.Id> {resource ->
            service.deleteById(resource.id)?.let {
                call.respond(it)
            }
        }
    }
}


