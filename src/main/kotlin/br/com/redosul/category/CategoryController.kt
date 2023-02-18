package br.com.redosul.category

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.*
import org.jooq.*
import org.jooq.impl.*

@Resource("/categories")
class CategoryResource {
    @Resource("{id}")
    class Id(val parent: CategoryResource, val id: CategoryId)
}

fun Application.categoryRoutes(service: CategoryService) {
    routing {
        get<CategoryResource> {_ ->
            val records = service.findAll()
            call.respond(records.toTreeResponse())
        }

        get<CategoryResource.Id> {resource ->
            val record = service.findById(resource.id)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(record.toResponse())
        }

        post<CategoryResource> {_ ->
            val payload = call.receive<CategorySetPayload>()
            val record = service.create(payload)

            call.response.status(HttpStatusCode.Created)
            call.respond(record.toResponse())
        }

        post<CategoryResource.Id> {resource ->
            val payload = call.receive<CategorySetPayload>()
            val record = service.updateById(resource.id, payload)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            call.respond(record.toResponse())
        }

        delete<CategoryResource.Id> {resource ->
            val record = service.deleteById(resource.id)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(record.toResponse())
        }
    }
}


