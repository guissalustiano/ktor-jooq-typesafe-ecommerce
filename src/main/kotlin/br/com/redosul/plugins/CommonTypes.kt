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
        require(value.matches(Regex("[a-z0-9-]+"))) { "Slug must be lowercase, alphanumeric and hyphenated" }
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