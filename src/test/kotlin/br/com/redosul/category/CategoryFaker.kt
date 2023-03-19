package br.com.redosul.category

import br.com.redosul.faker
import br.com.redosul.past

object CategoryFaker {
    fun createPayload() = CategoryCreatePayload(
        name = faker.team().name(),
        description = faker.lorem().sentence(),
    )

    fun response() = CategoryResponse(
        id = CategoryId(),
        parentSlug = null,
        name = faker.team().name(),
        slug = faker.team().name().toCategorySlug(),
        description = faker.lorem().sentence(),
        createdAt = faker.date().past(),
        updatedAt = faker.date().past(),
    )

    fun treeResponse() = listOf(response()).toTreeResponse().first()
}
