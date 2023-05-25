package br.com.redosul.product

import br.com.redosul.category.CategorySlug
import br.com.redosul.file.ImageCreatePayload
import br.com.redosul.file.ImageResponse
import br.com.redosul.file.MimeType
import br.com.redosul.generated.enums.ClotheSize
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.SlugId
import br.com.redosul.plugins.URL
import br.com.redosul.plugins.Undefined
import br.com.redosul.plugins.toSlug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@JvmInline
@Serializable
value class ProductSlug(override val value: Slug): SlugId
fun String.toProductSlug() = ProductSlug(toSlug())

@JvmInline
@Serializable
value class ProductVariantSlug(override val value: Slug): SlugId
fun String.toProductVariantSlug() = ProductVariantSlug(toSlug())


@JvmInline
@Serializable
value class ProductImageSlug(override val value: Slug): SlugId
fun String.toProductImageSlug() = ProductImageSlug(toSlug())

@Serializable
data class ProductCreatePayload(
    val categorySlug: CategorySlug,
    val name: String,
    val slug: ProductSlug = name.toProductSlug(),
    val description: String = "",
)

@Serializable
data class ProductUpdatePayload(
    val categorySlug: Undefined<CategorySlug> = Undefined.None,
    val name: Undefined<String> = Undefined.None,
    val slug: Undefined<ProductSlug> = Undefined.None,
    val description: Undefined<String> = Undefined.None,
)

@Serializable
data class ProductResponse(
    val categorySlug: CategorySlug,
    val name: String,
    val slug: ProductSlug = name.toProductSlug(),
    val description: String = "",
    val variants: List<ProductVariantResponse>? = null,
    val images: List<ProductImageResponse> = emptyList(),
) {
    init {
        variants?.isSameType()?.let { require(it) { "Variants must be the same type" } }
    }
}

@Serializable
sealed class ProductVariantCreatePayload {
    abstract val productSlug: ProductSlug
    abstract val slug: ProductImageSlug
    abstract val images: List<ProductImageCreatePayload>

    @Serializable
    sealed class Color: ProductVariantCreatePayload() {
        abstract val name: String
            @Serializable
            @SerialName("RGB")
            data class RGB(
                override val productSlug: ProductSlug,
                override val slug: ProductImageSlug,
                override val name: String = "",
                val red: UByte,
                val green: UByte,
                val blue: UByte,
                override val images: List<ProductImageCreatePayload> = emptyList(),
            ): Color()

            @Serializable
            @SerialName("Image")
            data class Image(
                override val productSlug: ProductSlug,
                override val slug: ProductImageSlug,
                override val name: String = "",
                val url: String,
                override val images: List<ProductImageCreatePayload> = emptyList(),
            ): Color()
        }

    @Serializable
    @SerialName("Size")
    data class Size(
        override val productSlug: ProductSlug,
        override val slug: ProductImageSlug,
        val value: ClotheSize,
        override val images: List<ProductImageCreatePayload> = emptyList(),
    ): ProductVariantCreatePayload()

    @Serializable
    @SerialName("ColorSize")
    data class ColorSize(
        override val productSlug: ProductSlug,
        override val slug: ProductImageSlug,
        val size: Size,
        val color: Color,
        override val images: List<ProductImageCreatePayload> = emptyList(),
    ): ProductVariantCreatePayload()
}

@Serializable
sealed class ProductVariantResponse {
    abstract val slug: ProductImageSlug
    abstract val images: List<ProductImageResponse>

    @Serializable
    sealed class Color: ProductVariantResponse() {
        abstract val name: String
            @Serializable
            @SerialName("RGB")
            data class RGB(
                override val slug: ProductImageSlug,
                override val name: String = "",
                val red: UByte,
                val green: UByte,
                val blue: UByte,
                override val images: List<ProductImageResponse> = emptyList(),
            ): Color()

            @Serializable
            @SerialName("Image")
            data class Image(
                override val slug: ProductImageSlug,
                override val name: String = "",
                val url: String,
                override val images: List<ProductImageResponse> = emptyList(),
            ): Color()
        }

    @Serializable
    @SerialName("Size")
    data class Size(
        override val slug: ProductImageSlug,
        val value: ClotheSize,
        override val images: List<ProductImageResponse> = emptyList(),
    ): ProductVariantResponse()

    @Serializable
    @SerialName("ColorSize")
    data class ColorSize(
        override val slug: ProductImageSlug,
        val size: Size,
        val color: Color,
        override val images: List<ProductImageResponse> = emptyList(),
    ): ProductVariantResponse()
}

@Serializable
data class ProductImageCreatePayload(
    val productSlug: ProductSlug,
    override val slug: ProductImageSlug,
    override val mimeType: MimeType,
    override val alt: String = "",
): ImageCreatePayload

@Serializable
data class ProductImageResponse(
    override val url: URL,
    override val alt: String = "",
): ImageResponse {
    val slug get() = url.lastPath.toProductImageSlug()
}

private fun List<ProductVariantResponse>.isSameType(): Boolean {
    val first = firstOrNull() ?: return true
    return when(first) {
        is ProductVariantResponse.Size -> all { it is ProductVariantResponse.Size }
        is ProductVariantResponse.ColorSize -> all { it is ProductVariantResponse.ColorSize }
        is ProductVariantResponse.Color -> all { it is ProductVariantResponse.Color }
    }
}