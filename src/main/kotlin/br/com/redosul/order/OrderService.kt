package br.com.redosul.order

import org.jooq.DSLContext

class OrderService(private val dsl: DSLContext) {
    suspend fun findAll() : List<OrderDto> = TODO()

    suspend fun findById(id: OrderId) : OrderDto? = TODO()

    suspend fun create(payload: OrderCreateDto): OrderDto.WithoutPrice = TODO()

    suspend fun setPrice(id: OrderId, payload: OrderPriceDto): OrderDto.WithPrice? = TODO()

    suspend fun setApprove(id: OrderId): OrderDto.WithPrice? = TODO()

    suspend fun setProduce(id: OrderId): OrderDto.WithPrice? = TODO()

    suspend fun setDeliver(id: OrderId): OrderDto.WithPrice? = TODO()

    suspend fun setBill(id: OrderId): OrderDto.WithPrice? = TODO()

    suspend fun setFinished(id: OrderId): OrderDto.WithPrice? = TODO()

    suspend fun setCancel(id: OrderId): OrderDto? = TODO()

    suspend fun deleteById(id: OrderId): OrderDto? = TODO()

    suspend fun addItem(id: OrderId, payload: OrderItemCreateDto): OrderItemDto = TODO()

    suspend fun removeItem(id: OrderItemId): OrderItemDto? = TODO()
}