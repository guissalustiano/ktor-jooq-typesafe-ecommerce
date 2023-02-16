package br.com.redosul.plugins

import kotlinx.serialization.Serializable

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