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
value class Slug(val value: String) {
    init {
        require(value.matches(Regex("[a-z0-9-]+"))) { "Slug must be lowercase, alphanumeric and hyphenated, current: $value" }
    }
    companion object {
        fun from(value: String) = Slug(value.lowercase().replace(" ", "-"))
    }
}

fun String.toSlug() = Slug.from(this)


fun OffsetDateTime.toKotlinInstant(): Instant = toInstant().toKotlinInstant()

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
        require(value.matches(Regex("\\+[0-9]{13,15}"))) { "Phone must be 13 or 15 digits with +" }
    }
}