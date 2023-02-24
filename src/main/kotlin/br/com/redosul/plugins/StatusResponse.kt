package br.com.redosul.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException


@OptIn(ExperimentalSerializationApi::class)
fun Application.configureStatusResponse() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val firstCause = (cause.cause as? JsonConvertException) ?: return@exception
            val secondCause = (firstCause.cause as? SerializationException) ?: return@exception

            if (secondCause is MissingFieldException) {
                call.respond(HttpStatusCode.BadRequest, "Missing field: ${secondCause.missingFields}")
            }

            val message = secondCause.localizedMessage ?: return@exception
            val filteredMessage = message
                .replace(Regex("""Use '.*' in 'Json \{}'.*"""), "")
                .replace(Regex("""It is possible to deserialize them using 'JsonBuilder.*"""), "")
                .replace("\n\n", "\n")

            call.respond(HttpStatusCode.BadRequest, filteredMessage)
        }
    }
}
