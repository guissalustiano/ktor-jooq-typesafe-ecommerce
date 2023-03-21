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

    context(CategoryService::findBySlug) {
        test("should return a category when found") {
            val fake = CategoryFaker.createPayload().also { categoryService.create(it) }

            val response = categoryService.findBySlug(fake.slug)

            response shouldNotBe null
        }

        test("should return null when not found") {
            val response = categoryService.findBySlug("not-found".toCategorySlug())

            response shouldBe null
        }
    }

    context(CategoryService::findAll) {
        test("should a recursive tree of categories") {
            val fakeParent = CategoryFaker.createPayload().also { categoryService.create(it) }
            val fakeChild = CategoryFaker.createPayload().copy(parentSlug = fakeParent.slug).also { categoryService.create(it) }

            val response = categoryService.findAll()

            val responseParent = response.find { it.slug == fakeParent.slug }
            responseParent shouldNotBe null
            responseParent?.children?.size shouldBe 1
            responseParent?.children?.get(0)?.slug shouldBe fakeChild.slug
        }
    }

    context(CategoryService::create) {
        test("should create a category") {
            val fakeParent = CategoryFaker.createPayload().also { categoryService.create(it) }
            val fakeChild = CategoryFaker.createPayload().copy(parentSlug = fakeParent.slug)

            categoryService.create(fakeChild)

            categoryService.findBySlug(fakeChild.slug) shouldNotBe null
        }

        test("should throw an exception when parent not found on create") {
            val fake = CategoryFaker.createPayload().copy(parentSlug = "not-found".toCategorySlug())

            shouldThrow<CategoryError.ParentNotFound> {
                categoryService.create(fake)
            }
        }

        test("should throw an exception when slug already exists  on create") {
            val otherFake = CategoryFaker.createPayload().also { categoryService.create(it) }
            val fake = CategoryFaker.createPayload().copy(slug = otherFake.slug)

            shouldThrow<CategoryError.SlugAlreadyExists> {
                categoryService.create(fake)
            }
        }
    }

    context(CategoryService::updateBySlug) {
        test("should update a category") {
            val fakeParent = CategoryFaker.createPayload().also { categoryService.create(it) }
            val otherFakeParent = CategoryFaker.createPayload().also { categoryService.create(it) }
            val fake = CategoryFaker.createPayload().copy(parentSlug = fakeParent.slug).also { categoryService.create(it) }

            val newName = CategoryFaker.response().name
            val update = CategoryUpdatePayload(
                parentSlug = Undefined.Defined(otherFakeParent.slug),
                name = Undefined.Defined(newName),
                slug = Undefined.Defined(newName.toCategorySlug()),
            )
            categoryService.updateBySlug(fake.slug, update)

            categoryService.findBySlug(fake.slug) shouldBe null
            categoryService.findBySlug(update.slug.get()).run{
                this shouldNotBe null; this!!
                name shouldBe newName
                slug shouldBe newName.toCategorySlug()
                parentSlug shouldBe otherFakeParent.slug
            }
        }

        test("should throw an exception when parent not found on update") {
            val fake = CategoryFaker.createPayload().also { categoryService.create(it) }
            val newSlug = CategoryFaker.createPayload().slug

            val update = CategoryUpdatePayload(
                parentSlug = Undefined.Defined(newSlug)
            )

            shouldThrow<CategoryError.ParentNotFound> {
                categoryService.updateBySlug(fake.slug, update)
            }
        }

        test("should throw an exception when slug already exists on update") {
            val fake = CategoryFaker.createPayload().also { categoryService.create(it) }
            val otherFake = CategoryFaker.createPayload().also { categoryService.create(it) }

            val update = CategoryUpdatePayload(
                slug = Undefined.Defined(otherFake.slug)
            )

            shouldThrow<CategoryError.SlugAlreadyExists> {
                categoryService.updateBySlug(fake.slug, update)
            }
        }
    }

    context(CategoryService::createOrUpdate) {
        test("should create if not exists") {
            val fake = CategoryFaker.createPayload()

            categoryService.createOrUpdate(fake)

            categoryService.findBySlug(fake.slug) shouldNotBe null
        }

        test("should update if exists") {
            val fake = CategoryFaker.createPayload().also { categoryService.create(it) }
            val newName = CategoryFaker.response().name

            categoryService.createOrUpdate(fake.copy(name = newName))

            categoryService.findBySlug(fake.slug)?.name shouldBe newName
        }
    }

    context(CategoryService::deleteBySlug) {
        test("should delete a category") {
            val fake = CategoryFaker.createPayload().also { categoryService.create(it) }

            categoryService.deleteBySlug(fake.slug)

            categoryService.findBySlug(fake.slug) shouldBe null
        }
    }
})