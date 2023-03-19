package br.com.redosul.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

@Serializable
sealed class Undefined<out T> {
    @Serializable
    object None : Undefined<Nothing>()

    @Serializable
    data class Defined<T>(val value: T) : Undefined<T>()
}

inline fun <reified T> Undefined<T>.ifDefined(transform: (T) -> Unit) {
    when (this) {
        Undefined.None -> Undefined.None
        is Undefined.Defined -> Undefined.Defined(transform(value))
    }
}

inline fun <reified T> Undefined<T>.getOrElse(default: () -> T): T {
    return when (this) {
        Undefined.None -> default()
        is Undefined.Defined -> value
    }
}

inline fun <reified T> Undefined<T>.get(): T = getOrElse { throw Exception("Undefined not found $this") }

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

typealias UUID = @Serializable(UUIDSerializer::class) java.util.UUID