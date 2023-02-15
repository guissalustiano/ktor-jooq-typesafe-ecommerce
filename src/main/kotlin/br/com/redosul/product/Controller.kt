package br.com.redosul.product

import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.generated.tables.references.PRODUCT
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

@Resource("/products")
class ProductsResource {
    @Resource("{id}")
    class Id(val parent: ProductsResource = ProductsResource(), val id: Long)
}

@Serializable
data class ProductResponse(val id: Long, val name: String)
private fun ProductRecord.toResponse() = ProductResponse(
    id!!,
    name!!
)

@Serializable
data class ProductCreatePayload(val name: String)

@Serializable
// data class ProductUpdatePayload(val name: Undefinable<String>)
data class ProductUpdatePayload(val name: String? = null)

fun Application.product(dsl: DSLContext) {
    routing {
        get<ProductsResource> {
            val rows = dsl.selectFrom(PRODUCT)
                .fetchInto(PRODUCT)

            call.respond(listOf(rows.map { it.toResponse() }))
        }

        post<ProductsResource> {
            val payload = call.receive<ProductCreatePayload>()

            val row = dsl.insertInto(PRODUCT)
                .set(PRODUCT.NAME, payload.name)
                .returningResult(PRODUCT.ID, PRODUCT.NAME)
                .fetchOneInto(PRODUCT)!!

            call.respond(row.toResponse())
            call.response.status(HttpStatusCode.Created)
        }

        get<ProductsResource.Id> {
            val row = dsl.selectFrom(PRODUCT)
                .where(PRODUCT.ID.eq(it.id))
                .fetchOneInto(PRODUCT)

            if (row == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(row.toResponse())
        }

        patch<ProductsResource.Id> {
            val payload = call.receive<ProductUpdatePayload>()

            val record = dsl.newRecord(PRODUCT).apply {
                if (payload.name != null) {
                    name = payload.name
                }
            }

            val row = dsl.update(PRODUCT)
                .set(record)
                .where(PRODUCT.ID.eq(it.id))
                .returningResult(PRODUCT.ID, PRODUCT.NAME)
                .fetchOneInto(PRODUCT)

            if (row == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@patch
            }

            call.response.status(HttpStatusCode.Created)
            call.respond(row.toResponse())
        }

        delete<ProductsResource.Id> {
            val row = dsl.deleteFrom(PRODUCT)
                .where(PRODUCT.ID.eq(it.id))
                .returningResult(PRODUCT.ID, PRODUCT.NAME)
                .fetchOneInto(PRODUCT)

            if (row == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(row.toResponse())
        }
    }
}


