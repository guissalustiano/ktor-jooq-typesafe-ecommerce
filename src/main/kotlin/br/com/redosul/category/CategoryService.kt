package br.com.redosul.category

import br.com.redosul.generated.tables.records.CategoryRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.plugins.await
import br.com.redosul.plugins.awaitFirstInto
import br.com.redosul.plugins.awaitFirstOrNullInto
import org.jooq.DSLContext
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.ZonedDateTime

class CategoryService(private val dsl: DSLContext) {
    suspend fun findAll() = dsl.selectFrom(CATEGORY).await()

    suspend fun findById(id: CategoryId) = dsl.selectFrom(CATEGORY)
        .where(CATEGORY.ID.eq(id.value))
        .awaitFirstOrNullInto(CATEGORY)

    suspend fun create(record: CategoryRecord): CategoryRecord {
        return dsl.transactionCoroutine {
            it.dsl().insertInto(CATEGORY)
                .set(record)
                .set(CATEGORY.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .set(CATEGORY.CREATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .returningResult(CATEGORY.asterisk())
                .awaitFirstInto(CATEGORY).also { row ->
                    require(row.id != row.parentId) {
                        "Category cannot be its own parent"
                    }
                }
        }
    }

    suspend fun updateById(id: CategoryId, record: CategoryRecord): CategoryRecord? {
        require(id.value != record.parentId) {
            "Category cannot be its own parent"
        }

        return dsl.update(CATEGORY)
            .set(record)
            .set(CATEGORY.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
            .where(CATEGORY.ID.eq(id.value))
            .returningResult(CATEGORY.asterisk())
            .awaitFirstOrNullInto(CATEGORY)
    }

    suspend fun deleteById(id: CategoryId) = dsl.deleteFrom(CATEGORY)
        .where(CATEGORY.ID.eq(id.value))
        .returningResult(CATEGORY.asterisk())
        .awaitFirstOrNullInto(CATEGORY)
}