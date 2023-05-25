package br.com.redosul.product

import br.com.redosul.category.CategoryId
import br.com.redosul.generated.tables.records.ProductImageRecord
import br.com.redosul.generated.tables.records.ProductRecord
import br.com.redosul.generated.tables.records.ProductVariantRecord
import br.com.redosul.generated.tables.references.CATEGORY
import br.com.redosul.generated.tables.references.PRODUCT
import br.com.redosul.generated.tables.references.PRODUCT_IMAGE
import br.com.redosul.generated.tables.references.PRODUCT_VARIANT
import br.com.redosul.plugins.Slug
import br.com.redosul.plugins.URL
import br.com.redosul.plugins.await
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.*

class ProductService(private val dsl: DSLContext) {

    suspend fun findAll(categoryId: CategoryId?): List<ProductResponse> {
        TODO()
//        val cteName = "subcategory"
//
//        val cteCondition = if (categoryId == null) noCondition() else CATEGORY.ID.eq(categoryId.value)
//
//        val cte = name(cteName).`as`(
//                select(CATEGORY.asterisk())
//                    .from(CATEGORY)
//                    .where(cteCondition)
//                    .unionAll(
//                        select(CATEGORY.asterisk()).from(table(cteName))
//                            .join(CATEGORY)
//                            .on(
//                                CATEGORY.PARENT_ID
//                                    .eq(field(name(cteName, CATEGORY.ID.name), CATEGORY.ID.dataType))
//                            )
//                    )
//            )
//
//        return dsl.withRecursive(cte)
//                    .select(
//                        PRODUCT.asterisk(),
//                        multiset(
//                            select(PRODUCT_VARIANT.asterisk())
//                                .from(PRODUCT_VARIANT)
//                                .where(PRODUCT_VARIANT.PRODUCT_ID.eq(PRODUCT.ID))
//                        ).`as`("variants"),
//                        multiset(
//                            select(PRODUCT_IMAGE.asterisk())
//                                .from(PRODUCT_IMAGE)
//                                .where(PRODUCT_IMAGE.PRODUCT_ID.eq(PRODUCT.ID))
//                        ).`as`("images"),
//                    )
//                    .distinctOn(PRODUCT.ID)
//                    .from(cte)
//                    .join(PRODUCT)
//                    .on(PRODUCT.CATEGORY_ID.eq(field(name(cteName, CATEGORY.ID.name), CATEGORY.ID.dataType)))
//                    .await().map {
//                        it.toProductWithVariantDto()
//                    }


    }

    suspend fun findBySlug(slug: ProductSlug): ProductResponse? {
        TODO()
//        return dsl.select(
//                PRODUCT.asterisk(),
//                multiset(
//                    select(PRODUCT_VARIANT.asterisk())
//                        .from(PRODUCT_VARIANT)
//                        .where(PRODUCT_VARIANT.PRODUCT_ID.eq(PRODUCT.ID))
//                ).`as`("variants"),
//                multiset(
//                    select(PRODUCT_IMAGE.asterisk())
//                        .from(PRODUCT_IMAGE)
//                        .where(PRODUCT_IMAGE.PRODUCT_ID.eq(PRODUCT.ID))
//                ).`as`("images"),
//            )
//                .from(PRODUCT)
//                .where(PRODUCT.ID.eq(id.value))
//                .awaitFirstOrNull()
//                ?.toProductWithVariantDto()
    }

    suspend fun create(payload: ProductCreatePayload): Unit {
        TODO()
//        val productRecord = payload.toRecord()
//        val variantsRecord = payload.variants?.map { it.toRecord() } ?: emptyList()
//        val imagesRecord = payload.images.map{ it.toRecord().apply { productVariantId = null } } +
//            (payload.variants?.flatMap{ variant ->
//                variant.images.map{ it.toRecord().apply { productVariantId = variant.id.value } }
//            } ?: emptyList())
//
//        return dsl.transactionCoroutine {config ->
//            val dsl = config.dsl()
//
//            val product = dsl.insertInto(PRODUCT)
//                .set(productRecord)
//                .set(PRODUCT.UPDATED_AT, OffsetDateTime.now())
//                .set(PRODUCT.CREATED_AT, OffsetDateTime.now())
//                .returningResult(PRODUCT.asterisk())
//                .awaitFirst().into(PRODUCT)
//
//            val variants = variantsRecord.map {
//                dsl.insertInto(PRODUCT_VARIANT)
//                    .set(it)
//                    .set(PRODUCT_VARIANT.PRODUCT_ID, product.id)
//                    .set(PRODUCT_VARIANT.UPDATED_AT, OffsetDateTime.now())
//                    .set(PRODUCT_VARIANT.CREATED_AT, OffsetDateTime.now())
//                    .returningResult(PRODUCT_VARIANT.asterisk())
//                    .awaitFirst().into(PRODUCT_VARIANT)
//            }
//
//            val images = imagesRecord.map {
//                    dsl.insertInto(PRODUCT_IMAGE)
//                        .set(it)
//                        .set(PRODUCT_IMAGE.PRODUCT_ID, product.id)
//                        .set(PRODUCT_IMAGE.UPDATED_AT, OffsetDateTime.now())
//                        .set(PRODUCT_IMAGE.CREATED_AT, OffsetDateTime.now())
//                        .returningResult(PRODUCT_IMAGE.asterisk())
//                        .awaitFirst().into(PRODUCT_IMAGE)
//            }
//
//            productToDto(product, variants, images)
//        }
    }

