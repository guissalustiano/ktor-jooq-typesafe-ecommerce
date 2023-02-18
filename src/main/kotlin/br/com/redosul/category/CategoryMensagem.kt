package br.com.redosul.category

import br.com.redosul.generated.tables.pojos.Category
import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.toSlug
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CategoryId(override val value: Long): Id

@Serializable
data class CategorySetPayload(
    val parentId: CategoryId? = null,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = ""
)


@Serializable
data class CategoryResponse(
    val id: CategoryId,
    val parentId: CategoryId?,
    val name: String,
    val slug: Slug,
    val description: String)

@Serializable
data class CategoryTreeResponse(
    val id: CategoryId,
    val name: String,
    val slug: Slug,
    val description: String,
    val children: List<CategoryTreeResponse>?,
)

fun CategorySetPayload.toRecord() = CategoryRecord().also {
    it.parentId = parentId?.value
    it.name = name
    it.slug = slug.value
    it.description = description
}

fun Category.toResponse() = CategoryResponse(
    CategoryId(id!!),
    parentId?.let { CategoryId(it) },
    name!!,
    Slug(slug!!),
    description!!
)

private fun getChildren(parentId: CategoryId?, plain: List<CategoryResponse>): List<CategoryTreeResponse> {
    return plain.filter {
        it.parentId == parentId
    }.map {
        CategoryTreeResponse(
            it.id,
            it.name,
            it.slug,
            it.description,
            getChildren(it.id, plain).ifEmpty { null }
        )
    }
}

fun Iterable<Category>.toTreeResponse() = getChildren(null, this.map { it.toResponse() })