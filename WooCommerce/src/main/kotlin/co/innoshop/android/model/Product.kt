package co.innoshop.android.model

import android.os.Parcelable
import co.innoshop.android.extensions.formatDateToISO8601Format
import co.innoshop.android.extensions.roundError
import co.innoshop.android.ui.products.ProductBackorderStatus
import co.innoshop.android.ui.products.ProductStatus
import co.innoshop.android.ui.products.ProductStockStatus
import co.innoshop.android.ui.products.ProductType
import kotlinx.android.parcel.Parcelize
import org.wordpress.android.fluxc.model.WCProductModel
import org.wordpress.android.util.DateTimeUtils
import java.math.BigDecimal
import java.util.Date

@Parcelize
data class Product(
    val remoteId: Long,
    val name: String,
    val description: String,
    val type: ProductType,
    val status: ProductStatus?,
    val stockStatus: ProductStockStatus,
    val backorderStatus: ProductBackorderStatus,
    val dateCreated: Date,
    val firstImageUrl: String?,
    val totalSales: Int,
    val reviewsAllowed: Boolean,
    val isVirtual: Boolean,
    val ratingCount: Int,
    val averageRating: Float,
    val permalink: String,
    val externalUrl: String,
    val price: BigDecimal?,
    val salePrice: BigDecimal?,
    val regularPrice: BigDecimal?,
    val taxClass: String,
    val manageStock: Boolean,
    val stockQuantity: Int,
    val sku: String,
    val length: Float,
    val width: Float,
    val height: Float,
    val weight: Float,
    val shippingClass: String,
    val isDownloadable: Boolean,
    val fileCount: Int,
    val downloadLimit: Int,
    val downloadExpiry: Int,
    val purchaseNote: String,
    val numVariations: Int,
    val images: List<Image>,
    val attributes: List<Attribute>,
    val dateOnSaleToGmt: Date?,
    val dateOnSaleFromGmt: Date?,
    val soldIndividually: Boolean
) : Parcelable {
    @Parcelize
    data class Image(
        val id: Long,
        val name: String,
        val source: String,
        val dateCreated: Date
    ) : Parcelable

    @Parcelize
    data class Attribute(
        val id: Long,
        val name: String,
        val options: List<String>,
        val isVisible: Boolean
    ) : Parcelable

    fun isSameProduct(product: Product): Boolean {
        return remoteId == product.remoteId &&
                stockQuantity == product.stockQuantity &&
                stockStatus == product.stockStatus &&
                status == product.status &&
                manageStock == product.manageStock &&
                backorderStatus == product.backorderStatus &&
                soldIndividually == product.soldIndividually &&
                sku == product.sku &&
                type == product.type &&
                numVariations == product.numVariations &&
                name == product.name &&
                description == product.description &&
                images == product.images
    }

    /**
     * Method merges the updated product fields edited by the user with the locally cached
     * [Product] model and returns the updated [Product] model.
     *
     * [newProduct] includes the updated product fields edited by the user.
     * if [newProduct] is not null, a copy of the stored [Product] model is created
     * and product fields edited by the user and added to this model before returning it
     *
     */
    fun mergeProduct(newProduct: Product?): Product {
        return newProduct?.let { updatedProduct ->
            this.copy(
                    description = updatedProduct.description,
                    name = updatedProduct.name,
                    sku = updatedProduct.sku,
                    manageStock = updatedProduct.manageStock,
                    stockStatus = updatedProduct.stockStatus,
                    stockQuantity = updatedProduct.stockQuantity,
                    backorderStatus = updatedProduct.backorderStatus,
                    soldIndividually = updatedProduct.soldIndividually
            )
        } ?: this.copy()
    }
}

fun Product.toDataModel(storedProductModel: WCProductModel?): WCProductModel {
    return (storedProductModel ?: WCProductModel()).also {
        it.remoteProductId = remoteId
        it.description = description
        it.name = name
        it.sku = sku
        it.manageStock = manageStock
        it.stockStatus = ProductStockStatus.fromStockStatus(stockStatus)
        it.stockQuantity = stockQuantity
        it.soldIndividually = soldIndividually
        it.backorders = ProductBackorderStatus.fromBackorderStatus(backorderStatus)
    }
}

fun WCProductModel.toAppModel(): Product {
    return Product(
        this.remoteProductId,
        this.name,
        this.description,
        ProductType.fromString(this.type),
        ProductStatus.fromString(this.status),
        ProductStockStatus.fromString(this.stockStatus),
        ProductBackorderStatus.fromString(this.backorders),
        DateTimeUtils.dateFromIso8601(this.dateCreated) ?: Date(),
        this.getFirstImageUrl(),
        this.totalSales,
        this.reviewsAllowed,
        this.virtual,
        this.ratingCount,
        this.averageRating.toFloatOrNull() ?: 0f,
        this.permalink,
        this.externalUrl,
        this.price.toBigDecimalOrNull()?.roundError(),
        this.salePrice.toBigDecimalOrNull()?.roundError(),
        this.regularPrice.toBigDecimalOrNull()?.roundError(),
        this.taxClass,
        this.manageStock,
        this.stockQuantity,
        this.sku,
        this.length.toFloatOrNull() ?: 0f,
        this.width.toFloatOrNull() ?: 0f,
        this.height.toFloatOrNull() ?: 0f,
        this.weight.toFloatOrNull() ?: 0f,
        this.shippingClass,
        this.downloadable,
        this.getDownloadableFiles().size,
        this.downloadLimit,
        this.downloadExpiry,
        this.purchaseNote,
        this.getNumVariations(),
        this.getImages().map {
            Product.Image(
                    it.id,
                    it.name,
                    it.src,
                    DateTimeUtils.dateFromIso8601(this.dateCreated) ?: Date()
            )
        },
        this.getAttributes().map {
            Product.Attribute(
                    it.id,
                    it.name,
                    it.options,
                    it.visible
            )
        },
        this.dateOnSaleToGmt.formatDateToISO8601Format(),
        this.dateOnSaleFromGmt.formatDateToISO8601Format(),
        this.soldIndividually
    )
}

/**
 * Returns the product as a [ProductReviewProduct] for use with the product reviews feature.
 */
fun WCProductModel.toProductReviewProductModel() =
        ProductReviewProduct(this.remoteProductId, this.name, this.permalink)
