package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.ProductVariant
import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.generated.tables.records.ProductVariantRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.generated.tables.references.PRODUCT
import br.com.redosul.generated.tables.references.PRODUCT_VARIANT
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.await
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.Record
import org.jooq.impl.DSL.*
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class ProductService(private val dsl: DSLContext) {

    suspend fun findAll(categoryId: CategoryId?): List<ProductDto> {
        val cteName = "subcategory"

        val cteCondition = if (categoryId == null) noCondition() else CATEGORY.ID.eq(categoryId.value)

        val cte = name(cteName).`as`(
                select(CATEGORY.asterisk())
                    .from(CATEGORY)
                    .where(cteCondition)
                    .unionAll(
                        select(CATEGORY.asterisk()).from(table(cteName))
                            .join(CATEGORY)
                            .on(
                                CATEGORY.PARENT_ID
                                    .eq(field(name(cteName, CATEGORY.ID.name), CATEGORY.ID.dataType))
                            )
                    )
            )

        return dsl.withRecursive(cte)
                    .selectDistinct(
                        PRODUCT.asterisk(),
                        multiset(
                            select(PRODUCT_VARIANT.asterisk())
                                .from(PRODUCT_VARIANT)
                                .where(PRODUCT_VARIANT.PRODUCT_ID.eq(PRODUCT.ID))
                        ).`as`("variants")
                    )
                    .from(cte)
                    .join(PRODUCT)
                    .on(PRODUCT.CATEGORY_ID.eq(field(name(cteName, CATEGORY.ID.name), CATEGORY.ID.dataType)))
                    .await().map {
                        it.toProductWithVariantDto()
                    }


    }

    suspend fun findById(id: ProductId): ProductDto? {
        return dsl.select(
                PRODUCT.asterisk(),
                multiset(
                    select(PRODUCT_VARIANT.asterisk())
                        .from(PRODUCT_VARIANT)
                        .where(PRODUCT_VARIANT.PRODUCT_ID.eq(PRODUCT.ID))
                ).`as`("variants")
            )
                .from(PRODUCT)
                .where(PRODUCT.ID.eq(id.value))
                .awaitFirstOrNull()
                ?.toProductWithVariantDto()
    }

    suspend fun create(payload: ProductDto): ProductDto {
        val productRecord = payload.toRecord()
        val variantsRecord = payload.variants?.map { it.toRecord() }

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()

            val product = dsl.insertInto(PRODUCT)
                .set(productRecord)
                .set(PRODUCT.UPDATED_AT, OffsetDateTime.now())
                .set(PRODUCT.CREATED_AT, OffsetDateTime.now())
                .returningResult(PRODUCT.asterisk())
                .awaitFirst().into(PRODUCT)

            val variants = variantsRecord?.map {
                dsl.insertInto(PRODUCT_VARIANT)
                    .set(it)
                    .set(PRODUCT_VARIANT.PRODUCT_ID, product.into(PRODUCT).id)
                    .set(PRODUCT_VARIANT.UPDATED_AT, OffsetDateTime.now())
                    .set(PRODUCT_VARIANT.CREATED_AT, OffsetDateTime.now())
                    .returningResult(PRODUCT_VARIANT.asterisk())
                    .awaitFirst().into(PRODUCT_VARIANT)
            }

            Pair(product, variants).toDto()
        }
    }

    suspend fun updateById(id: ProductId, payload: ProductDto): ProductDto? {
        val record = payload.toRecord()

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()

            val product =  dsl.update(PRODUCT)
                .set(record)
                .set(PRODUCT.UPDATED_AT, OffsetDateTime.now())
                .where(PRODUCT.ID.eq(id.value))
                .returningResult(PRODUCT.asterisk())
                .awaitFirstOrNull()?.into(PRODUCT)

            val currentVariantsIds = dsl.select(PRODUCT_VARIANT.ID).from(PRODUCT_VARIANT)
                .where(PRODUCT_VARIANT.PRODUCT_ID.eq(id.value))
                .await()
                .map { it.get(PRODUCT_VARIANT.ID)!! }

            val variants = payload.variants ?: emptyList()

            // delete variants that are not in payload
            // for some reason filterNot { it.id in variants.map { it.id }.values } doesn't work
            currentVariantsIds.filterNot {id -> ProductVariantId(id) in variants.map { it.id } }.map {
                dsl.deleteFrom(PRODUCT_VARIANT)
                    .where(PRODUCT_VARIANT.ID.eq(it))
                    .returningResult(PRODUCT_VARIANT.asterisk())
                    .awaitFirstOrNull()?.into(PRODUCT)
            }

            // create variants that are not in db
            val newVariants = variants.filterNot { it.id.value in currentVariantsIds }.map {
                dsl.insertInto(PRODUCT_VARIANT)
                    .set(it.toRecord())
                    .set(PRODUCT_VARIANT.PRODUCT_ID, id.value)
                    .set(PRODUCT_VARIANT.UPDATED_AT, OffsetDateTime.now())
                    .set(PRODUCT_VARIANT.CREATED_AT, OffsetDateTime.now())
                    .returningResult(PRODUCT_VARIANT.asterisk())
                    .awaitFirst().into(PRODUCT_VARIANT)
            }

            // update variants that are in db and payload
            val updatedVariants = variants.filter { it.id.value in currentVariantsIds }.map {
                dsl.update(PRODUCT_VARIANT)
                    .set(it.toRecord())
                    .set(PRODUCT_VARIANT.UPDATED_AT, OffsetDateTime.now())
                    .where(PRODUCT_VARIANT.ID.eq(it.id.value))
                    .returningResult(PRODUCT_VARIANT.asterisk())
                    .awaitFirst().into(PRODUCT_VARIANT)
            }

            product?.let{ Pair(it, newVariants + updatedVariants).toDto() }

        }
    }

    suspend fun deleteById(id: ProductId): ProductDto? {

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()

            val variants = dsl.deleteFrom(PRODUCT_VARIANT)
                    .where(PRODUCT_VARIANT.PRODUCT_ID.eq(id.value))
                    .returningResult(PRODUCT_VARIANT.asterisk())
                    .await().map{it.into(PRODUCT_VARIANT)}


            val product = dsl.deleteFrom(PRODUCT)
                .where(PRODUCT.ID.eq(id.value))
                .returningResult(PRODUCT.asterisk())
                .awaitFirstOrNull()?.into(PRODUCT)


            product ?: return@transactionCoroutine null

            Pair(product, variants).toDto()
        }
    }
}

