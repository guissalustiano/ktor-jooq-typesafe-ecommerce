package br.com.redosul.user;

import br.com.redosul.context
import br.com.redosul.plugins.Undefined
import br.com.redosul.plugins.get
import br.com.redosul.testDatabaseDsl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class UserServiceTest : FunSpec({
    val dsl = testDatabaseDsl()
    val userService = UserService(dsl)

    context(UserService::findBySlug) {
        test("should return a user when found") {
            val fake = UserFaker.createPayload().also { userService.create(it) }

            val response = userService.findBySlug(fake.slug)

            response shouldNotBe null
        }

        test("should return null when not found") {
            val response = userService.findBySlug("not-found".toUserSlug())

            response shouldBe null
        }
    }

    context(UserService::findAll) {
        test("should a list of users") {
            val fake = UserFaker.createPayload().also { userService.create(it) }

            val response = userService.findAll()

            response.find { fake.slug == it.slug } shouldNotBe null
        }
    }

    context(UserService::create) {
        test("should create a user") {
            val fake = UserFaker.createPayload()

            userService.create(fake)

            userService.findBySlug(fake.slug) shouldNotBe null
        }

        test("should throw an exception when slug already exists  on create") {
            val otherFake = UserFaker.createPayload().also { userService.create(it) }
            val fake = UserFaker.createPayload().copy(slug = otherFake.slug)

            shouldThrow<UserError.SlugAlreadyExists> {
                userService.create(fake)
            }
        }
    }

    context(UserService::updateBySlug) {
        test("should update a user all fields") {
            val fake = UserFaker.createPayload().also { userService.create(it) }

            val newFake = UserFaker.response()
            val update = UserUpdatePayload(
                name = Undefined.Defined(newFake.name),
                phone = Undefined.Defined(newFake.phone),
            )
            userService.updateBySlug(fake.slug, update)

            userService.findBySlug(fake.slug).run{
                this shouldNotBe null; this!!
                name shouldBe newFake.name
                phone shouldBe newFake.phone
            }
        }
        test("should update a user one field") {
            val fake = UserFaker.createPayload().also { userService.create(it) }

            val newName = UserFaker.response().name
            val update = UserUpdatePayload(
                name = Undefined.Defined(newName),
            )
            userService.updateBySlug(fake.slug, update)

            userService.findBySlug(fake.slug).run{
                this shouldNotBe null; this!!
                name shouldBe newName
                slug shouldBe fake.slug
            }
        }
    }

    context(UserService::createOrUpdate) {
        test("should create if not exists") {
            val fake = UserFaker.createPayload()

            userService.createOrUpdate(fake)

            userService.findBySlug(fake.slug) shouldNotBe null
        }

        test("should update if exists") {
            val fake = UserFaker.createPayload().also { userService.create(it) }
            val newName = UserFaker.response().name

            userService.createOrUpdate(fake.copy(name = newName))

            userService.findBySlug(fake.slug)?.name shouldBe newName
        }
    }


    context(UserService::deleteBySlug) {
        test("should delete a user") {
            val fake = UserFaker.createPayload().also { userService.create(it) }

            userService.deleteBySlug(fake.slug)

            userService.findBySlug(fake.slug) shouldBe null
        }
    }
})