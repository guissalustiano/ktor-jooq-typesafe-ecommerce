package br.com.redosul.user

import br.com.redosul.plugins.Email
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Name
import br.com.redosul.plugins.Phone
import br.com.redosul.plugins.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class UserId(override val value: UUID = UUID.randomUUID()): Id

@Serializable
data class UserDto(
    val id: UserId = UserId(),
    val email: Email,
    val name: Name,
    val phone: Phone,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)