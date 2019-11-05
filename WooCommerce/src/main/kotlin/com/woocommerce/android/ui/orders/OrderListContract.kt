package com.woocommerce.android.ui.orders

import com.woocommerce.android.ui.base.BasePresenter
import com.woocommerce.android.ui.base.BaseView
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.WCOrderStatusModel

interface OrderListContract {
    interface Presenter : BasePresenter<View> {
        var isShipmentTrackingProviderFetched: Boolean
        var arePaymentGatewaysFetched: Boolean
        fun loadOrders(filterByStatus: String? = null, forceRefresh: Boolean, isFirstRun: Boolean = false)
        fun loadMoreOrders(orderStatusFilter: String? = null)
        fun canLoadMoreOrders(): Boolean
        fun isLoadingOrders(): Boolean
        fun isOrderStatusOptionsRefreshing(): Boolean
        fun openOrderDetail(order: WCOrderModel)
        fun fetchOrdersFromDb(orderStatusFilter: String? = null, isForceRefresh: Boolean): List<WCOrderModel>
        fun fetchAndLoadOrdersFromDb(
            orderStatusFilter: String? = null,
            isForceRefresh: Boolean,
            isFirstRun: Boolean = false
        )
        fun searchOrders(searchQuery: String)
        fun searchMoreOrders(searchQuery: String)
        fun getOrderStatusOptions(): Map<String, WCOrderStatusModel>
        fun getOrderStatusList(): List<WCOrderStatusModel>
        fun refreshOrderStatusOptions()
        fun loadShipmentTrackingProviders(order: WCOrderModel)
        suspend fun loadPaymentGateways()
    }

    interface View : BaseView<Presenter> {
        var isRefreshPending: Boolean
        var isSearching: Boolean
        var isRefreshing: Boolean
        var isFilterEnabled: Boolean

        fun setLoadingMoreIndicator(active: Boolean)
        fun showOrders(orders: List<WCOrderModel>, filterByStatus: String? = null, isFreshData: Boolean)
        fun showEmptyView(show: Boolean)
        fun refreshFragmentState()
        fun showLoadOrdersError()
        fun showNoConnectionError()

        fun showOrderDetail(order: WCOrderModel)

        fun submitSearch(query: String)
        fun submitSearchDelayed(query: String)
        fun showSearchResults(query: String, orders: List<WCOrderModel>)
        fun addSearchResults(query: String, orders: List<WCOrderModel>)
        fun clearSearchResults()

        fun showLoading(show: Boolean)
        fun showSkeleton(show: Boolean)
        fun showRefreshingIndicator(show: Boolean)

        fun setOrderStatusOptions(orderStatusOptions: Map<String, WCOrderStatusModel>)
        fun updateOrderStatusList(orderStatusList: List<WCOrderStatusModel>)
        fun showNoOrderStatusListError()
    }
}
