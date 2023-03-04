package br.com.redosul

import br.com.redosul.order.OrderService
import br.com.redosul.order.categoryRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.redosul.plugins.*
import br.com.redosul.product.ProductService
import br.com.redosul.product.product
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

    product(ProductService(dsl))
    categoryRoutes(OrderService(dsl))
    userRoutes(UserService(dsl))
}
