package br.com.redosul.category

import br.com.redosul.faker
import br.com.redosul.past
import kotlinx.datetime.toKotlinInstant
import java.util.UUID
import java.util.concurrent.TimeUnit

object CategoryFaker {
    fun category() = CategoryDto(
        id = CategoryId(),
        parentId = null,
        name = faker.team().name(),
        description = faker.lorem().sentence(),
        createdAt = faker.date().past(),
        updatedAt = faker.date().past(),
    )

    fun categoryTree() = CategoryTreeDto(
        id = CategoryId(),
        name = faker.lorem().word(),
        description = faker.lorem().sentence(),
        createdAt = faker.date().past(),
        updatedAt = faker.date().past(),
        children = listOf(),
    )
}
