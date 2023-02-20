package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.enums.ClotheSize
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.URL
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


@JvmInline
@Serializable
value class ProductImageId(override val value: UUID = UUID.randomUUID()): Id

@Serializable
data class ProductDto(
    val id: ProductId = ProductId(),
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = "",
    val variants: List<ProductVariantDto>? = null,
    val images: List<ProductImageDto> = emptyList(),
) {
    init {
        variants?.isSameType()?.let { require(it) { "Variants must be the same type" } }
    }
}

@Serializable
sealed class ProductVariantDto {
    abstract val id: ProductVariantId
    abstract val images: List<ProductImageDto>

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
                override val images: List<ProductImageDto> = emptyList(),
            ): Color()

            @Serializable
            @SerialName("Image")
            data class Image(
                override val id: ProductVariantId = ProductVariantId(),
                override val name: String = "",
                val url: String,
                override val images: List<ProductImageDto> = emptyList(),
            ): Color()
        }

    @Serializable
    @SerialName("Size")
    data class Size(
        override val id: ProductVariantId = ProductVariantId(),
        val value: ClotheSize,
        override val images: List<ProductImageDto> = emptyList(),
    ): ProductVariantDto()

    @Serializable
    @SerialName("ColorSize")
    data class ColorSize(
        override val id: ProductVariantId = ProductVariantId(),
        val size: Size,
        val color: Color,
        override val images: List<ProductImageDto> = emptyList(),
    ): ProductVariantDto()
}

@Serializable
data class ProductImageDto(
    val id: ProductImageId = ProductImageId(),
    val url: URL,
    val alt: String = "",
)

private fun List<ProductVariantDto>.isSameType(): Boolean {
    val first = firstOrNull() ?: return true
    return when(first) {
        is ProductVariantDto.Size -> all { it is ProductVariantDto.Size }
        is ProductVariantDto.ColorSize -> all { it is ProductVariantDto.ColorSize }
        is ProductVariantDto.Color -> all { it is ProductVariantDto.Color }
    }
}