private fun ProductDto.toRecord() = ProductRecord().also {
    it.categoryId = categoryId.value
    it.name = name
    it.slug = slug.value
    it.description = description
}

private fun ProductVariantDto.toRecord() = ProductVariantRecord().also {
    it.id = id.value

    fun setSize(size: ProductVariantDto.Size) {
        it.size = size.value
    }

    fun setColor(color: ProductVariantDto.Color) {
        it.colorName = color.name

        if (color is ProductVariantDto.Color.RGB) {
            it.colorRed = color.red.toShort()
            it.colorGreen = color.green.toShort()
            it.colorBlue = color.blue.toShort()
        }

        if (color is ProductVariantDto.Color.Image) {
            it.colorUrl = color.url
        }
    }

    if (this is ProductVariantDto.Size) {
        setSize(this)
    }

    if (this is ProductVariantDto.Color) {
        setColor(this)
    }

    if (this is ProductVariantDto.ColorSize) {
        setSize(this.size)
        setColor(this.color)
    }
}

private fun Pair<ProductRecord, List<ProductVariantRecord>?>.toDto(): ProductDto {
    val (product, variants) = this
    return ProductDto(
        ProductId(product.id!!),
        CategoryId(product.categoryId!!),
        product.name!!,
        Slug(product.slug!!),
        product.description!!,
        variants?.map { it.toDto() }
    )
}


private fun Record.toProductWithVariantDto(): ProductDto {
    val product = this.into(PRODUCT)
    val variants = this.get("variants", List::class.java)?.map { (it as Record).into(PRODUCT_VARIANT) }

    return Pair(product, variants).toDto()
}

private fun ProductVariantRecord.toDto(): ProductVariantDto {
    requireNotNull(id)
    fun parseOrNullSize(): ProductVariantDto.Size? {
        size ?: return null

        return ProductVariantDto.Size(
            ProductVariantId(id!!),
            size!!
        )
    }

    fun parseOrNullColorImage(): ProductVariantDto.Color.Image? {
        colorName ?: return null
        colorUrl ?: return null

        return ProductVariantDto.Color.Image(
            ProductVariantId(id!!),
            colorName!!,
            colorUrl!!
        )
    }

    fun parseOrNullColorRGB(): ProductVariantDto.Color.RGB? {
        colorName ?: return null
        colorRed ?: return null
        colorGreen ?: return null
        colorBlue ?: return null

        return ProductVariantDto.Color.RGB(
            ProductVariantId(id!!),
            colorName!!,
            colorRed!!.toUByte(),
            colorGreen!!.toUByte(),
            colorBlue!!.toUByte(),
        )
    }

    fun parseOrNullColor(): ProductVariantDto.Color? {
        return parseOrNullColorImage() ?: parseOrNullColorRGB()
    }

    val color = parseOrNullColor()
    val size = parseOrNullSize()

    return when {
        color != null && size != null -> ProductVariantDto.ColorSize(
            ProductVariantId(id!!),
            size,
            color
        )
        color != null -> color
        size != null -> size
        else -> error("Dirty ${ProductVariantDto::class} with id $id")
    }
}