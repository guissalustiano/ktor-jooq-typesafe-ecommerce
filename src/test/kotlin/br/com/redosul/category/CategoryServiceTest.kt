package br.com.redosul.category;

import br.com.redosul.testDatabaseDsl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CategoryServiceTest : FunSpec({
    val dsl = testDatabaseDsl()

    val categoryService = CategoryService(dsl)

    context("::findById") {
        test("should return a category") {
            val fake = CategoryFaker.category()
            categoryService.create(fake)

            val category = categoryService.findById(fake.id)

            category!!.id shouldBe fake.id
        }
    }
})