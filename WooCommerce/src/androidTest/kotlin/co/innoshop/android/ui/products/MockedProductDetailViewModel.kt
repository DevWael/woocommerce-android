package co.innoshop.android.ui.products

import co.innoshop.android.viewmodel.SavedStateWithArgs
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.model.Product
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.CurrencyFormatter
import org.wordpress.android.fluxc.store.WooCommerceStore
import java.math.BigDecimal
import kotlin.math.roundToInt

final class MockedProductDetailViewModel @AssistedInject constructor(
    dispatchers: CoroutineDispatchers,
    wooCommerceStore: WooCommerceStore,
    selectedSite: SelectedSite,
    productRepository: ProductDetailRepository,
    networkStatus: NetworkStatus,
    private val currencyFormatter: CurrencyFormatter,
    @Assisted val arg0: SavedStateWithArgs
) : ProductDetailViewModel(
        arg0,
        dispatchers,
        selectedSite,
        productRepository,
        networkStatus,
        currencyFormatter,
        wooCommerceStore
) {
    // FIXME: This is a temporary fix that allows the connected test to be built. It fails and should be fixed, though.
//    override val viewStateData: LiveDataDelegate<ViewState> =
//            LiveDataDelegate(arg0, ViewState(), "", onChange = {
//                combineData(it.product!!, Parameters("$", "oz", "in"))
//            })

    private fun combineData(product: Product, parameters: Parameters): ProductDetailViewState {
        val weight = if (product.weight > 0) "${product.weight.roundToInt()}${parameters.weightUnit ?: ""}" else ""

        val hasLength = product.length > 0
        val hasWidth = product.width > 0
        val hasHeight = product.height > 0
        val unit = parameters.dimensionUnit ?: ""
        val size = if (hasLength && hasWidth && hasHeight) {
            "${product.length.roundToInt()} x ${product.width.roundToInt()} x ${product.height.roundToInt()} $unit"
        } else if (hasWidth && hasHeight) {
            "${product.width.roundToInt()} x ${product.height.roundToInt()} $unit"
        } else {
            ""
        }.trim()

        return ProductDetailViewState(
                product = product,
                storedProduct = product,
                cachedProduct = product,
                weightWithUnits = weight,
                sizeWithUnits = size,
                priceWithCurrency = formatCurrency(product.price, parameters.currencyCode),
                salePriceWithCurrency = formatCurrency(product.salePrice, parameters.currencyCode),
                regularPriceWithCurrency = formatCurrency(product.regularPrice, parameters.currencyCode)
        )
    }

    private fun formatCurrency(amount: BigDecimal?, currencyCode: String?): String {
        return currencyCode?.let {
            currencyFormatter.formatCurrency(amount ?: BigDecimal.ZERO, it)
        } ?: amount.toString()
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<MockedProductDetailViewModel>
}
