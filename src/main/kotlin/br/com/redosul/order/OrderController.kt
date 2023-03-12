package br.com.redosul.order

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.*
import org.jooq.*
import org.jooq.impl.*

/*
GET -> List all
POST -> Create
 */
@Resource("/orders")
class OrderResource {
    /*
    GET -> Find by id
    POST -> Update
    DELETE /orders/{id} -> Delete
     */
    @Resource("{id}")
    class Id(val parent: OrderResource, val id: OrderId) {
        /*
        POST /orders/{id}/items -> Add item
        DELETE /orders/{id}/items/{itemId} -> Remove item
         */
        @Resource("items")
        class Item(val parent: Id) {
            /*
            DELETE /orders/{id}/items/{itemId} -> Remove item
             */
            @Resource("{itemId}")
            class ItemId(val parent: Item, val itemId: OrderItemId)
        }

        @Resource("budget")
        class Budget(val parent: Id)

        @Resource("approve")
        class Approve(val parent: Id)

        @Resource("produce")
        class Produce(val parent: Id)

        @Resource("deliver")
        class Deliver(val parent: Id)

        @Resource("bill")
        class Bill(val parent: Id)

        @Resource("finish")
        class Finished(val parent: Id)

        @Resource("cancel")
        class Cancel(val parent: Id)
    }
}

fun Application.orderRoutes(service: OrderService) {
    routing {
        get<OrderResource> { _ ->
            service.findAll().let{
                call.respond(it)
            }
        }

        get<OrderResource.Id> { resource ->
            service.findById(resource.id)?.let{
                call.respond(it)
            }
        }

        post<OrderResource> { _ ->
            val payload = call.receive<OrderCreateDto>()
            service.create(payload).let{
                call.response.status(HttpStatusCode.Created)
                call.respond(it)
            }
        }

        post<OrderResource.Id.Item> { resource ->
            val payload = call.receive<OrderItemCreateDto>()
            service.addItem(resource.parent.id, payload)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Budget> { resource ->
            val payload = call.receive<OrderPriceDto>()
            service.setPrice(resource.parent.id, payload)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Approve> { resource ->
            service.setApprove(resource.parent.id)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Produce> { resource ->
            service.setProduce(resource.parent.id)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Deliver> { resource ->
            service.setDeliver(resource.parent.id)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Bill> { resource ->
            service.setBill(resource.parent.id)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Finished> { resource ->
            service.setFinished(resource.parent.id)?.let {
                call.respond(it)
            }
        }

        post<OrderResource.Id.Cancel> { resource ->
            service.setCancel(resource.parent.id)?.let {
                call.respond(it)
            }
        }

        delete<OrderResource.Id> { resource ->
            service.deleteById(resource.id)?.let {
                call.respond(it)
            }
        }

        delete<OrderResource.Id.Item.ItemId> { resource ->
            service.removeItem(resource.itemId)?.let {
                call.respond(it)
            }
        }
    }
}


