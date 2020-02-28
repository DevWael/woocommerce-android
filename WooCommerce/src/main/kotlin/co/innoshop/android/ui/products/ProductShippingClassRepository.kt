package co.innoshop.android.ui.products

import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.util.WooLog
import co.innoshop.android.util.WooLog.T.PRODUCTS
import co.innoshop.android.util.suspendCancellableCoroutineWithTimeout
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode.MAIN
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.WCProductAction.FETCH_PRODUCT_SHIPPING_CLASS_LIST
import org.wordpress.android.fluxc.generated.WCProductActionBuilder
import org.wordpress.android.fluxc.model.WCProductShippingClassModel
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.FetchProductShippingClassListPayload
import org.wordpress.android.fluxc.store.WCProductStore.OnProductShippingClassesChanged
import javax.inject.Inject
import kotlin.coroutines.resume

@OpenClassOnDebug
class ProductShippingClassRepository @Inject constructor(
    private val dispatcher: Dispatcher,
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite
) {
    companion object {
        private const val ACTION_TIMEOUT = 10L * 1000
        private const val SHIPPING_CLASS_PAGE_SIZE = WCProductStore.DEFAULT_PRODUCT_SHIPPING_CLASS_PAGE_SIZE
    }

    private var shippingClassOffset = 0
    private var continuation: CancellableContinuation<Boolean>? = null

    final var canLoadMoreShippingClasses = true
        private set

    init {
        dispatcher.register(this)
    }

    fun onCleanup() {
        dispatcher.unregister(this)
    }

    suspend fun fetchProductShippingClasses(loadMore: Boolean = false): List<WCProductShippingClassModel> {
        try {
            continuation?.cancel()
            suspendCancellableCoroutineWithTimeout<Boolean>(ACTION_TIMEOUT) {
                continuation = it
                shippingClassOffset = if (loadMore) {
                    shippingClassOffset + SHIPPING_CLASS_PAGE_SIZE
                } else {
                    0
                }
                val payload = FetchProductShippingClassListPayload(
                        selectedSite.get(),
                        pageSize = SHIPPING_CLASS_PAGE_SIZE,
                        offset = shippingClassOffset
                )
                dispatcher.dispatch(WCProductActionBuilder.newFetchProductShippingClassListAction(payload))
            }
        } catch (e: CancellationException) {
            WooLog.d(PRODUCTS, "CancellationException while fetching product shipping classes")
        }

        continuation = null
        return getProductShippingClasses()
    }

    fun getProductShippingClasses() =
            productStore.getShippingClassListForSite(selectedSite.get())

    @SuppressWarnings("unused")
    @Subscribe(threadMode = MAIN)
    fun onProductShippingClassesChanged(event: OnProductShippingClassesChanged) {
        if (event.causeOfChange == FETCH_PRODUCT_SHIPPING_CLASS_LIST) {
            canLoadMoreShippingClasses = event.canLoadMore
            if (event.isError) {
                continuation?.resume(false)
            } else {
                continuation?.resume(true)
            }
        }
    }
}
