package br.com.redosul.category

import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.UUID
import br.com.redosul.plugins.toSlug
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CategoryId(override val value: UUID = UUID.randomUUID()): Id

@Serializable
data class CategoryDto(
    val id: CategoryId = CategoryId(),
    val parentId: CategoryId? = null,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = "",
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    init {
        require(id != parentId) {
            "Category cannot be its own parent"
        }
    }
}

@Serializable
data class CategoryTreeDto(
    val id: CategoryId,
    val name: String,
    val slug: Slug,
    val description: String,
    val children: List<CategoryTreeDto>?,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)