    suspend fun create(payload: ProductVariantCreatePayload): Unit {
        TODO()
    }

    suspend fun create(payload: ProductImageCreatePayload): Unit {
        TODO()
    }

    suspend fun createOrUpdate(payload: ProductCreatePayload): Unit {
        TODO()
    }

    suspend fun createOrUpdate(payload: ProductVariantCreatePayload): Unit {
        TODO()
    }

    suspend fun createOrUpdate(payload: ProductImageCreatePayload): Unit {
        TODO()
    }

    suspend fun updateById(slug: ProductSlug, payload: ProductResponse): ProductResponse? {
        TODO()
//        val record = payload.toRecord()
//
//        return dsl.transactionCoroutine {config ->
//            val dsl = config.dsl()
//
//            val product =  dsl.update(PRODUCT)
//                .set(record)
//                .set(PRODUCT.UPDATED_AT, OffsetDateTime.now())
//                .where(PRODUCT.ID.eq(id.value))
//                .returningResult(PRODUCT.asterisk())
//                .awaitFirstOrNull()?.into(PRODUCT)
//
//            val currentVariantsIds = dsl.select(PRODUCT_VARIANT.ID).from(PRODUCT_VARIANT)
//                .where(PRODUCT_VARIANT.PRODUCT_ID.eq(id.value))
//                .await()
//                .map { it.get(PRODUCT_VARIANT.ID)!! }
//
//            val variants = payload.variants ?: emptyList()
//
//            // delete variants that are not in payload
//            // for some reason filterNot { it.id in variants.map { it.id }.values } doesn't work
//            currentVariantsIds.filterNot {id -> ProductVariantId(id) in variants.map { it.id } }.map {
//                dsl.deleteFrom(PRODUCT_VARIANT)
//                    .where(PRODUCT_VARIANT.ID.eq(it))
//                    .returningResult(PRODUCT_VARIANT.asterisk())
//                    .awaitFirstOrNull()?.into(PRODUCT)
//            }
//
//            // create variants that are not in db
//            val newVariants = variants.filterNot { it.id.value in currentVariantsIds }.map {
//                dsl.insertInto(PRODUCT_VARIANT)
//                    .set(it.toRecord())
//                    .set(PRODUCT_VARIANT.PRODUCT_ID, id.value)
//                    .set(PRODUCT_VARIANT.UPDATED_AT, OffsetDateTime.now())
//                    .set(PRODUCT_VARIANT.CREATED_AT, OffsetDateTime.now())
//                    .returningResult(PRODUCT_VARIANT.asterisk())
//                    .awaitFirst().into(PRODUCT_VARIANT)
//            }
//
//            // update variants that are in db and payload
//            val updatedVariants = variants.filter { it.id.value in currentVariantsIds }.map {
//                dsl.update(PRODUCT_VARIANT)
//                    .set(it.toRecord())
//                    .set(PRODUCT_VARIANT.UPDATED_AT, OffsetDateTime.now())
//                    .where(PRODUCT_VARIANT.ID.eq(it.id.value))
//                    .returningResult(PRODUCT_VARIANT.asterisk())
//                    .awaitFirst().into(PRODUCT_VARIANT)
//            }
//
//            // TODO: update images
//
//            product?.let{ productToDto(it, newVariants + updatedVariants, emptyList()) }
//
//        }
    }

    suspend fun deleteBySlug(slug: ProductSlug): Unit? {
        TODO()
//        return dsl.transactionCoroutine {config ->
//            val dsl = config.dsl()
//
//            val images = dsl.deleteFrom(PRODUCT_IMAGE)
//                .where(PRODUCT_IMAGE.PRODUCT_ID.eq(id.value))
//                .returningResult(PRODUCT_IMAGE.asterisk())
//                .await().map{it.into(PRODUCT_IMAGE)}
//
//            val variants = dsl.deleteFrom(PRODUCT_VARIANT)
//                    .where(PRODUCT_VARIANT.PRODUCT_ID.eq(id.value))
//                    .returningResult(PRODUCT_VARIANT.asterisk())
//                    .await().map{it.into(PRODUCT_VARIANT)}
//
//
//            val product = dsl.deleteFrom(PRODUCT)
//                .where(PRODUCT.ID.eq(id.value))
//                .returningResult(PRODUCT.asterisk())
//                .awaitFirstOrNull()?.into(PRODUCT)
//
//
//            product ?: return@transactionCoroutine null
//
//            productToDto(product, variants, images)
//        }
    }

