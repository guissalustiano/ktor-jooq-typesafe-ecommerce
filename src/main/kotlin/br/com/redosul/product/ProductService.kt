package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.pojos.Product
import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.generated.tables.references.PRODUCT
import br.com.redosul.plugins.awaitFirstInto
import br.com.redosul.plugins.awaitFirstOrNullInto
import br.com.redosul.plugins.awaitInto
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.noCondition
import java.time.ZonedDateTime

class ProductService(private val dsl: DSLContext) {

    suspend fun findAll(categoryId: CategoryId?): Iterable<Product> {
        val cteName = "subcategory"

        val cteCondition = if (categoryId == null) noCondition() else CATEGORY.ID.eq(categoryId.value)

        val cte = DSL.name(cteName).`as`(
                DSL.select(CATEGORY.asterisk())
                    .from(CATEGORY)
                    .where(cteCondition)
                    .unionAll(
                        DSL.select(CATEGORY.asterisk()).from(DSL.table(cteName))
                            .join(CATEGORY)
                            .on(
                                CATEGORY.PARENT_ID
                                    .eq(DSL.field(DSL.name(cteName, CATEGORY.ID.name), CATEGORY.ID.dataType))
                            )
                    )
            )

        return dsl.withRecursive(cte)
                    .select(PRODUCT.asterisk())
                    .from(cte)
                    .join(PRODUCT)
                    .on(PRODUCT.CATEGORY_ID.eq(DSL.field(DSL.name(cteName, CATEGORY.ID.name), CATEGORY.ID.dataType)))
                    .awaitInto(PRODUCT, Product::class)
    }

    suspend fun findById(id: ProductId) = dsl.selectFrom(PRODUCT)
        .where(PRODUCT.ID.eq(id.value))
        .awaitFirstOrNullInto(PRODUCT, Product::class)

    suspend fun create(record: ProductRecord) = dsl.insertInto(PRODUCT)
        .set(record)
        .set(PRODUCT.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
        .set(PRODUCT.CREATED_AT, ZonedDateTime.now().toOffsetDateTime())
        .returningResult(PRODUCT.asterisk())
        .awaitFirstInto(PRODUCT, Product::class)

    suspend fun updateById(id: ProductId, record: ProductRecord): Product? {
        return dsl.update(PRODUCT)
            .set(record)
            .set(PRODUCT.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
            .where(PRODUCT.ID.eq(id.value))
            .returningResult(PRODUCT.asterisk())
            .awaitFirstOrNullInto(PRODUCT, Product::class)
    }

    suspend fun deleteById(id: ProductId) = dsl.deleteFrom(PRODUCT)
        .where(PRODUCT.ID.eq(id.value))
        .returningResult(PRODUCT.asterisk())
        .awaitFirstOrNullInto(PRODUCT, Product::class)
}
