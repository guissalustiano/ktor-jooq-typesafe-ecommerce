package br.com.redosul.category

import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.SlugId
import br.com.redosul.plugins.UUID
import br.com.redosul.plugins.Undefined
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
data class CategoryCreatePayload(
    val parentId: CategoryId? = null,
    val name: String,
    val slug: CategorySlug = name.toCategorySlug(),
    val description: String = "",
)

@Serializable
data class CategoryUpdatePayload(
    val parentId: Undefined<CategoryId?> = Undefined.None,
    val name: Undefined<String> = Undefined.None,
    val slug: Undefined<CategorySlug> = Undefined.None,
    val description: Undefined<String> = Undefined.None,
)

@Serializable
data class CategoryResponse(
    val id: CategoryId = CategoryId(),
    val parentId: CategoryId?,
    val name: String,
    val slug: CategorySlug,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        if (id == parentId) {
            throw CategoryError.CyclicReference(id)
        }
    }
}

@Serializable
data class CategoryTreeResponse(
    val id: CategoryId,
    val name: String,
    val slug: CategorySlug,
    val description: String,
    val children: List<CategoryTreeResponse>?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

// Errors
sealed class CategoryError(message: String): Exception(message) {
    class CyclicReference(id: CategoryId): CategoryError("Category with id $id cannot be its own parent")
    class SlugAlreadyExists(slug: CategorySlug): CategoryError("Category with slug $slug already exists")
    class ParentNotFound(id: CategoryId): CategoryError("Parent category with id $id not found")
}