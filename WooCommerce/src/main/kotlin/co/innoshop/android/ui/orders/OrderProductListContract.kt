package co.innoshop.android.ui.orders

import co.innoshop.android.model.Refund
import co.innoshop.android.ui.base.BasePresenter
import co.innoshop.android.ui.base.BaseView
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.order.OrderIdentifier

interface OrderProductListContract {
    interface Presenter : BasePresenter<View> {
        fun getOrderDetailFromDb(orderIdentifier: OrderIdentifier): WCOrderModel?
        fun loadOrderDetail(orderIdentifier: OrderIdentifier)
    }

    interface View : BaseView<Presenter>, OrderProductActionListener {
        fun showOrderProducts(order: WCOrderModel, refunds: List<Refund>)
    }
}
