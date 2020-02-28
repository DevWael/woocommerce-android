package co.innoshop.android.ui.products

import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_LOADED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_UPDATE_ERROR
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_UPDATE_SUCCESS
import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.model.Product
import co.innoshop.android.model.toAppModel
import co.innoshop.android.model.toDataModel
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.util.WooLog
import co.innoshop.android.util.WooLog.T.PRODUCTS
import co.innoshop.android.util.suspendCancellableCoroutineWithTimeout
import co.innoshop.android.util.suspendCoroutineWithTimeout
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.WCProductAction.FETCH_PRODUCT_SKU_AVAILABILITY
import org.wordpress.android.fluxc.action.WCProductAction.FETCH_SINGLE_PRODUCT
import org.wordpress.android.fluxc.action.WCProductAction.UPDATED_PRODUCT
import org.wordpress.android.fluxc.generated.WCProductActionBuilder
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.FetchProductSkuAvailabilityPayload
import org.wordpress.android.fluxc.store.WCProductStore.OnProductChanged
import org.wordpress.android.fluxc.store.WCProductStore.OnProductSkuAvailabilityChanged
import org.wordpress.android.fluxc.store.WCProductStore.OnProductUpdated
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@OpenClassOnDebug
class ProductDetailRepository @Inject constructor(
    private val dispatcher: Dispatcher,
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite
) {
    companion object {
        private const val ACTION_TIMEOUT = 10L * 1000
    }

    private var continuationUpdateProduct: Continuation<Boolean>? = null
    private var continuationFetchProduct: CancellableContinuation<Boolean>? = null
    private var continuationVerifySku: CancellableContinuation<Boolean>? = null

    init {
        dispatcher.register(this)
    }

    fun onCleanup() {
        dispatcher.unregister(this)
    }

    suspend fun fetchProduct(remoteProductId: Long): Product? {
        try {
            continuationFetchProduct?.cancel()
            suspendCancellableCoroutineWithTimeout<Boolean>(ACTION_TIMEOUT) {
                continuationFetchProduct = it

                val payload = WCProductStore.FetchSingleProductPayload(selectedSite.get(), remoteProductId)
                dispatcher.dispatch(WCProductActionBuilder.newFetchSingleProductAction(payload))
            }
        } catch (e: CancellationException) {
            WooLog.d(PRODUCTS, "CancellationException while fetching single product")
        }

        continuationFetchProduct = null
        return getProduct(remoteProductId)
    }

    /**
     * Fires the request to update the product
     *
     * @return the result of the action as a [Boolean]
     */
    suspend fun updateProduct(updatedProduct: Product): Boolean {
        return try {
            suspendCoroutineWithTimeout<Boolean>(ACTION_TIMEOUT) {
                continuationUpdateProduct = it

                val payload = WCProductStore.UpdateProductPayload(
                        selectedSite.get(), updatedProduct.toDataModel(getCachedWCProductModel(updatedProduct.remoteId))
                )
                dispatcher.dispatch(WCProductActionBuilder.newUpdateProductAction(payload))
            } ?: false // request timed out
        } catch (e: CancellationException) {
            WooLog.e(PRODUCTS, "Exception encountered while updating product", e)
            false
        }
    }

    /**
     * Fires the request to check if sku is available for a given [selectedSite]
     *
     * @return the result of the action as a [Boolean]
     */
    suspend fun verifySkuAvailability(sku: String): Boolean? {
        continuationVerifySku?.cancel()
        return try {
            suspendCancellableCoroutineWithTimeout<Boolean>(ACTION_TIMEOUT) {
                continuationVerifySku = it

                val payload = FetchProductSkuAvailabilityPayload(selectedSite.get(), sku)
                dispatcher.dispatch(WCProductActionBuilder.newFetchProductSkuAvailabilityAction(payload))
            } // request timed out
        } catch (e: CancellationException) {
            WooLog.e(PRODUCTS, "Exception encountered while verifying product sku availability", e)
            null
        }
    }

    private fun getCachedWCProductModel(remoteProductId: Long) =
            productStore.getProductByRemoteId(selectedSite.get(), remoteProductId)

    fun getProduct(remoteProductId: Long): Product? = getCachedWCProductModel(remoteProductId)?.toAppModel()

    fun geProductExistsBySku(sku: String) = productStore.geProductExistsBySku(selectedSite.get(), sku)

    fun getCachedVariantCount(remoteProductId: Long) =
            productStore.getVariationsForProduct(selectedSite.get(), remoteProductId).size

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductChanged(event: OnProductChanged) {
        if (event.causeOfChange == FETCH_SINGLE_PRODUCT) {
            if (event.isError) {
                continuationFetchProduct?.resume(false)
            } else {
                AnalyticsTracker.track(PRODUCT_DETAIL_LOADED)
                continuationFetchProduct?.resume(true)
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductUpdated(event: OnProductUpdated) {
        if (event.causeOfChange == UPDATED_PRODUCT) {
            if (event.isError) {
                AnalyticsTracker.track(PRODUCT_DETAIL_UPDATE_ERROR, mapOf(
                        AnalyticsTracker.KEY_ERROR_CONTEXT to this::class.java.simpleName,
                        AnalyticsTracker.KEY_ERROR_TYPE to event.error?.type?.toString(),
                        AnalyticsTracker.KEY_ERROR_DESC to event.error?.message))
                continuationUpdateProduct?.resume(false)
            } else {
                AnalyticsTracker.track(PRODUCT_DETAIL_UPDATE_SUCCESS)
                continuationUpdateProduct?.resume(true)
            }
            continuationUpdateProduct = null
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductSkuAvailabilityChanged(event: OnProductSkuAvailabilityChanged) {
        if (event.causeOfChange == FETCH_PRODUCT_SKU_AVAILABILITY) {
            // TODO: add event to track sku availability success
            continuationVerifySku?.resume(event.available)
            continuationVerifySku = null
        }
    }
}
