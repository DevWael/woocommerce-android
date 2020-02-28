package co.innoshop.android.ui.orders

import androidx.annotation.StringRes
import co.innoshop.android.ui.base.BasePresenter
import co.innoshop.android.ui.base.BaseView
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.WCOrderShipmentProviderModel
import org.wordpress.android.fluxc.model.order.OrderIdentifier

interface AddOrderTrackingProviderListContract {
    interface Presenter : BasePresenter<View> {
        var orderModel: WCOrderModel?
        var isShipmentTrackingProviderListFetched: Boolean
        fun loadOrderDetailFromDb(orderIdentifier: OrderIdentifier): WCOrderModel?
        fun loadShipmentTrackingProviders(orderIdentifier: OrderIdentifier?)
        fun loadShipmentTrackingProvidersFromDb()
        fun getShipmentTrackingProvidersFromDb(): List<WCOrderShipmentProviderModel>
        fun fetchShipmentTrackingProvidersFromApi(order: WCOrderModel)
        fun loadStoreCountryFromDb(): String?
    }

    interface View : BaseView<Presenter> {
        fun getCountryName(): String?
        fun showSkeleton(show: Boolean)
        fun showProviderListErrorSnack(@StringRes stringResId: Int)
        fun showProviderList(providers: List<WCOrderShipmentProviderModel>)
    }
}