    suspend fun deleteBySlug(slug: ProductVariantSlug): Unit? {
        TODO()
    }

    suspend fun deleteBySlug(slug: ProductImageSlug): Unit? {
        TODO()
    }
}

//private fun ProductResponse.toRecord() = ProductRecord().also {
//    it.categoryId = categoryId.value
//    it.name = name
//    it.slug = slug.value
//    it.description = description
//}
//
//private fun ProductImageResponse.toRecord() = ProductImageRecord().also {
//    it.id = id.value
//    it.url = url.value
//}
//
//
//private fun ProductVariantResponse.toRecord() = ProductVariantRecord().also {
//    it.id = id.value
//
//    fun setSize(size: ProductVariantResponse.Size) {
//        it.size = size.value
//    }
//
//    fun setColor(color: ProductVariantResponse.Color) {
//        it.colorName = color.name
//
//        if (color is ProductVariantResponse.Color.RGB) {
//            it.colorRed = color.red.toShort()
//            it.colorGreen = color.green.toShort()
//            it.colorBlue = color.blue.toShort()
//        }
//
//        if (color is ProductVariantResponse.Color.Image) {
//            it.colorUrl = color.url
//        }
//    }
//
//    if (this is ProductVariantResponse.Size) {
//        setSize(this)
//    }
//
//    if (this is ProductVariantResponse.Color) {
//        setColor(this)
//    }
//
//    if (this is ProductVariantResponse.ColorSize) {
//        setSize(this.size)
//        setColor(this.color)
//    }
//}
//
//private fun productToDto(
//    product: ProductRecord,
//    variants: List<ProductVariantRecord>?,
//    images: List<ProductImageRecord>,
//): ProductResponse {
//    val (productImages, variantsImages) = images.partition{ it.productVariantId == null}
//    return ProductResponse(
//        ProductId(product.id!!),
//        CategoryId(product.categoryId!!),
//        product.name!!,
//        Slug(product.slug!!),
//        product.description!!,
//        variants?.map { it.toDto(variantsImages.filter { img -> img.productVariantId == it.id }) },
//        productImages.map { it.toDto() },
//    )
//}
//
//private fun ProductImageRecord.toDto(): ProductImageResponse {
//    return ProductImageResponse(
//        ProductImageId(id!!),
//        URL(url!!)
//    )
//}
//
//
//private fun Record.toProductWithVariantDto(): ProductResponse {
//    val product = this.into(PRODUCT)
//    val variants = this.get("variants", List::class.java)?.map { (it as Record).into(PRODUCT_VARIANT) }
//    val images = this.get("images", List::class.java)?.map { (it as Record).into(PRODUCT_IMAGE) } ?: emptyList()
//
//    return productToDto(product, variants, images)
//}
//
//private fun ProductVariantRecord.toDto(images: List<ProductImageRecord>): ProductVariantResponse {
//    requireNotNull(id)
//    fun parseOrNullSize(): ProductVariantResponse.Size? {
//        size ?: return null
//
//        return ProductVariantResponse.Size(
//            ProductVariantId(id!!),
//            size!!,
//            images.map { it.toDto() }
//        )
//    }
//
//    fun parseOrNullColorImage(): ProductVariantResponse.Color.Image? {
//        colorName ?: return null
//        colorUrl ?: return null
//
//        return ProductVariantResponse.Color.Image(
//            ProductVariantId(id!!),
//            colorName!!,
//            colorUrl!!,
//                    images.map { it.toDto() }
//        )
//    }
//
//    fun parseOrNullColorRGB(): ProductVariantResponse.Color.RGB? {
//        colorName ?: return null
//        colorRed ?: return null
//        colorGreen ?: return null
//        colorBlue ?: return null
//
//        return ProductVariantResponse.Color.RGB(
//            ProductVariantId(id!!),
//            colorName!!,
//            colorRed!!.toUByte(),
//            colorGreen!!.toUByte(),
//            colorBlue!!.toUByte(),
//            images.map { it.toDto() }
//        )
//    }
//
//    fun parseOrNullColor(): ProductVariantResponse.Color? {
//        return parseOrNullColorImage() ?: parseOrNullColorRGB()
//    }
//
//    val color = parseOrNullColor()
//    val size = parseOrNullSize()
//
//    return when {
//        color != null && size != null -> ProductVariantResponse.ColorSize(
//            ProductVariantId(id!!),
//            size,
//            color,
//            images.map { it.toDto() }
//        )
//        color != null -> color
//        size != null -> size
//        else -> error("Dirty ${ProductVariantResponse::class} with id $id")
//    }
//}