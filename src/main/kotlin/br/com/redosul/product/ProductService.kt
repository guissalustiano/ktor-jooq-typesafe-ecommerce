package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.pojos.Category
import br.com.redosul.generated.tables.pojos.Product
import br.com.redosul.generated.tables.pojos.ProductVariant
import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.generated.tables.records.ProductVariantRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.generated.tables.references.PRODUCT
import br.com.redosul.generated.tables.references.PRODUCT_VARIANT
import br.com.redosul.plugins.awaitFirstInto
import br.com.redosul.plugins.awaitFirstOrNullInto
import br.com.redosul.plugins.awaitInto
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.noCondition
import org.jooq.kotlin.coroutines.transactionCoroutine
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

    suspend fun create(payload: ProductSetPayload): Pair<Product, List<ProductVariant>?> {
        val productRecord = payload.toRecord()
        val variantsRecord = payload.variants?.map { it.toRecord() }

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()

            val product = dsl.insertInto(PRODUCT)
                .set(productRecord)
                .set(PRODUCT.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .set(PRODUCT.CREATED_AT, ZonedDateTime.now().toOffsetDateTime())
                .returningResult(PRODUCT.asterisk())
                .awaitFirstInto(PRODUCT, Product::class)

            val variants = variantsRecord?.map {
                dsl.insertInto(PRODUCT_VARIANT)
                    .set(it)
                    .set(PRODUCT_VARIANT.PRODUCT_ID, product.id)
                    .set(PRODUCT_VARIANT.UPDATED_AT, ZonedDateTime.now().toOffsetDateTime())
                    .set(PRODUCT_VARIANT.CREATED_AT, ZonedDateTime.now().toOffsetDateTime())
                    .returningResult(PRODUCT_VARIANT.asterisk())
                    .awaitFirstInto(PRODUCT_VARIANT, ProductVariant::class)
            }

            Pair(product, variants)
        }
    }

    suspend fun updateById(id: ProductId, payload: ProductSetPayload): Product? {
        val record = payload.toRecord()

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

private fun ProductVariantSetPayload.toRecord() = ProductVariantRecord().also {
    fun setSize(size: ProductVariantSetPayload.Size) {
        it.size = size.value
    }

    fun setColor(color: ProductVariantSetPayload.Color) {
        it.colorName = color.name

        if (color is ProductVariantSetPayload.Color.RGB) {
            it.colorRed = color.red.toShort()
            it.colorGreen = color.green.toShort()
            it.colorBlue = color.blue.toShort()
        }

        if (color is ProductVariantSetPayload.Color.Image) {
            it.colorUrl = color.url
        }
    }

    if (this is ProductVariantSetPayload.Size) {
        setSize(this)
    }

    if (this is ProductVariantSetPayload.Color) {
        setColor(this)
    }

    if (this is ProductVariantSetPayload.ColorSize) {
        setSize(this.size)
        setColor(this.color)
    }
}

private fun ProductSetPayload.toRecord() = ProductRecord().also {
    it.categoryId = categoryId.value
    it.name = name
    it.slug = slug.value
    it.description = description
}