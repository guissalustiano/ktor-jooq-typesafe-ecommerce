package br.com.redosul

import br.com.redosul.category.CategoryService
import br.com.redosul.category.categoryRoutes
import br.com.redosul.order.OrderService
import br.com.redosul.order.orderRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.redosul.plugins.*
import br.com.redosul.product.ProductService
import br.com.redosul.product.productRoutes
import br.com.redosul.user.UserService
import br.com.redosul.user.userRoutes

fun main() {
    embeddedServer(Netty, port = 8080, watchPaths = listOf("classes"), host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    val dsl = configureDatabases()
    configureStatusResponse()
    configureRouting()
    configureCORS()
    configureOpenAPI()

    categoryRoutes(CategoryService(dsl))
    productRoutes(ProductService(dsl))
    orderRoutes(OrderService(dsl))
    userRoutes(UserService(dsl))
}
