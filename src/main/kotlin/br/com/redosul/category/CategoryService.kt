package br.com.redosul.category

import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.plugins.Id
import br.com.redosul.plugins.UUID
import br.com.redosul.plugins.Undefined
import br.com.redosul.plugins.await
import br.com.redosul.plugins.map
import br.com.redosul.plugins.map
import br.com.redosul.plugins.toKotlinInstant
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Record1
import org.jooq.TableField
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class CategoryService(private val dsl: DSLContext) {
    private val PARENT_CATEGORY = CATEGORY.`as`("parent")

    suspend fun findAll() : List<CategoryTreeResponse> {
        return dsl.select(CATEGORY.asterisk(), PARENT_CATEGORY.SLUG).from(CATEGORY)
            .leftJoin(PARENT_CATEGORY).on(CATEGORY.PARENT_ID.eq(PARENT_CATEGORY.ID))
            .await()
            .map{r -> r.toCategoryDto()}
            .toTreeResponse()
    }

    suspend fun findBySlug(slugId: CategorySlug) : CategoryResponse? {
        return dsl.select(CATEGORY.asterisk(), PARENT_CATEGORY.SLUG).from(CATEGORY)
            .leftJoin(PARENT_CATEGORY).on(CATEGORY.PARENT_ID.eq(PARENT_CATEGORY.ID))
            .where(CATEGORY.SLUG.eq(slugId.slug))
            .awaitFirstOrNull()
            ?.map{ r -> r.toCategoryDto() }
    }

    suspend fun create(payload: CategoryCreatePayload): Unit {
        val payloadParent = payload.parentSlug?.let {
            findBySlug(it) ?: throw CategoryError.ParentNotFound(it)
        }

        findBySlug(payload.slug)?.let {
            throw CategoryError.SlugAlreadyExists(payload.slug)
        }

        val record = dsl.newRecord(CATEGORY).apply {
            parentId = payloadParent?.id?.value
            name = payload.name
            slug = payload.slug.slug
            description = payload.description
        }

        val insertedSlug = dsl.transactionCoroutine { config ->
            val dsl = config.dsl()

            dsl.insertInto(CATEGORY)
                .set(record)
                .returningResult(CATEGORY.SLUG)
                .awaitFirst()
                .toCategorySlug()
        }

        insertedSlug.let { }
    }

    suspend fun updateBySlug(slugId: CategorySlug, payload: CategoryUpdatePayload): Unit? {
        val payloadParent = payload.parentSlug.map { parentSlug ->
            parentSlug?.let {
                findBySlug(it) ?: throw CategoryError.ParentNotFound(it)
            }
        }

        payload.slug.map { slug ->
            findBySlug(slug)?.takeIf { it.slug != slugId }?.let {
                throw CategoryError.SlugAlreadyExists(it.slug)
            }
        }

        val record = dsl.newRecord(CATEGORY).apply {
            payloadParent.map { parentId = it?.id?.value }
            payload.name.map { name = it }
            payload.slug.map { slug = it.slug }
            payload.description.map { description = it }
        }

        val updatedSlug = dsl.transactionCoroutine { config ->
            val dsl = config.dsl()

            dsl.update(CATEGORY)
                .set(record)
                .set(CATEGORY.UPDATED_AT, OffsetDateTime.now())
                .where(CATEGORY.SLUG.eq(slugId.slug))
                .returningResult(CATEGORY.SLUG)
                .awaitFirstOrNull()
                ?.toCategorySlug()
        }

        return updatedSlug?.let { }
    }

    suspend fun createOrUpdate(payload: CategoryCreatePayload): Unit {
        if (findBySlug(payload.slug) == null) {
            create(payload)
        } else {
            updateBySlug(payload.slug, CategoryUpdatePayload(
                Undefined.Defined(payload.parentSlug),
                Undefined.Defined(payload.name),
                Undefined.Defined(payload.slug),
                Undefined.Defined(payload.description),
            ))
        }
    }

    suspend fun deleteBySlug(slugId: CategorySlug) : Unit? {
        val deletedSlug = dsl.transactionCoroutine { config ->
            val dsl = config.dsl()

            dsl.deleteFrom(CATEGORY)
                .where(CATEGORY.SLUG.eq(slugId.slug))
                .returningResult(CATEGORY.SLUG)
                .awaitFirstOrNull()
                ?.toCategorySlug()

        }

        return deletedSlug?.let { }
    }

    private fun <T1> Record1<T1>.toCategorySlug() = map { r -> r.into(CATEGORY).slug }.toCategorySlug()

    private fun Record.toCategoryDto(): CategoryResponse {
        val category = into(CATEGORY)
        val parent = into(PARENT_CATEGORY)
        return CategoryResponse(
            CategoryId(category.id!!),
            parent.slug?.toCategorySlug(),
            category.name!!,
            category.slug!!.toCategorySlug(),
            category.description!!,
            category.createdAt!!.toKotlinInstant(),
            category.updatedAt!!.toKotlinInstant(),
        )
    }
}

private fun getChildren(parentSlug: CategorySlug?, plain: List<CategoryResponse>): List<CategoryTreeResponse> {
    return plain.filter {
        it.parentSlug == parentSlug
    }.map {
        CategoryTreeResponse(
            it.id,
            it.name,
            it.slug,
            it.description,
            getChildren(it.slug, plain).ifEmpty { null },
            it.createdAt,
            it.updatedAt,
        )
    }
}

internal fun List<CategoryResponse>.toTreeResponse() = getChildren(null, this)