package br.com.redosul.user

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

@Resource("/users")
/*
 * GET    - List: () -> UserResponse
 * POST   - Create: UserCreatePayload -> Unit
 * PUT    - Create/Update: UserCreatePayload -> Unit
 */
class UserResource {
    @Resource("{id}")
    /*
     * GET    - Find by id: UserId -> UserResponse
     * DELETE - Delete: UserId -> Unit?
     */
    class Id(val parent: UserResource, val slug: UserSlug)
}

fun Application.userRoutes(service: UserService) {
    routing {
        get<UserResource> { _ ->
            service.findAll().let{
                call.respond(it)
            }
        }

        get<UserResource.Id> { resource ->
            service.findBySlug(resource.slug)?.let{
                call.respond(it)
            }
        }

        post<UserResource> { _ ->
            val payload = call.receive<UserCreatePayload>()
            service.create(payload).let{
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        put<UserResource> { _ ->
            val payload = call.receive<UserCreatePayload>()
            service.createOrUpdate(payload).let {
                call.respond(it)
            }
        }

        delete<UserResource.Id> { resource ->
            service.deleteById(resource.slug)?.let {
                call.respond(it)
            }
        }
    }
}


