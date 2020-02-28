package co.innoshop.android.ui.orders

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.WCOrderShipmentProviderModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooCommerceRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.OrderRestClient
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WooCommerceStore

@Module
abstract class MockedAddOrderTrackingProviderListModule {
    @Module
    companion object {
        private var order: WCOrderModel? = null
        private var storeCountry: String? = null
        private var providers: List<WCOrderShipmentProviderModel>? = null

        fun setOrderInfo(order: WCOrderModel) {
            this.order = order
        }

        fun setStoreCountry(storeCountry: String) {
            this.storeCountry = storeCountry
        }

        fun setOrderShipmentTrackingProviders(orderShipmentTrackingProviders: List<WCOrderShipmentProviderModel>) {
            this.providers = orderShipmentTrackingProviders
        }

        @JvmStatic
        @Provides
        fun provideAddOrderTrackingProviderListPresenter(): AddOrderTrackingProviderListContract.Presenter {
            /**
             * Creating a spy object here since we need to mock specific methods of
             * [AddOrderTrackingProviderListPresenter] class instead of mocking all the methods in the class.
             * We cannot mock final classes ([WCOrderStore], [SelectedSite] and [NetworkStatus]), so
             * creating a mock instance of those classes and passing to the presenter class constructor.
             */
            val mockDispatcher = mock<Dispatcher>()
            val mockContext = mock<Context>()
            val mockSiteStore = mock<SiteStore>()
            val mockNetworkStatus = mock<NetworkStatus>()

            val mockPresenter = spy(AddOrderTrackingProviderListPresenter(
                    mockDispatcher,
                    WCOrderStore(mockDispatcher, OrderRestClient(mockContext, mockDispatcher, mock(), mock(), mock())),
                    WooCommerceStore(
                            mockContext,
                            mockDispatcher,
                            WooCommerceRestClient(mockContext, mockDispatcher, mock(), mock(), mock())
                    ),
                    SelectedSite(mockContext, mockSiteStore),
                    mockNetworkStatus
            ))

            /**
             * Mocking the below methods in [AddOrderTrackingProviderListPresenter] class to pass mock values.
             * These are the methods that invoke [WCOrderShipmentProviderModel] methods from FluxC.
             */
            doReturn(order).whenever(mockPresenter).loadOrderDetailFromDb(any())
            doReturn(storeCountry).whenever(mockPresenter).loadStoreCountryFromDb()
            doReturn(providers).whenever(mockPresenter).getShipmentTrackingProvidersFromDb()

            return mockPresenter
        }
    }
}
