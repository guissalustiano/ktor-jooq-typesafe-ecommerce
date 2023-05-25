package br.com.redosul.order

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.enums.OrderStatus
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.UUID
import br.com.redosul.product.ProductImageResponse
import br.com.redosul.product.ProductSlug
import br.com.redosul.product.ProductVariantResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@JvmInline
@Serializable
value class OrderId(override val value: UUID = UUID.randomUUID()): Id

@Serializable
data class OrderCreateDto(
    val id: OrderId = OrderId(),
    val orders: List<OrderItemCreateDto> = emptyList(),
)

@Serializable
sealed class OrderDto{
    abstract val id: OrderId
    abstract val orders: List<OrderItemDto>

    @Serializable
    data class WithoutPrice(
        override val id: OrderId,
        override val orders: List<OrderItemDto.WithoutPrice>,
        val status: OrderStatus,
    ): OrderDto()

    @Serializable
    data class WithPrice(
        override val id: OrderId,
        override val orders: List<OrderItemDto.WithPrice>,
        val status: OrderStatus,
    ): OrderDto()
}

sealed class OrderItemDto{
    abstract val id: OrderItemId
    abstract val product: ProductOneVariantDto
    abstract val quantity: Int

    @Serializable
    data class WithoutPrice(
        override val id: OrderItemId,
        override val product: ProductOneVariantDto,
        override val quantity: Int,
    ): OrderItemDto()

    @Serializable
    data class WithPrice(
        override val id: OrderItemId,
        override val product: ProductOneVariantDto,
        override val quantity: Int,
        val price: Double,
    ): OrderItemDto()
}

@JvmInline
@Serializable
value class OrderItemId(override val value: UUID = UUID.randomUUID()): Id

@Serializable
data class OrderItemCreateDto(
    val productSlug: ProductSlug,
    val quantity: Int,
)

@Serializable
data class OrderPriceItemDto(
    val productSlug: ProductSlug,
    val price: Double,
)

@Serializable(with = OrderPriceDtoSerializer::class)
class OrderPriceDto(
    val m: Map<OrderItemId, OrderPriceItemDto>
): Map<OrderItemId, OrderPriceItemDto> by m

object OrderPriceDtoSerializer: KSerializer<OrderPriceDto> {
    private val delegateSerializer = MapSerializer(OrderItemId.serializer(), OrderPriceItemDto.serializer())
    override val descriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: OrderPriceDto) {
        encoder.encodeSerializableValue(delegateSerializer, value.m)
    }

    override fun deserialize(decoder: Decoder): OrderPriceDto {
        val values = decoder.decodeSerializableValue(delegateSerializer)
        return OrderPriceDto(values)
    }
}

@Serializable
data class ProductOneVariantDto(
    val categoryId: CategoryId,
    val name: String,
    val slug: ProductSlug,
    val description: String,
    val variants: ProductVariantResponse,
    val images: List<ProductImageResponse> = emptyList(),
)