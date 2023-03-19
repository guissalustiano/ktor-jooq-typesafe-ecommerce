package br.com.redosul.category;

import br.com.redosul.context
import br.com.redosul.testDatabaseDsl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CategoryServiceTest : FunSpec({
    val dsl = testDatabaseDsl()
    val categoryService = CategoryService(dsl)

    context(CategoryService::findById) {
        test("should return a category when found") {
            val fake = CategoryFaker.category()
            categoryService.create(fake)

            val response = categoryService.findBySlug(fake.slug)

            response!!.id shouldBe fake.id
        }

        test("should return null when not found") {
            val response = categoryService.findBySlug("not-found".toCategorySlug())

            response shouldBe null
        }
    }

    context(CategoryService::findAll) {
        test("should a recursive tree of categories") {
            val fakeParent = CategoryFaker.category()
            val fakeChild = CategoryFaker.category().copy(parentId = fakeParent.id)

            categoryService.create(fakeParent)
            categoryService.create(fakeChild)

            val response = categoryService.findAll()

            val responseParent = response.find { it.id == fakeParent.id }
            responseParent shouldNotBe null
            responseParent?.children?.size shouldBe 1
            responseParent?.children?.get(0)?.id shouldBe fakeChild.id
        }
    }

    context(CategoryService::create) {
        test("should create a category") {
            val fakeParent = CategoryFaker.category()
            val fakeChild = CategoryFaker.category().copy(parentId = fakeParent.id)

            categoryService.create(fakeParent)
            val response = categoryService.create(fakeChild)

            response.id shouldNotBe null
            categoryService.findBySlug(fakeChild.slug) shouldNotBe null
        }

        test("should throw an exception when parent not found on create") {
            val fake = CategoryFaker.category().copy(parentId = CategoryId())

            shouldThrow<CategoryError.ParentNotFound> {
                categoryService.create(fake)
            }
        }

        test("should throw an exception when slug already exists  on create") {
            val fake = CategoryFaker.category()

            categoryService.create(fake)

            shouldThrow<CategoryError.SlugAlreadyExists> {
                categoryService.create(fake)
            }
        }
    }

    context(CategoryService::updateById) {
        test("should update a category") {
            val fakeParent = CategoryFaker.category()
            val otherFakeParent = CategoryFaker.category()
            val fake = CategoryFaker.category().copy(parentId = fakeParent.id)
            categoryService.create(fakeParent)
            categoryService.create(otherFakeParent)
            categoryService.create(fake)

            val newName = CategoryFaker.category().name
            val update = fake.copy(name = newName, slug = newName.toCategorySlug(), parentId = otherFakeParent.id)
            val response = categoryService.updateById(fake.id, update)

            response?.id shouldBe fake.id
            response?.name shouldBe newName
            response?.slug shouldBe newName.toCategorySlug()
            response?.parentId shouldBe otherFakeParent.id
            categoryService.findBySlug(fake.slug) shouldBe null
            categoryService.findBySlug(update.slug) shouldNotBe null
        }

        test("should throw an exception when parent not found on update") {
            val fake = CategoryFaker.category()
            categoryService.create(fake)

            val update = fake.copy(parentId = CategoryId())

            shouldThrow<CategoryError.ParentNotFound> {
                categoryService.updateById(fake.id, update)
            }
        }

        test("should throw an exception when slug already exists on update") {
            val fake = CategoryFaker.category()
            val otherFake = CategoryFaker.category()
            categoryService.create(fake)
            categoryService.create(otherFake)

            val update = fake.copy(slug = otherFake.slug)

            shouldThrow<CategoryError.SlugAlreadyExists> {
                categoryService.updateById(fake.id, update)
            }
        }
    }

    context(CategoryService::deleteById) {
        test("should delete a category") {
            val fake = CategoryFaker.category()
            categoryService.create(fake)

            categoryService.deleteById(fake.id)

            categoryService.findBySlug(fake.slug) shouldBe null
        }
    }
})