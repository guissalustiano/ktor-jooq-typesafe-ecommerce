package br.com.redosul.category;

import br.com.redosul.context
import br.com.redosul.plugins.Undefined
import br.com.redosul.plugins.get
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
            val fake = CategoryFaker.createPayload().let { categoryService.create(it) }

            val response = categoryService.findBySlug(fake.slug)

            response shouldNotBe null
            response?.id shouldBe fake.id
        }

        test("should return null when not found") {
            val response = categoryService.findBySlug("not-found".toCategorySlug())

            response shouldBe null
        }
    }

    context(CategoryService::findAll) {
        test("should a recursive tree of categories") {
            val fakeParent = CategoryFaker.createPayload().let { categoryService.create(it) }
            val fakeChild = CategoryFaker.createPayload().copy(parentId = fakeParent.id).let { categoryService.create(it) }

            val response = categoryService.findAll()

            val responseParent = response.find { it.id == fakeParent.id }
            responseParent shouldNotBe null
            responseParent?.children?.size shouldBe 1
            responseParent?.children?.get(0)?.id shouldBe fakeChild.id
        }
    }

    context(CategoryService::create) {
        test("should create a category") {
            val fakeParent = CategoryFaker.createPayload().let { categoryService.create(it) }
            val fakeChild = CategoryFaker.createPayload().copy(parentId = fakeParent.id)

            val response = categoryService.create(fakeChild)

            response.id shouldNotBe null
            categoryService.findBySlug(fakeChild.slug) shouldNotBe null
        }

        test("should throw an exception when parent not found on create") {
            val fake = CategoryFaker.createPayload().copy(parentId = CategoryId())

            shouldThrow<CategoryError.ParentNotFound> {
                categoryService.create(fake)
            }
        }

        test("should throw an exception when slug already exists  on create") {
            val otherFake = CategoryFaker.createPayload().let { categoryService.create(it) }
            val fake = CategoryFaker.createPayload().copy(slug = otherFake.slug)

            shouldThrow<CategoryError.SlugAlreadyExists> {
                categoryService.create(fake)
            }
        }
    }

    context(CategoryService::updateById) {
        test("should update a category") {
            val fakeParent = CategoryFaker.createPayload().let { categoryService.create(it) }
            val otherFakeParent = CategoryFaker.createPayload().let { categoryService.create(it) }
            val fake = CategoryFaker.createPayload().copy(parentId = fakeParent.id).let { categoryService.create(it) }

            val newName = CategoryFaker.response().name
            val update = CategoryUpdatePayload(
                parentId = Undefined.Defined(otherFakeParent.id),
                name = Undefined.Defined(newName),
                slug = Undefined.Defined(newName.toCategorySlug()),
            )
            val response = categoryService.updateById(fake.id, update)

            response?.id shouldBe fake.id
            response?.name shouldBe newName
            response?.slug shouldBe newName.toCategorySlug()
            response?.parentId shouldBe otherFakeParent.id
            categoryService.findBySlug(fake.slug) shouldBe null
            categoryService.findBySlug(update.slug.get()) shouldNotBe null
        }

        test("should throw an exception when parent not found on update") {
            val fake = CategoryFaker.createPayload().let { categoryService.create(it) }

            val update = CategoryUpdatePayload(
                parentId = Undefined.Defined(CategoryId())
            )

            shouldThrow<CategoryError.ParentNotFound> {
                categoryService.updateById(fake.id, update)
            }
        }

        test("should throw an exception when slug already exists on update") {
            val fake = CategoryFaker.createPayload().let { categoryService.create(it) }
            val otherFake = CategoryFaker.createPayload().let { categoryService.create(it) }

            val update = CategoryUpdatePayload(
                slug = Undefined.Defined(otherFake.slug)
            )

            shouldThrow<CategoryError.SlugAlreadyExists> {
                categoryService.updateById(fake.id, update)
            }
        }
    }

    context(CategoryService::deleteById) {
        test("should delete a category") {
            val fake = CategoryFaker.createPayload().let { categoryService.create(it) }

            categoryService.deleteById(fake.id)

            categoryService.findBySlug(fake.slug) shouldBe null
        }
    }
})