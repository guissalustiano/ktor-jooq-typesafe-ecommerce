package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.enums.ClotheSize
import br.com.redosul.generated.tables.pojos.Product
import br.com.redosul.generated.tables.pojos.ProductVariant
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.UUID
import br.com.redosul.plugins.toSlug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JvmInline
@Serializable
value class ProductId(override val value: UUID = UUID.randomUUID()): Id

@JvmInline
@Serializable
value class ProductVariantId(override val value: UUID = UUID.randomUUID()): Id

@Serializable
data class ProductDto(
    val id: ProductId = ProductId(),
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = "",
    val variants: List<ProductVariantDto>? = null
) {
    init {
        variants?.isSameType()?.let { require(it) { "Variants must be the same type" } }
    }
}

@Serializable
sealed class ProductVariantDto {
    abstract val id: ProductVariantId
    @Serializable
    sealed class Color: ProductVariantDto() {
        abstract val name: String
            @Serializable
            @SerialName("RGB")
            data class RGB(
                override val id: ProductVariantId = ProductVariantId(),
                override val name: String = "",
                val red: UByte,
                val green: UByte,
                val blue: UByte,
            ): Color()

            @Serializable
            @SerialName("Image")
            data class Image(
                override val id: ProductVariantId = ProductVariantId(),
                override val name: String = "",
                val url: String,
            ): Color()
        }

    @Serializable
    @SerialName("Size")
    data class Size(
        override val id: ProductVariantId = ProductVariantId(),
        val value: ClotheSize,
    ): ProductVariantDto()

    @Serializable
    @SerialName("ColorSize")
    data class ColorSize(
        override val id: ProductVariantId = ProductVariantId(),
        val size: Size,
        val color: Color,
    ): ProductVariantDto()
}

private fun List<ProductVariantDto>.isSameType(): Boolean {
    val first = firstOrNull() ?: return true
    return when(first) {
        is ProductVariantDto.Size -> all { it is ProductVariantDto.Size }
        is ProductVariantDto.ColorSize -> all { it is ProductVariantDto.ColorSize }
        is ProductVariantDto.Color -> all { it is ProductVariantDto.Color }
    }
}