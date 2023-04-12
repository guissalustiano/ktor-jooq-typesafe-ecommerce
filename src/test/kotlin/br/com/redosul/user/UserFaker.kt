package br.com.redosul.user

import br.com.redosul.faker
import br.com.redosul.fullPhoneWithoutPunctuation
import br.com.redosul.plugins.Email
import br.com.redosul.plugins.Name
import br.com.redosul.plugins.Phone
import br.com.redosul.plugins.toSlug
import kotlinx.datetime.Clock

object UserFaker {
    object Default {
        val createPayload = UserCreatePayload(
            name = Name("Maria", "Test da Silva"),
            email = Email("maria_silva@test.com"),
            phone = Phone("+5511999999999"),
        )


        val response = UserResponse(
            slug = "maria-test-da-silva".toUserSlug(),
            name = Name("Maria", "Test da Silva"),
            email = Email("maria_silva@test.com"),
            phone = Phone("+5511999999999"),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
    }

    fun createPayload() = UserCreatePayload(
        name = faker.name().let {  Name(it.firstName(), it.lastName()) },
        email = Email(faker.internet().emailAddress()),
        phone = Phone(faker.phoneNumber().fullPhoneWithoutPunctuation()),
    )

    fun response() = Default.response
}
