package br.com.redosul.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UndefinableSerializer::class)
sealed class Undefinable<out T> {
    object Undefined : Undefinable<Nothing>()
    data class Defined<T>(val value: T) : Undefinable<T>()
}

fun <T> Undefinable<T>.ifPresent(predicate: (T) -> Unit) = when (this) {
    is Undefinable.Defined -> predicate(value)
    is Undefinable.Undefined -> Unit
}

class UndefinableSerializer<T>(private val dataSerializer: KSerializer<T>): KSerializer<Undefinable<T>> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("Undefinable<${dataSerializer.descriptor.serialName}>", PolymorphicKind.SEALED)

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