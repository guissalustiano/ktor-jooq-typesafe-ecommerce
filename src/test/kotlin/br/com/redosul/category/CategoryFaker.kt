package br.com.redosul.category

import br.com.redosul.faker
import br.com.redosul.past
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object CategoryFaker {
    object Default {
        val createPayload = CategoryCreatePayload(
            name = "Test",
            description = "Long description for test",
        )

        val response = CategoryResponse(
            parentSlug = null,
            name = "Test",
            slug = "test".toCategorySlug(),
            description = "Long description for test",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
    }

    fun createPayload() = CategoryCreatePayload(
        name = faker.team().name(),
        description = faker.lorem().sentence(),
    )

    fun response() = CategoryResponse(
        parentSlug = null,
        name = faker.team().name(),
        slug = faker.team().name().toCategorySlug(),
        description = faker.lorem().sentence(),
        createdAt = faker.date().past(),
        updatedAt = faker.date().past(),
    )

    fun treeResponse() = listOf(response()).toTreeResponse().first()
}
