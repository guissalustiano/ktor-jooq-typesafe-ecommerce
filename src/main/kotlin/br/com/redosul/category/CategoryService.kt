package br.com.redosul.category

import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.await
import br.com.redosul.plugins.toKotlinInstant
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class CategoryService(private val dsl: DSLContext) {
    suspend fun findAll() : List<CategoryTreeDto> = dsl.selectFrom(CATEGORY)
        .await()
        .map{r -> r.toCategoryDto()}
        .toTreeResponse()

    suspend fun findById(id: CategoryId) : CategoryDto? {
        return dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(id.value))
            .awaitFirstOrNull()
            ?.map{ r -> r.toCategoryDto() }
    }

    suspend fun create(payload: CategoryDto): CategoryDto {
        return dsl.transactionCoroutine {
            it.dsl().insertInto(CATEGORY)
                .set(payload.toRecord())
                .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
                .set(CATEGORY.CREATED_AT, OffsetDateTime.now())
                .returningResult(CATEGORY.asterisk())
                .awaitFirst()
                .map { r -> r.toCategoryDto() }
        }
    }

    suspend fun updateById(id: CategoryId, payload: CategoryDto): CategoryDto? {
        return dsl.update(CATEGORY)
            .set(payload.toRecord())
            .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
            .where(CATEGORY.ID.eq(id.value))
            .returningResult(CATEGORY.asterisk())
            .awaitFirstOrNull()
            ?.map { r -> r.toCategoryDto() }
    }

    suspend fun deleteById(id: CategoryId) : CategoryDto? = dsl.deleteFrom(CATEGORY)
        .where(CATEGORY.ID.eq(id.value))
        .returningResult(CATEGORY.asterisk())
        .awaitFirstOrNull()
        ?.map { r -> r.toCategoryDto() }
}

private fun CategoryDto.toRecord() = CategoryRecord().also {
    it.id = id.value
    it.parentId = parentId?.value
    it.name = name
    it.slug = slug.value
    it.description = description
}

private fun Record.toCategoryDto() = this.into(CATEGORY).toDto()

private fun CategoryRecord.toDto() = CategoryDto(
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

private fun List<CategoryDto>.toTreeResponse() = getChildren(null, this)