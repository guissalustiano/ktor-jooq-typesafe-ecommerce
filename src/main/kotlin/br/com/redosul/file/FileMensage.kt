package br.com.redosul.file

import br.com.redosul.plugins.SlugId
import br.com.redosul.plugins.URL
import br.com.redosul.product.ProductImageSlug
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable

typealias ImageSlug = SlugId
typealias MimeType = String// ContentType

interface ImageCreatePayload {
    val slug: ImageSlug
    val mimeType: MimeType
    val alt: String
}

@Serializable
data class ImageUploadPayload(
    /*
     * Signed URL
     */
    val putUrl: URL,
    val url: URL,
)

interface ImageResponse{
    val url: URL
    val alt: String
}