package br.com.redosul.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
// object UndefinableSerializer: JsonContentPolymorphicSerializer<Undefinable<T>>

class UndefinableSerializer<T>(private val dataSerializer: KSerializer<T>): KSerializer<Undefinable<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Undefinable<T>) {
        when (value) {
            is Undefinable.Defined -> dataSerializer.serialize(encoder, value.value)
            is Undefinable.Undefined -> Unit
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Undefinable<T> {
        return try {
            Undefinable.Defined(dataSerializer.deserialize(decoder))
        } catch (e: MissingFieldException) {
            Undefinable.Undefined
        }
    }
}