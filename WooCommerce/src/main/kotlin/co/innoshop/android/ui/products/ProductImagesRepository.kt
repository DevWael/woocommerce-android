package co.innoshop.android.ui.products

import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.model.Product
import co.innoshop.android.model.toAppModel
import co.innoshop.android.tools.SelectedSite
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

@OpenClassOnDebug
class ProductImagesRepository @Inject constructor(
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite
) {
    fun getProduct(remoteProductId: Long): Product? =
            productStore.getProductByRemoteId(selectedSite.get(), remoteProductId)?.toAppModel()
}
