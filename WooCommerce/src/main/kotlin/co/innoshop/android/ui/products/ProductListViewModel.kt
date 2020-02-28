package co.innoshop.android.ui.products

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.media.ProductImagesService.Companion.OnProductImagesUpdateCompletedEvent
import co.innoshop.android.model.Product
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.WooLog
import co.innoshop.android.viewmodel.LiveDataDelegate
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.SavedStateWithArgs
import co.innoshop.android.viewmodel.ScopedViewModel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@OpenClassOnDebug
class ProductListViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers,
    private val productRepository: ProductListRepository,
    private val networkStatus: NetworkStatus
) : ScopedViewModel(savedState, dispatchers) {
    companion object {
        private const val SEARCH_TYPING_DELAY_MS = 500L
    }

    private val _productList = MutableLiveData<List<Product>>()
    val productList: LiveData<List<Product>> = _productList

    final val viewStateLiveData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateLiveData

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        EventBus.getDefault().register(this)
        if (_productList.value == null) {
            loadProducts()
        }
    }

    override fun onCleared() {
        super.onCleared()
        productRepository.onCleanup()
        EventBus.getDefault().unregister(this)
    }

    fun isSearching() = viewState.isSearchActive == true

    private fun isLoadingMore() = viewState.isLoadingMore == true

    private fun isRefreshing() = viewState.isRefreshing == true

    fun getSearchQuery() = viewState.query

    fun onSearchQueryChanged(query: String) {
        viewState = viewState.copy(query = query, isEmptyViewVisible = false)

        if (query.length > 2) {
            onSearchRequested()
        } else {
            launch {
                searchJob?.cancelAndJoin()

                _productList.value = emptyList()
                viewState = viewState.copy(isEmptyViewVisible = false)
            }
        }
    }

    fun onRefreshRequested() {
        AnalyticsTracker.track(Stat.PRODUCT_LIST_PULLED_TO_REFRESH)
        refreshProducts()
    }

    fun onSearchOpened() {
        _productList.value = emptyList()
        viewState = viewState.copy(isSearchActive = true)
    }

    fun onSearchClosed() {
        launch {
            searchJob?.cancelAndJoin()
            viewState = viewState.copy(query = null, isSearchActive = false, isEmptyViewVisible = false)
            loadProducts()
        }
    }

    fun onLoadMoreRequested() {
        loadProducts(loadMore = true)
    }

    fun onSearchRequested() {
        AnalyticsTracker.track(Stat.PRODUCT_LIST_SEARCHED,
                mapOf(AnalyticsTracker.KEY_SEARCH to viewState.query)
        )
        loadProducts()
    }

    final fun loadProducts(loadMore: Boolean = false) {
        if (loadMore && !productRepository.canLoadMoreProducts) {
            WooLog.d(WooLog.T.PRODUCTS, "can't load more products")
            return
        }

        if (loadMore && isLoadingMore()) {
            WooLog.d(WooLog.T.PRODUCTS, "already loading more products")
            return
        }

        if (loadMore && isRefreshing()) {
            WooLog.d(WooLog.T.PRODUCTS, "already refreshing products")
            return
        }

        if (isSearching()) {
            // cancel any existing search, then start a new one after a brief delay so we don't actually perform
            // the fetch until the user stops typing
            searchJob?.cancel()
            searchJob = launch {
                delay(SEARCH_TYPING_DELAY_MS)
                viewState = viewState.copy(
                        isLoadingMore = loadMore,
                        isSkeletonShown = !loadMore,
                        isEmptyViewVisible = false
                )
                fetchProductList(viewState.query, loadMore = loadMore)
            }
        } else {
            // if a fetch is already active, wait for it to finish before we start another one
            waitForExistingLoad()

            loadJob = launch {
                val showSkeleton: Boolean
                if (loadMore) {
                    showSkeleton = false
                } else {
                    // if this is the initial load, first get the products from the db and show them immediately
                    val productsInDb = productRepository.getProductList()
                    if (productsInDb.isEmpty()) {
                        showSkeleton = true
                    } else {
                        _productList.value = productsInDb
                        showSkeleton = viewState.isRefreshing == true
                    }
                }
                viewState = viewState.copy(
                        isSkeletonShown = showSkeleton,
                        isEmptyViewVisible = false,
                        isLoadingMore = loadMore
                )
                fetchProductList(loadMore = loadMore)
            }
        }
    }

    /**
     * If products are already being fetched, wait for the existing job to finish
     */
    private fun waitForExistingLoad() {
        if (loadJob?.isActive == true) {
            launch {
                try {
                    loadJob?.join()
                } catch (e: CancellationException) {
                    WooLog.d(WooLog.T.PRODUCTS, "CancellationException while waiting for existing fetch")
                }
            }
        }
    }

    fun refreshProducts() {
        viewState = viewState.copy(isRefreshing = true)
        loadProducts()
    }

    private suspend fun fetchProductList(searchQuery: String? = null, loadMore: Boolean = false) {
        if (networkStatus.isConnected()) {
            if (searchQuery.isNullOrEmpty()) {
                _productList.value = productRepository.fetchProductList(loadMore)
            } else {
                productRepository.searchProductList(searchQuery, loadMore)?.let { fetchedProducts ->
                    // make sure the search query hasn't changed while the fetch was processing
                    if (searchQuery == productRepository.lastSearchQuery) {
                        if (loadMore) {
                            _productList.value = _productList.value.orEmpty() + fetchedProducts
                        } else {
                            _productList.value = fetchedProducts
                        }
                    } else {
                        WooLog.d(WooLog.T.PRODUCTS, "Search query changed")
                    }
                }
            }

            viewState = viewState.copy(
                    canLoadMore = productRepository.canLoadMoreProducts,
                    isEmptyViewVisible = _productList.value?.isEmpty() == true
            )
        } else {
            triggerEvent(ShowSnackbar(R.string.offline_error))
        }

        viewState = viewState.copy(
                isSkeletonShown = false,
                isLoadingMore = false,
                isRefreshing = false
        )
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: OnProductImagesUpdateCompletedEvent) {
        loadProducts()
    }

    @Parcelize
    data class ViewState(
        val isSkeletonShown: Boolean? = null,
        val isLoadingMore: Boolean? = null,
        val canLoadMore: Boolean? = null,
        val isRefreshing: Boolean? = null,
        val query: String? = null,
        val isSearchActive: Boolean? = null,
        val isEmptyViewVisible: Boolean? = null
    ) : Parcelable

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ProductListViewModel>
}
