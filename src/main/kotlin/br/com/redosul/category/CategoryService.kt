package br.com.redosul.category

import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.plugins.await
import br.com.redosul.plugins.ifDefined
import br.com.redosul.plugins.toKotlinInstant
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class CategoryService(private val dsl: DSLContext) {
    suspend fun findAll() : List<CategoryTreeResponse> {
        return dsl.selectFrom(CATEGORY)
            .await()
            .map{r -> r.toCategoryDto()}
            .toTreeResponse()
    }

    suspend fun findById(id: CategoryId) : CategoryResponse? {
        return dsl.selectFrom(CATEGORY)
            .where(CATEGORY.ID.eq(id.value))
            .awaitFirstOrNull()
            ?.map{ r -> r.toCategoryDto() }
    }

    suspend fun findBySlug(slug: CategorySlug) : CategoryResponse? {
        return dsl.selectFrom(CATEGORY)
            .where(CATEGORY.SLUG.eq(slug.value.value))
            .awaitFirstOrNull()
            ?.map{ r -> r.toCategoryDto() }
    }

    suspend fun create(payload: CategoryCreatePayload): CategoryResponse {
        payload.parentId?.let {
            findById(it) ?: throw CategoryError.ParentNotFound(it)
        }

        findBySlug(payload.slug)?.let {
            throw CategoryError.SlugAlreadyExists(payload.slug)
        }

        return dsl.transactionCoroutine { config ->
            val dsl = config.dsl()

            dsl.insertInto(CATEGORY)
                .set(payload.toRecord())
                .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
                .set(CATEGORY.CREATED_AT, OffsetDateTime.now())
                .returningResult(CATEGORY.asterisk())
                .awaitFirst()
                .map { r -> r.toCategoryDto() }
        }
    }

    suspend fun updateById(id: CategoryId, payload: CategoryUpdatePayload): CategoryResponse? {
        payload.parentId.ifDefined { parentId ->
            parentId?.let {
                findById(it) ?: throw CategoryError.ParentNotFound(it)
            }
        }

        payload.slug.ifDefined { slug ->
                findBySlug(slug)?.takeIf { it.id != id }?.let {
                    throw CategoryError.SlugAlreadyExists(it.slug)
                }
        }

        return dsl.transactionCoroutine { config ->
            val dsl = config.dsl()

            dsl.update(CATEGORY)
                .set(payload.toRecord())
                .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
                .where(CATEGORY.ID.eq(id.value))
                .returningResult(CATEGORY.asterisk())
                .awaitFirstOrNull()
                ?.map { r -> r.toCategoryDto() }
        }
    }

    suspend fun deleteById(id: CategoryId) : CategoryResponse? {
        return dsl.transactionCoroutine { config ->
            val dsl = config.dsl()

            dsl.deleteFrom(CATEGORY)
                .where(CATEGORY.ID.eq(id.value))
                .returningResult(CATEGORY.asterisk())
                .awaitFirstOrNull()
                ?.map { r -> r.toCategoryDto() }
        }
    }
}

private fun CategoryCreatePayload.toRecord() = CategoryRecord().also {
    it.parentId = parentId?.value
    it.name = name
    it.slug = slug.value.value
    it.description = description
}

private fun CategoryUpdatePayload.toRecord() = CategoryRecord().also {
   parentId.ifDefined { v ->  it.parentId = v?.value }
    name.ifDefined { v -> it.name = v }
    slug.ifDefined { v -> it.slug = v.value.value }
    description.ifDefined { v -> it.description = v }
}

private fun Record.toCategoryDto() = this.into(CATEGORY).toDto()

private fun CategoryRecord.toDto() = CategoryResponse(
    CategoryId(id!!),
    parentId?.let { CategoryId(it) },
    name!!,
    slug!!.toCategorySlug(),
    description!!,
    createdAt!!.toKotlinInstant(),
    updatedAt!!.toKotlinInstant(),
)

private fun getChildren(parentId: CategoryId?, plain: List<CategoryResponse>): List<CategoryTreeResponse> {
    return plain.filter {
        it.parentId == parentId
    }.map {
        CategoryTreeResponse(
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

internal fun List<CategoryResponse>.toTreeResponse() = getChildren(null, this)