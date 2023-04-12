package br.com.redosul.plugins

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime


interface Id: Comparable<UUID> {
    val value: UUID

    override fun compareTo(other: UUID): Int = value.compareTo(other)
}

@JvmInline
@Serializable
value class Slug(val value: String): Comparable<Slug> {
    init {
        require(value.matches(Regex("[a-z0-9-]+"))) { "Slug must be lowercase, alphanumeric and hyphenated, current: $value" }
    }
    companion object {
        fun from(value: String) = Slug(value.lowercase().replace(" ", "-"))
    }

    override fun compareTo(other: Slug): Int = value.compareTo(other.value)
}

interface SlugId: Comparable<Slug>{
    val value: Slug
    val slug: String
        get() = value.value

    override fun compareTo(other: Slug): Int = value.compareTo(other)
}

fun String.toSlug() = Slug.from(this)


fun OffsetDateTime.toKotlinInstant(): Instant = toInstant().toKotlinInstant()

@Serializable
sealed class Undefined<out T> {
    @Serializable
    object None : Undefined<Nothing>()

    @Serializable
    data class Defined<T>(val value: T) : Undefined<T>()
}

inline fun <reified T, R> Undefined<T>.map(transform: (T) -> R): Undefined<R> {
    return when (this) {
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

inline fun <reified T> Undefined<T>.getOrNull(): T? = getOrElse { null }

inline fun <reified T> Undefined<T>.get(): T = getOrElse { throw Exception("Undefined not found $this") }


@JvmInline
@Serializable
value class URL(val value: String) {
    init {
        require(value.matches(Regex("https?://.+"))) { "URL must be http or https" }
    }
}

@JvmInline
@Serializable
value class Email(val value: String) {
    init {
        require(value.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) { "Invalid email" }
    }
}

@Serializable
data class Name(val first: String, val last: String) {
    val full: String get() = "$first $last"

    init {
        require(first.isNotBlank()) { "First name cannot be blank" }
        require(last.isNotBlank()) { "Last name cannot be blank" }
    }
}

@JvmInline
@Serializable
value class Phone(val value: String) {
    init {
        require(value.matches(Regex("\\+[0-9]{11,}"))) { "Phone must be 11 or 15 digits with +, got $value" }
    }
}