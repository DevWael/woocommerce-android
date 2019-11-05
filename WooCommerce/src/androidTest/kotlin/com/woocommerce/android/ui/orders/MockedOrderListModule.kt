package com.woocommerce.android.ui.orders

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.woocommerce.android.di.ActivityScope
import com.woocommerce.android.tools.NetworkStatus
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.util.CoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.Dispatchers
import org.mockito.ArgumentMatchers.anyString
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.WCOrderStatusModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.OrderRestClient
import org.wordpress.android.fluxc.store.WCGatewayStore
import org.wordpress.android.fluxc.store.WCOrderStore

@Module
abstract class MockedOrderListModule {
    @Module
    companion object {
        private var orders: List<WCOrderModel>? = null
        private var orderStatusList: Map<String, WCOrderStatusModel>? = null

        fun setOrders(orders: List<WCOrderModel>) {
            this.orders = orders
        }

        fun setOrderStatusList(orderStatusList: Map<String, WCOrderStatusModel>) {
            this.orderStatusList = orderStatusList
        }

        @JvmStatic
        @ActivityScope
        @Provides
        fun provideOrderListPresenter(): OrderListContract.Presenter {
            /**
             * Creating a spy object here since we need to mock specific methods of [OrderListPresenter] class
             * instead of mocking all the methods in the class.
             * We cannot mock final classes ([WCOrderStore], [SelectedSite] and [NetworkStatus]), so
             * creating a mock instance of those classes and passing to the presenter class constructor.
             */
            val mockDispatcher = mock<Dispatcher>()
            val mockContext = mock<Context>()
            val gatewayStore = mock<WCGatewayStore>()
            val coroutineDispatchers = CoroutineDispatchers(
                    Dispatchers.Unconfined, Dispatchers.Unconfined, Dispatchers.Unconfined)
            val mockedOrderListPresenter = spy(OrderListPresenter(
                    coroutineDispatchers,
                    mockDispatcher,
                    WCOrderStore(mockDispatcher, OrderRestClient(mockContext, mockDispatcher, mock(), mock(), mock())),
                    SelectedSite(mockContext, mock()),
                    NetworkStatus(mockContext),
                    gatewayStore
            ))

            /**
             * Mocking the below methods in [OrderListPresenter] class to pass mock values.
             * These are the methods that invoke [WCOrderStore] methods from FluxC
             */
            doReturn(true).whenever(mockedOrderListPresenter).isOrderStatusOptionsRefreshing()
            doReturn(orderStatusList).whenever(mockedOrderListPresenter).getOrderStatusOptions()
            doReturn(orders).whenever(mockedOrderListPresenter).fetchOrdersFromDb(anyString(), eq(false))
            return mockedOrderListPresenter
        }
    }

    @ContributesAndroidInjector
    abstract fun orderListFragment(): OrderListFragment
}
