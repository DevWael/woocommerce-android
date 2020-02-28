package co.innoshop.android.ui.products

import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.R
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.ui.products.ProductDetailViewModel.ProductDetailViewState
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.viewmodel.BaseUnitTest
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.SavedStateWithArgs
import co.innoshop.android.viewmodel.test
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.WCProductSettingsModel
import org.wordpress.android.fluxc.model.WCSettingsModel
import org.wordpress.android.fluxc.store.WooCommerceStore
import java.math.BigDecimal

class ProductDetailViewModelTest : BaseUnitTest() {
    private val wooCommerceStore: WooCommerceStore = mock()
    private val selectedSite: SelectedSite = mock()
    private val networkStatus: NetworkStatus = mock()
    private val productRepository: ProductDetailRepository = mock()
    private val currencyFormatter: CurrencyFormatter = mock {
        on(it.formatCurrency(any<BigDecimal>(), any(), any())).thenAnswer { i -> "${i.arguments[1]}${i.arguments[0]}" }
    }
    private val savedState: SavedStateWithArgs = mock()

    private val coroutineDispatchers = CoroutineDispatchers(
            Dispatchers.Unconfined, Dispatchers.Unconfined, Dispatchers.Unconfined)
    private val product = ProductTestUtils.generateProduct()
    private val productRemoteId = product.remoteId
    private lateinit var viewModel: ProductDetailViewModel

    private val productWithParameters = ProductDetailViewState(
            product = product,
            cachedProduct = product,
            storedProduct = product,
            isSkeletonShown = false,
            uploadingImageUris = emptyList(),
            weightWithUnits = "10kg",
            sizeWithUnits = "1 x 2 x 3 cm",
            priceWithCurrency = "CZK20.00",
            salePriceWithCurrency = "CZK10.00",
            regularPriceWithCurrency = "CZK30.00"
    )

    @Before
    fun setup() {
        doReturn(MutableLiveData(ProductDetailViewState()))
                .whenever(savedState).getLiveData<ProductDetailViewState>(any(), any())

        viewModel = spy(
                ProductDetailViewModel(
                        savedState,
                        coroutineDispatchers,
                        selectedSite,
                        productRepository,
                        networkStatus,
                        currencyFormatter,
                        wooCommerceStore
                )
        )
        val prodSettings = WCProductSettingsModel(0).apply {
            dimensionUnit = "cm"
            weightUnit = "kg"
        }
        val siteSettings = mock<WCSettingsModel> {
            on(it.currencyCode).thenReturn("CZK")
        }

        doReturn(SiteModel()).whenever(selectedSite).get()
        doReturn(true).whenever(networkStatus).isConnected()
        doReturn(prodSettings).whenever(wooCommerceStore).getProductSettings(any())
        doReturn(siteSettings).whenever(wooCommerceStore).getSiteSettings(any())
    }

    @Test
    fun `Displays the product detail view correctly`() {
        doReturn(product).whenever(productRepository).getProduct(any())

        var productData: ProductDetailViewState? = null
        viewModel.productDetailViewStateData.observeForever { _, new -> productData = new }

        assertThat(productData).isEqualTo(ProductDetailViewState())

        viewModel.start(productRemoteId)

        assertThat(productData).isEqualTo(productWithParameters)
    }

    @Test
    fun `Display error message on fetch product error`() = test {
        whenever(productRepository.fetchProduct(productRemoteId)).thenReturn(null)
        whenever(productRepository.getProduct(productRemoteId)).thenReturn(null)

        var snackbar: ShowSnackbar? = null
        viewModel.event.observeForever {
            if (it is ShowSnackbar) snackbar = it
        }

        viewModel.start(productRemoteId)

        verify(productRepository, times(1)).fetchProduct(productRemoteId)

        assertThat(snackbar).isEqualTo(ShowSnackbar(R.string.product_detail_fetch_product_error))
    }

    @Test
    fun `Do not fetch product from api when not connected`() = test {
        doReturn(product).whenever(productRepository).getProduct(any())
        doReturn(false).whenever(networkStatus).isConnected()

        var snackbar: ShowSnackbar? = null
        viewModel.event.observeForever {
            if (it is ShowSnackbar) snackbar = it
        }

        viewModel.start(productRemoteId)

        verify(productRepository, times(1)).getProduct(productRemoteId)
        verify(productRepository, times(0)).fetchProduct(any())

        assertThat(snackbar).isEqualTo(ShowSnackbar(R.string.offline_error))
    }

