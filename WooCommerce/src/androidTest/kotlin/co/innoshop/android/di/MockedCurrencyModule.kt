package co.innoshop.android.di

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.ui.orders.WcOrderTestUtils
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.utils.WCSiteUtils
import dagger.Module
import dagger.Provides
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooCommerceRestClient
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Singleton

@Module
object MockedCurrencyModule {
    private var valueToReturn: String? = null

    fun setCurrencyFormatting(valueToReturn: String) {
        co.innoshop.android.di.MockedCurrencyModule.valueToReturn = valueToReturn
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideCurrencyFormatter(selectedSite: SelectedSite): CurrencyFormatter {
        val mockDispatcher = mock<Dispatcher>()
        val mockContext = mock<Context>()
        val mockWcStore = WooCommerceStore(
                mockContext,
                mockDispatcher,
                WooCommerceRestClient(mockContext, mockDispatcher, mock(), mock(), mock())
        )
        val mockCurrencyFormatter = spy(CurrencyFormatter(mockWcStore, selectedSite))
        /**
         * Whatever parameters are passed to the [CurrencyFormatter.formatCurrency] method, the same
         * parameters will be passed to the mock method defined in [WcOrderTestUtils.formatCurrencyForDisplay]
         */
        whenever(mockCurrencyFormatter.formatCurrency(anyString(), anyString(), anyBoolean()))
                .thenAnswer { invocation ->
            val args = invocation.arguments
            WCSiteUtils.formatCurrencyForDisplay(
                    args[0] as String,
                    WCSiteUtils.generateSiteSettings(),
                    args[1] as String?,
                    args[2] as Boolean
            )
        }
        return mockCurrencyFormatter
    }
}
