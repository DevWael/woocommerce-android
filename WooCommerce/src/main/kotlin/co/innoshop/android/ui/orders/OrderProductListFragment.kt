package co.innoshop.android.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.model.Refund
import co.innoshop.android.tools.ProductImageMap
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.main.MainNavigationRouter
import co.innoshop.android.util.CurrencyFormatter
import kotlinx.android.synthetic.main.fragment_order_product_list.*
import org.wordpress.android.fluxc.model.WCOrderModel
import javax.inject.Inject

class OrderProductListFragment : BaseFragment(), OrderProductListContract.View {
    @Inject lateinit var presenter: OrderProductListContract.Presenter
    @Inject lateinit var currencyFormatter: CurrencyFormatter
    @Inject lateinit var productImageMap: ProductImageMap

    private val navArgs: co.innoshop.android.ui.orders.OrderProductListFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_order_product_list, container, false)
    }

    override fun getFragmentTitle(): String = getString(R.string.orderdetail_orderstatus_ordernum, navArgs.orderNumber)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        presenter.takeView(this)
        presenter.loadOrderDetail(navArgs.orderId)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onDestroyView() {
        presenter.dropView()
        super.onDestroyView()
    }

    override fun showOrderProducts(order: WCOrderModel, refunds: List<Refund>) {
        orderProducts_list.initView(
                orderModel = order,
                productImageMap = productImageMap,
                expanded = true,
                formatCurrencyForDisplay = currencyFormatter.buildBigDecimalFormatter(order.currency),
                orderListener = null,
                productListener = this,
                refunds = refunds
        )
    }

    override fun openOrderProductDetail(remoteProductId: Long) {
        (activity as? MainNavigationRouter)?.showProductDetail(remoteProductId)
    }
}
