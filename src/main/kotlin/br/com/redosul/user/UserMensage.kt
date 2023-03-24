package br.com.redosul.user

import br.com.redosul.plugins.Email
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Name
import br.com.redosul.plugins.Phone
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.SlugId
import br.com.redosul.plugins.UUID
import br.com.redosul.plugins.Undefined
import br.com.redosul.plugins.toSlug
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class UserId(override val value: UUID = UUID.randomUUID()): Id

@JvmInline
@Serializable
value class UserSlug(override val value: Slug): SlugId

@Serializable
data class UserCreatePayload(
    val email: Email,
    val slug: UserSlug = email.toSlug(),
    val name: Name,
    val phone: Phone,
)

private fun Email.toSlug(): UserSlug {
    val emailPart = value
        .replace("@", "-")
        .replace(".", "_")

    val randomPart = UUID.randomUUID().toString().substring(0, 5)

    return "$emailPart $randomPart".toSlug().let(::UserSlug)
}

@Serializable
data class UserUpdatePayload(
    val name: Undefined<Name> = Undefined.None,
    val phone: Undefined<Phone> = Undefined.None,
)

@Serializable
data class UserResponse(
    val slug: UserSlug,
    val email: Email,
    val name: Name,
    val phone: Phone,
    val createdAt: Instant,
    val updatedAt: Instant,
)