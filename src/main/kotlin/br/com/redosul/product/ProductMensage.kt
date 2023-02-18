package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.enums.ClotheSize
import br.com.redosul.generated.tables.pojos.Product
import br.com.redosul.generated.tables.pojos.ProductVariant
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.toSlug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JvmInline
@Serializable
value class ProductId(override val value: Long): Id

@JvmInline
@Serializable
value class ProductVariantId(override val value: Long): Id

@Serializable
data class ProductSetPayload(
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String = "",
    val variants: List<ProductVariantSetPayload>? = null
) {
    init {
        variants?.isSameType()?.let { require(it) { "Variants must be the same type" } }
    }
}

fun List<ProductVariantSetPayload>.isSameType(): Boolean {
    return when(get(0)) {
        is ProductVariantSetPayload.Size -> all { it is ProductVariantSetPayload.Size }
        is ProductVariantSetPayload.ColorSize -> all { it is ProductVariantSetPayload.ColorSize }
        is ProductVariantSetPayload.Color -> all { it is ProductVariantSetPayload.Color }
    }
}

@Serializable
sealed class ProductVariantSetPayload {
    @Serializable
    sealed class Color: ProductVariantSetPayload() {
        abstract val name: String
            @Serializable
            @SerialName("RGB")
            data class RGB(
                override val name: String = "",
                val red: UByte,
                val green: UByte,
                val blue: UByte,
            ): Color()

            @Serializable
            @SerialName("Image")
            data class Image(
                override val name: String = "",
                val url: String,
            ): Color()
        }

    @Serializable
    @SerialName("Size")
    data class Size(val value: ClotheSize): ProductVariantSetPayload()

    @Serializable
    @SerialName("ColorSize")
    data class ColorSize(val size: Size, val color: Color): ProductVariantSetPayload()
}


@Serializable
data class ProductResponse(
    val id: ProductId,
    val categoryId: CategoryId,
    val name: String,
    val slug: Slug = name.toSlug(),
    val description: String,
    val variants: List<ProductVariantResponse>?
)

@Serializable
sealed class ProductVariantResponse {
    abstract val id: ProductVariantId
    @Serializable
    sealed class Color: ProductVariantResponse() {
        abstract val name: String
        @Serializable
        @SerialName("RGB")
        data class RGB(
            override val id: ProductVariantId,
            override val name: String = "",
            val red: UByte,
            val green: UByte,
            val blue: UByte,
        ): Color()

        @Serializable
        @SerialName("Image")
        data class Image(
            override val id: ProductVariantId,
            override val name: String = "",
            val url: String,
        ): Color()
    }

    @Serializable
    @SerialName("Size")
    data class Size(
        override val id: ProductVariantId,
        val value: ClotheSize,
    ): ProductVariantResponse()

    @Serializable
    @SerialName("ColorSize")
    data class ColorSize(
        override val id: ProductVariantId,
        val size: Size,
        val color: Color
    ): ProductVariantResponse()
}

fun Product.toResponse() = Pair(this, emptyList<ProductVariant>()).toResponse()

fun Pair<Product, List<ProductVariant>?>.toResponse(): ProductResponse {
    val (product, variants) = this
    return ProductResponse(
        ProductId(product.id!!),
        CategoryId(product.categoryId!!),
        product.name!!,
        Slug(product.slug!!),
        product.description!!,
        variants?.map { it.toResponse() }
    )
}

private fun ProductVariant.toResponse(): ProductVariantResponse {
    requireNotNull(id)
    fun parseOrNullSize(): ProductVariantResponse.Size? {
        size ?: return null

        return ProductVariantResponse.Size(
            ProductVariantId(id),
            size
        )
    }

    fun parseOrNullColorImage(): ProductVariantResponse.Color.Image? {
        colorName ?: return null
        colorUrl ?: return null

        return ProductVariantResponse.Color.Image(
            ProductVariantId(id),
            colorName,
            colorUrl
        )
    }

    fun parseOrNullColorRGB(): ProductVariantResponse.Color.RGB? {
        colorName ?: return null
        colorRed ?: return null
        colorGreen ?: return null
        colorBlue ?: return null

        return ProductVariantResponse.Color.RGB(
            ProductVariantId(id),
            colorName,
            colorRed.toUByte(),
            colorGreen.toUByte(),
            colorBlue.toUByte(),
        )
    }

    fun parseOrNullColor(): ProductVariantResponse.Color? {
        return parseOrNullColorImage() ?: parseOrNullColorRGB()
    }

    val color = parseOrNullColor()
    val size = parseOrNullSize()

    return when {
        color != null && size != null -> ProductVariantResponse.ColorSize(
            ProductVariantId(id),
            size,
            color
        )
        color != null -> color
        size != null -> size
        else -> error("Dirty ${ProductVariant::class} with id $id")
    }
}
