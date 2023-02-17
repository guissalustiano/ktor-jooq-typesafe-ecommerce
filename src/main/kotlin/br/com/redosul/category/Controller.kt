package br.com.redosul.category

import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.toSlug
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.DSL.name
import java.time.ZonedDateTime

@Serializable
data class CategorySetPayload(
    val parentId: CategoryId? = null,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = ""
)

private fun CategorySetPayload.toRecord() = CategoryRecord().also {
    it.parentId = parentId?.value
    it.name = name
    it.slug = slug.value
    it.description = description
}

@JvmInline
@Serializable
value class CategoryId(val value: Long)

@Serializable
data class CategoryResponse(
    val id: CategoryId,
    val parentId: CategoryId?,
    val name: String,
    val slug: Slug,
    val description: String)

@Serializable
data class CategoryTreeResponse(
    val id: CategoryId,
    val name: String,
    val slug: Slug,
    val description: String,
    val children: List<CategoryTreeResponse>?,
)

private fun CategoryRecord.toResponse() = CategoryResponse(
    CategoryId(id!!),
    parentId?.let { CategoryId(it) },
    name!!,
    Slug(slug!!),
    description!!
)

@Resource("/categories")
class CategoryResource {
    @Resource("{id}")
    class Id(val parent: CategoryResource, val id: CategoryId) {
        @Resource("products")
        class Product(val parent: CategoryResource.Id)
    }

}

fun Application.categoryRoutes(dsl: DSLContext) {
    routing {
        get<CategoryResource> {resource ->
            val records = dsl.selectFrom(CATEGORY)
                .fetchInto(CATEGORY)

            fun getChildren(parentId: CategoryId?): List<CategoryTreeResponse> {
                return records.map { it.toResponse() }.filter {
                    it.parentId == parentId
                }.map {
                    CategoryTreeResponse(
                        it.id,
                        it.name,
                        it.slug,
                        it.description,
                        getChildren(it.id).ifEmpty { null }
                    )
                }
            }

            call.respond(getChildren(null))
        }

        post<CategoryResource> {resource ->
            val payload = call.receive<CategorySetPayload>()

            val record = dsl.insertInto(CATEGORY)
                .set(payload.toRecord())
                .set(CATEGORY.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .set(CATEGORY.CREATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .returningResult(asterisk())
                .fetchOneInto(CATEGORY)!!

            call.respond(record.toResponse())
            call.response.status(HttpStatusCode.Created)
        }

        get<CategoryResource.Id> {resource ->
            val record = dsl.selectFrom(CATEGORY)
                .where(CATEGORY.ID.eq(resource.id.value))
                .fetchOneInto(CATEGORY)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(record.toResponse())
        }

        post<CategoryResource.Id> {resource ->
            val payload = call.receive<CategorySetPayload>()

            require(resource.id != payload.parentId) {
                "Category cannot be its own parent"
            }

            val record = dsl.update(CATEGORY)
                .set(payload.toRecord())
                .set(CATEGORY.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .where(CATEGORY.ID.eq(resource.id.value))
                .returningResult(asterisk())
                .fetchOneInto(CATEGORY)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            call.respond(record.toResponse())
        }

        delete<CategoryResource.Id> {resource ->
            val record = dsl.deleteFrom(CATEGORY)
                .where(CATEGORY.ID.eq(resource.id.value))
                .returningResult(asterisk())
                .fetchOneInto(CATEGORY)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(record.toResponse())
        }
    }
}


