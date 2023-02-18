package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.toSlug
import kotlinx.serialization.Serializable


@JvmInline
@Serializable
value class ProductId(override val value: Long): Id

@Serializable
data class ProductSetPayload(
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = ""
)

@Serializable
data class ProductResponse(
    val id: ProductId,
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String
)

fun ProductSetPayload.toRecord() = ProductRecord().also {
    it.categoryId = categoryId.value
    it.name = name
    it.slug = slug.value
    it.description = description
}

fun ProductRecord.toResponse() = ProductResponse(
    ProductId(id!!),
    CategoryId(categoryId!!),
    name!!,
    Slug(slug!!),
    description!!
)