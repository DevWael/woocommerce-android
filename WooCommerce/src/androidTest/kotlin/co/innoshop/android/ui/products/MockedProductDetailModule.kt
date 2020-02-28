package co.innoshop.android.ui.products

import android.os.Bundle
import co.innoshop.android.viewmodel.SavedStateWithArgs
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.model.toAppModel
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers.Unconfined
import org.wordpress.android.fluxc.model.WCProductModel
import org.wordpress.android.fluxc.store.WooCommerceStore

@Module
internal abstract class MockedProductDetailModule {
    @Module
    companion object {
        private var product: WCProductModel? = null

        fun setMockProduct(product: WCProductModel) {
            this.product = product
        }

        @JvmStatic
        @Provides
        fun provideProductDetailViewModel(
            currencyFormatter: CurrencyFormatter,
            networkStatus: NetworkStatus,
            wcStore: WooCommerceStore,
            site: SelectedSite
        ): MockedProductDetailViewModel {
            val mockProductRepository = mock<ProductDetailRepository>()
            val coroutineDispatchers = CoroutineDispatchers(Unconfined, Unconfined, Unconfined)
            val savedState: SavedStateWithArgs = mock()

            val mockedProductDetailViewModel = spy(
                    MockedProductDetailViewModel(
                            coroutineDispatchers,
                            wcStore,
                            site,
                            mockProductRepository,
                            networkStatus,
                            currencyFormatter,
                            savedState
                    )
            )

            doReturn(product?.toAppModel()).whenever(mockProductRepository).getProduct(any())
            doReturn(true).whenever(networkStatus).isConnected()

            return mockedProductDetailViewModel
        }

        @JvmStatic
        @Provides
        fun provideDefaultArgs(): Bundle? {
            return null
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(MockedProductDetailViewModel::class)
    abstract fun bindFactory(factory: MockedProductDetailViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    abstract fun bindSavedStateRegistryOwner(fragment: ProductDetailFragment): SavedStateRegistryOwner
}
