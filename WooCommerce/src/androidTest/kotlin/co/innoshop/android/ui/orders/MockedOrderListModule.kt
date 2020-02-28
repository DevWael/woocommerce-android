package co.innoshop.android.ui.orders

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.savedstate.SavedStateRegistryOwner
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.ui.orders.list.MockedOrderListViewModel
import co.innoshop.android.ui.orders.list.OrderFetcher
import co.innoshop.android.ui.orders.list.OrderListFragment
import co.innoshop.android.ui.orders.list.OrderListItemUIType
import co.innoshop.android.ui.orders.list.OrderListRepository
import co.innoshop.android.ui.orders.list.OrderListViewModel
import co.innoshop.android.ui.orders.list.OrderListViewModel.ViewState
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.viewmodel.SavedStateWithArgs
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers.Unconfined
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.WCOrderStatusModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.OrderRestClient
import org.wordpress.android.fluxc.store.WCGatewayStore
import org.wordpress.android.fluxc.store.ListStore
import org.wordpress.android.fluxc.store.WCOrderStore

@Module
abstract class MockedOrderListModule {
    @Module
    companion object {
        private var testOrders: PagedList<OrderListItemUIType>? = null
        private var testOrderStatusOptions: Map<String, WCOrderStatusModel>? = null
        private val mockDispatcher = mock<Dispatcher>()

        fun setMockedOrders(ordersList: PagedList<OrderListItemUIType>) {
            this.testOrders = ordersList
        }

        fun setMockedOrderStatusList(orderStatusOptions: Map<String, WCOrderStatusModel>) {
            this.testOrderStatusOptions = orderStatusOptions
        }

        @JvmStatic
        @Provides
        fun provideOrderListViewModel(
            site: SelectedSite,
            networkStatus: NetworkStatus,
            listStore: ListStore
        ): OrderListViewModel {
            val mockContext = mock<Context>()
            val orderStore = WCOrderStore(
                    mockDispatcher, OrderRestClient(mockContext, mockDispatcher, mock(), mock(), mock()))
            val gatewayStore = mock<WCGatewayStore>()
            val testDispatchers = CoroutineDispatchers(Unconfined, Unconfined, Unconfined)
            val repository = spy(OrderListRepository(mockDispatcher, testDispatchers, orderStore, gatewayStore, site))
            val mockSavedState: SavedStateWithArgs = mock()
            val orderFetcher: OrderFetcher = mock()
            doReturn(MutableLiveData(ViewState())).whenever(mockSavedState).getLiveData<ViewState>(any(), any())

            val viewModel = spy(MockedOrderListViewModel(
                    dispatchers = testDispatchers,
                    repository = repository,
                    orderStore = orderStore,
                    listStore = listStore,
                    networkStatus = networkStatus,
                    dispatcher = mockDispatcher,
                    selectedSite = site,
                    fetcher = orderFetcher,
                    arg0 = mockSavedState
            ))
            viewModel.testOrderData = this.testOrders
            viewModel.testOrderStatusData = this.testOrderStatusOptions

            return viewModel
        }

        @JvmStatic
        @Provides
        fun provideDefaultArgs(): Bundle? {
            return null
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(OrderListViewModel::class)
    abstract fun bindFactory(factory: MockedOrderListViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    abstract fun bindSavedStateRegistryOwner(fragment: OrderListFragment): SavedStateRegistryOwner
}
