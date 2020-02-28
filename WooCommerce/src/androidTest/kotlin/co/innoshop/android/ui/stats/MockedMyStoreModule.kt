package co.innoshop.android.ui.stats

import android.content.Context
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.di.ActivityScope
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.ui.mystore.MyStoreContract
import co.innoshop.android.ui.mystore.MyStoreFragment
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooCommerceRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.OrderRestClient
import org.wordpress.android.fluxc.network.rest.wpcom.wc.orderstats.OrderStatsRestClient
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WCStatsStore
import org.wordpress.android.fluxc.store.WooCommerceStore
import co.innoshop.android.ui.mystore.MyStorePresenter
import dagger.android.ContributesAndroidInjector

@Module
abstract class MockedMyStoreModule {
    @Module
    companion object {
        @JvmStatic
        @ActivityScope
        @Provides
        fun provideMyStorePresenter(): MyStoreContract.Presenter {
            /**
             * Creating a spy object here since we need to mock specific methods of [MyStorePresenter] class
             * instead of mocking all the methods in the class.
             * We cannot mock final classes ([WCStatsStore], [WCOrderStore], [SelectedSite] and [NetworkStatus]), so
             * creating a mock instance of those classes and passing to the presenter class constructor.
             */
            val mockDispatcher = mock<Dispatcher>()
            val mockContext = mock<Context>()
            val mockedPresenter = spy(
                    MyStorePresenter(
                            mockDispatcher,
                            WooCommerceStore(
                                    mockContext,
                                    mockDispatcher,
                                    WooCommerceRestClient(mockContext, mockDispatcher, mock(), mock(), mock())
                            ),
                            WCStatsStore(
                                    mockDispatcher,
                                    mockContext,
                                    OrderStatsRestClient(mockContext, mockDispatcher, mock(), mock(), mock())
                            ),
                            WCOrderStore(
                                    mockDispatcher,
                                    OrderRestClient(mockContext, mockDispatcher, mock(), mock(), mock())),
                            SelectedSite(mockContext, mock()),
                            NetworkStatus(mockContext)
                    )
            )

            /**
             * Mocking the below methods in [MyStorePresenter] class to pass mock values.
             * These are the methods that invoke [WCStatsStore] methods from FluxC
             */
            doNothing().whenever(mockedPresenter).fetchHasOrders()
//            doReturn(any()).whenever(mockedPresenter).getStatsCurrency()
//            doReturn(orders).whenever(mockedPresenter).loadStats(DAYS, false)
            return mockedPresenter
        }
    }

    @ContributesAndroidInjector
    abstract fun myStoreFragment(): MyStoreFragment
}
