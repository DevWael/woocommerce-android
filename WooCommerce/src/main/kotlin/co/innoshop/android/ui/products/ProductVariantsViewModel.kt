package co.innoshop.android.ui.products

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.R.string
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.model.ProductVariant
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.util.WooLog
import co.innoshop.android.viewmodel.LiveDataDelegate
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.SavedStateWithArgs
import co.innoshop.android.viewmodel.ScopedViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ProductVariantsViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers,
    private val productVariantsRepository: ProductVariantsRepository,
    private val networkStatus: NetworkStatus,
    private val currencyFormatter: CurrencyFormatter
) : ScopedViewModel(savedState, dispatchers) {
    private var remoteProductId = 0L

    private val _productVariantList = MutableLiveData<List<ProductVariant>>()
    val productVariantList: LiveData<List<ProductVariant>> = _productVariantList

    val viewStateLiveData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateLiveData

    fun start(remoteProductId: Long) {
        loadProductVariants(remoteProductId)
    }

    fun refreshProductVariants(remoteProductId: Long) {
        viewState = viewState.copy(isRefreshing = true)
        loadProductVariants(remoteProductId)
    }

    fun onLoadMoreRequested(remoteProductId: Long) {
        loadProductVariants(remoteProductId, loadMore = true)
    }

    override fun onCleared() {
        super.onCleared()
        productVariantsRepository.onCleanup()
    }

    private fun isLoadingMore() = viewState.isLoadingMore == true

    private fun isRefreshing() = viewState.isRefreshing == true

    private fun loadProductVariants(
        remoteProductId: Long,
        loadMore: Boolean = false
    ) {
        if (loadMore && !productVariantsRepository.canLoadMoreProductVariants) {
            WooLog.d(WooLog.T.PRODUCTS, "can't load more product variants")
            return
        }

        if (loadMore && isLoadingMore()) {
            WooLog.d(WooLog.T.PRODUCTS, "already loading more product variants")
            return
        }

        if (loadMore && isRefreshing()) {
            WooLog.d(WooLog.T.PRODUCTS, "already refreshing product variants")
            return
        }

        this.remoteProductId = remoteProductId

        launch {
            viewState = viewState.copy(isLoadingMore = loadMore)
            if (!loadMore) {
                // if this is the initial load, first get the product variants from the db and if there are any show
                // them immediately, otherwise make sure the skeleton shows
                val variantsInDb = productVariantsRepository.getProductVariantList(remoteProductId)
                if (variantsInDb.isNullOrEmpty()) {
                    viewState = viewState.copy(isSkeletonShown = true)
                } else {
                    _productVariantList.value = combineData(variantsInDb)
                }
            }

            fetchProductVariants(remoteProductId, loadMore = loadMore)
        }
    }

    private suspend fun fetchProductVariants(
        remoteProductId: Long,
        loadMore: Boolean = false
    ) {
        if (networkStatus.isConnected()) {
            val fetchedVariants = productVariantsRepository.fetchProductVariants(remoteProductId, loadMore)
            if (fetchedVariants.isNullOrEmpty()) {
                if (!loadMore) {
                    viewState = viewState.copy(isEmptyViewVisible = true)
                }
            } else {
                _productVariantList.value = combineData(fetchedVariants)
            }
        } else {
            triggerEvent(ShowSnackbar(string.offline_error))
        }
        viewState = viewState.copy(
                isSkeletonShown = false,
                isRefreshing = false,
                isLoadingMore = false
        )
    }

    private fun combineData(productVariants: List<ProductVariant>): List<ProductVariant> {
        val currencyCode = productVariantsRepository.getCurrencyCode()
        productVariants.map { productVariant ->
            productVariant.priceWithCurrency = currencyCode?.let {
                currencyFormatter.formatCurrency(productVariant.price ?: BigDecimal.ZERO, it)
            } ?: productVariant.price.toString()
        }
        return productVariants
    }

    @Parcelize
    data class ViewState(
        val isSkeletonShown: Boolean? = null,
        val isRefreshing: Boolean? = null,
        val isLoadingMore: Boolean? = null,
        val canLoadMore: Boolean? = null,
        val isEmptyViewVisible: Boolean? = null
    ) : Parcelable

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ProductVariantsViewModel>
}
