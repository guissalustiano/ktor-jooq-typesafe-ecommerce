package br.com.redosul.plugins

import kotlinx.serialization.Serializable

@Serializable(with = UndefinableSerializer::class)
sealed class Undefinable<out T> {
    object Undefined : Undefinable<Nothing>()
    data class Defined<T>(val value: T) : Undefinable<T>()
}

fun <T> Undefinable<T>.ifPresent(predicate: (T) -> Unit) = when (this) {
    is Undefinable.Defined -> predicate(value)
    is Undefinable.Undefined -> Unit
}
