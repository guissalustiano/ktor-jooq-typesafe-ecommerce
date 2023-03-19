package br.com.redosul.category

import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.SlugId
import br.com.redosul.plugins.UUID
import br.com.redosul.plugins.toSlug
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CategoryId(override val value: UUID = UUID.randomUUID()): Id

@JvmInline
@Serializable
value class CategorySlug(override val value: Slug): SlugId

fun String.toCategorySlug() = CategorySlug(toSlug())

@Serializable
data class CategoryDto(
    val id: CategoryId = CategoryId(),
    val parentId: CategoryId? = null,
    val name: String,
    val slug: CategorySlug = name.toCategorySlug(),
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
    val slug: CategorySlug = name.toCategorySlug(),
    val description: String,
    val children: List<CategoryTreeDto>?,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

// Errors
sealed class CategoryError(message: String): Exception(message) {
    class NotFound(id: CategoryId): CategoryError("Category with id $id not found")
    class SlugAlreadyExists(slug: CategorySlug): CategoryError("Category with slug $slug already exists")
    class ParentNotFound(id: CategoryId): CategoryError("Parent category with id $id not found")
}