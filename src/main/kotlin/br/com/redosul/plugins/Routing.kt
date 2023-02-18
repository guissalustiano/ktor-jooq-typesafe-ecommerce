package br.com.redosul.plugins

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.enums.ClotheSize
import br.com.redosul.product.ProductSetPayload
import br.com.redosul.product.ProductVariantSetPayload
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.resources.*
import io.ktor.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.application.*

fun Application.configureRouting() {
    install(Resources)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
