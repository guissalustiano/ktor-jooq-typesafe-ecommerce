package br.com.redosul.category

import br.com.redosul.generated.tables.pojos.Category
import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.await
import br.com.redosul.plugins.awaitFirstInto
import br.com.redosul.plugins.awaitFirstOrNullInto
import br.com.redosul.plugins.toKotlinInstant
import org.jooq.DSLContext
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime
import kotlinx.datetime.toKotlinInstant

class CategoryService(private val dsl: DSLContext) {
    suspend fun findAll() : List<CategoryTreeDto> = dsl.selectFrom(CATEGORY).await(Category::class).toTreeResponse()

    suspend fun findById(id: CategoryId) : CategoryDto? = dsl.selectFrom(CATEGORY)
        .where(CATEGORY.ID.eq(id.value))
        .awaitFirstOrNullInto(CATEGORY, Category::class)?.toDto()

    suspend fun create(payload: CategoryDto): CategoryDto {
        val record = payload.toRecord()
        return dsl.transactionCoroutine {
            it.dsl().insertInto(CATEGORY)
                .set(record)
                .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
                .set(CATEGORY.CREATED_AT, OffsetDateTime.now())
                .returningResult(CATEGORY.asterisk())
                .awaitFirstInto(CATEGORY, Category::class).also { row ->
                    require(row.id != row.parentId) {
                        "Category cannot be its own parent"
                    }
                }
        }.toDto()
    }

    suspend fun updateById(id: CategoryId, payload: CategoryDto): CategoryDto? {
        require(id != payload.parentId) {
            "Category cannot be its own parent"
        }
        val record = payload.toRecord()

        return dsl.update(CATEGORY)
            .set(record)
            .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
            .where(CATEGORY.ID.eq(id.value))
            .returningResult(CATEGORY.asterisk())
            .awaitFirstOrNullInto(CATEGORY, Category::class)?.toDto()
    }

    suspend fun deleteById(id: CategoryId) : CategoryDto? = dsl.deleteFrom(CATEGORY)
        .where(CATEGORY.ID.eq(id.value))
        .returningResult(CATEGORY.asterisk())
        .awaitFirstOrNullInto(CATEGORY, Category::class)?.toDto()
}


private fun CategoryDto.toRecord() = CategoryRecord().also {
    it.id = id.value
    it.parentId = parentId?.value
    it.name = name
    it.slug = slug.value
    it.description = description
}


private fun Category.toDto() = CategoryDto(
    CategoryId(id!!),
    parentId?.let { CategoryId(it) },
    name!!,
    Slug(slug!!),
    description!!,
    createdAt?.toKotlinInstant(),
    updatedAt?.toKotlinInstant(),
)


private fun getChildren(parentId: CategoryId?, plain: List<CategoryDto>): List<CategoryTreeDto> {
    return plain.filter {
        it.parentId == parentId
    }.map {
        CategoryTreeDto(
            it.id,
            it.name,
            it.slug,
            it.description,
            getChildren(it.id, plain).ifEmpty { null },
            it.createdAt,
            it.updatedAt,
        )
    }
}

private fun Iterable<Category>.toTreeResponse() = getChildren(null, this.map { it.toDto() })