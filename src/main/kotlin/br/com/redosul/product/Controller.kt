package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.generated.tables.references.PRODUCT
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
import org.jooq.impl.DSL.asterisk
import java.time.ZonedDateTime

@Serializable
data class ProductSetPayload(
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = ""
)

private fun ProductSetPayload.toRecord() = ProductRecord().also {
    it.categoryId = categoryId.value
    it.name = name
    it.slug = slug.value
    it.description = description
}

@JvmInline
@Serializable
value class ProductId(val value: Long)

@Serializable
data class ProductResponse(
    val id: ProductId,
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String
)
private fun ProductRecord.toResponse() = ProductResponse(
    ProductId(id!!),
    CategoryId(categoryId!!),
    name!!,
    Slug(slug!!),
    description!!
)


@Resource("/products")
class ProductsResource {
    @Resource("{id}")
    class Id(val parent: ProductsResource = ProductsResource(), val id: Long)
}

fun Application.product(dsl: DSLContext) {
    routing {
        get<ProductsResource> {resource ->
            val records = dsl.selectFrom(PRODUCT)
                .fetchInto(PRODUCT)

            call.respond(records.map { it.toResponse() })
        }

        post<ProductsResource> {resource ->
            val payload = call.receive<ProductSetPayload>()

            val record = dsl.insertInto(PRODUCT)
                .set(payload.toRecord())
                .set(PRODUCT.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .set(PRODUCT.CREATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .returningResult(asterisk())
                .fetchOneInto(PRODUCT)!!

            call.respond(record.toResponse())
            call.response.status(HttpStatusCode.Created)
        }

        get<ProductsResource.Id> {resource ->
            val record = dsl.selectFrom(PRODUCT)
                .where(PRODUCT.ID.eq(resource.id))
                .fetchOneInto(PRODUCT)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(record.toResponse())
        }

        post<ProductsResource.Id> {resource ->
            val payload = call.receive<ProductSetPayload>()

            val record = dsl.update(PRODUCT)
                .set(payload.toRecord())
                .set(PRODUCT.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .where(PRODUCT.ID.eq(resource.id))
                .returningResult(asterisk())
                .fetchOneInto(PRODUCT)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@post
            }

            call.response.status(HttpStatusCode.Created)
            call.respond(record.toResponse())
        }

        delete<ProductsResource.Id> {resource ->
            val record = dsl.deleteFrom(PRODUCT)
                .where(PRODUCT.ID.eq(resource.id))
                .returningResult(asterisk())
                .fetchOneInto(PRODUCT)

            if (record == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(record.toResponse())
        }
    }
}