    @Test
    fun `Shows and hides product detail skeleton correctly`() = test {
        doReturn(null).whenever(productRepository).getProduct(any())

        val isSkeletonShown = ArrayList<Boolean>()
        viewModel.productDetailViewStateData.observeForever {
            old, new -> new.isSkeletonShown?.takeIfNotEqualTo(old?.isSkeletonShown) { isSkeletonShown.add(it) }
        }

        viewModel.start(productRemoteId)

        assertThat(isSkeletonShown).containsExactly(true, false)
    }

    @Test
    fun `Displays the updated product detail view correctly`() {
        doReturn(product).whenever(productRepository).getProduct(any())

        var productData: ProductDetailViewState? = null
        viewModel.productDetailViewStateData.observeForever { _, new -> productData = new }

        assertThat(productData).isEqualTo(ProductDetailViewState())

        viewModel.start(productRemoteId)
        assertThat(productData).isEqualTo(productWithParameters)

        val updatedDescription = "Updated product description"
        viewModel.updateProductDraft(updatedDescription)

        viewModel.start(productRemoteId)
        assertThat(productData?.product?.description).isEqualTo(updatedDescription)
    }

    @Test
    fun `Displays update menu action if product is edited`() {
        doReturn(product).whenever(productRepository).getProduct(any())

        var productData: ProductDetailViewState? = null
        viewModel.productDetailViewStateData.observeForever { _, new -> productData = new }

        viewModel.start(productRemoteId)
        assertThat(productData?.isProductUpdated).isNull()

        val updatedDescription = "Updated product description"
        viewModel.updateProductDraft(updatedDescription)

        viewModel.start(productRemoteId)
        assertThat(productData?.isProductUpdated).isTrue()
    }

    @Test
    fun `Displays progress dialog when product is edited`() = test {
        doReturn(product).whenever(productRepository).getProduct(any())
        doReturn(false).whenever(productRepository).updateProduct(any())

        val isProgressDialogShown = ArrayList<Boolean>()
        viewModel.productDetailViewStateData.observeForever { old, new ->
            new.isProgressDialogShown?.takeIfNotEqualTo(old?.isProgressDialogShown) {
                isProgressDialogShown.add(it)
            } }

        viewModel.start(productRemoteId)
        viewModel.onUpdateButtonClicked()

        assertThat(isProgressDialogShown).containsExactly(true, false)
    }

    @Test
    fun `Do not update product when not connected`() = test {
        doReturn(product).whenever(productRepository).getProduct(any())
        doReturn(false).whenever(networkStatus).isConnected()

        var snackbar: ShowSnackbar? = null
        viewModel.event.observeForever {
            if (it is ShowSnackbar) snackbar = it
        }

        var productData: ProductDetailViewState? = null
        viewModel.productDetailViewStateData.observeForever { _, new -> productData = new }

        viewModel.start(productRemoteId)
        viewModel.onUpdateButtonClicked()

        verify(productRepository, times(0)).updateProduct(any())
        assertThat(snackbar).isEqualTo(ShowSnackbar(R.string.offline_error))
        assertThat(productData?.isProgressDialogShown).isFalse()
    }

    @Test
    fun `Display error message on update product error`() = test {
        doReturn(product).whenever(productRepository).getProduct(any())
        doReturn(false).whenever(productRepository).updateProduct(any())

        var snackbar: ShowSnackbar? = null
        viewModel.event.observeForever {
            if (it is ShowSnackbar) snackbar = it
        }

        var productData: ProductDetailViewState? = null
        viewModel.productDetailViewStateData.observeForever { _, new -> productData = new }

        viewModel.start(productRemoteId)
        viewModel.onUpdateButtonClicked()

        verify(productRepository, times(1)).updateProduct(any())
        assertThat(snackbar).isEqualTo(ShowSnackbar(R.string.product_detail_update_product_error))
        assertThat(productData?.isProgressDialogShown).isFalse()
    }

    @Test
    fun `Display success message on update product success`() = test {
        doReturn(product).whenever(productRepository).getProduct(any())
        doReturn(true).whenever(productRepository).updateProduct(any())

        var snackbar: ShowSnackbar? = null
        viewModel.event.observeForever {
            if (it is ShowSnackbar) snackbar = it
        }

        var productData: ProductDetailViewState? = null
        viewModel.productDetailViewStateData.observeForever { _, new -> productData = new }

        viewModel.start(productRemoteId)
        viewModel.onUpdateButtonClicked()

        verify(productRepository, times(1)).updateProduct(any())
        verify(productRepository, times(2)).getProduct(productRemoteId)
        verify(productRepository, times(1)).fetchProduct(any())

        assertThat(snackbar).isEqualTo(ShowSnackbar(R.string.product_detail_update_product_success))
        assertThat(productData?.isProgressDialogShown).isFalse()
        assertThat(productData?.isProductUpdated).isFalse()
        assertThat(productData?.product).isEqualTo(product)
    }
}
