package co.innoshop.android.model

import android.os.Parcelable
import co.innoshop.android.extensions.roundError
import co.innoshop.android.ui.products.ProductStockStatus
import kotlinx.android.parcel.Parcelize
import org.wordpress.android.fluxc.model.WCProductVariationModel
import org.wordpress.android.fluxc.model.WCProductVariationModel.ProductVariantOption
import java.math.BigDecimal

@Parcelize
data class ProductVariant(
    val remoteProductId: Long,
    val remoteVariationId: Long,
    val imageUrl: String?,
    val price: BigDecimal?,
    val stockStatus: ProductStockStatus,
    val stockQuantity: Int,
    val optionName: String,
    var priceWithCurrency: String? = null,
    val purchasable: Boolean
) : Parcelable {
    fun isSameVariant(variant: ProductVariant): Boolean {
        return remoteVariationId == variant.remoteVariationId &&
                remoteProductId == variant.remoteProductId &&
                imageUrl == variant.imageUrl &&
                price == variant.price &&
                stockQuantity == variant.stockQuantity &&
                stockStatus == variant.stockStatus &&
                optionName == variant.optionName &&
                priceWithCurrency == variant.priceWithCurrency &&
                purchasable == variant.purchasable
    }
}

fun WCProductVariationModel.toAppModel(): ProductVariant {
    return ProductVariant(
            this.remoteProductId,
            this.remoteVariationId,
            this.imageUrl,
            this.price.toBigDecimalOrNull()?.roundError(),
            ProductStockStatus.fromString(this.stockStatus),
            this.stockQuantity,
            getAttributeOptionName(this.getProductVariantOptions()),
            purchasable = this.purchasable
    )
}

/**
 * Given a list of [ProductVariantOption]
 * returns the product variant combination name in the format {option1} - {option2}
 */
private fun getAttributeOptionName(variantOptions: List<ProductVariantOption>): String {
    var optionName = ""
    for (variantOption in variantOptions) {
        if (!variantOption.option.isNullOrEmpty()) {
            if (optionName.isNotEmpty()) {
                optionName += " - "
            }
            optionName += "${variantOption.option}"
        }
    }
    return optionName
}
