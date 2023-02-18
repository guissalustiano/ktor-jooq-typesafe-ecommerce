package br.com.redosul

import br.com.redosul.category.CategoryService
import br.com.redosul.category.categoryRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import br.com.redosul.plugins.*
import br.com.redosul.product.ProductService
import br.com.redosul.product.product

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    val dsl = configureDatabases()
    configureRouting()

    product(ProductService(dsl))
    categoryRoutes(CategoryService(dsl))
}
