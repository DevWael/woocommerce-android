package co.innoshop.android.ui.orders

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.analytics.AnalyticsTracker.Stat.ORDER_DETAIL_PRODUCT_TAPPED
import co.innoshop.android.model.Order
import co.innoshop.android.model.Refund
import co.innoshop.android.model.toAppModel
import co.innoshop.android.tools.ProductImageMap
import co.innoshop.android.ui.products.ProductHelper
import co.innoshop.android.widgets.AlignedDividerDecoration
import kotlinx.android.synthetic.main.order_detail_product_list.view.*
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.CoreOrderStatus
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP

class OrderDetailProductListView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null)
    : ConstraintLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.order_detail_product_list, this)
    }
    private lateinit var divider: AlignedDividerDecoration
    private lateinit var viewAdapter: ProductListAdapter
    private var isExpanded = false

    /**
     * Initialize and format this view.
     *
     * @param [orderModel] The order containing the product list to display.
     * @param [productImageMap] Images for products.
     * @param [expanded] If true, expanded view will be shown, else collapsed view.
     * @param [formatCurrencyForDisplay] Function to use for formatting currencies for display.
     * @param [orderListener] Listener for routing order click actions. If null, the buttons will be hidden.
     * @param [productListener] Listener for routing product click actions.
     * @param [refunds] List of refunds order the order.
     */
    fun initView(
        orderModel: WCOrderModel,
        productImageMap: ProductImageMap,
        expanded: Boolean,
        formatCurrencyForDisplay: (BigDecimal) -> String,
        orderListener: OrderActionListener? = null,
        productListener: OrderProductActionListener? = null,
        refunds: List<Refund>
    ) {
        isExpanded = expanded

        divider = AlignedDividerDecoration(context,
                DividerItemDecoration.VERTICAL, R.id.productInfo_name, clipToMargin = false)

        ContextCompat.getDrawable(context, R.drawable.list_divider)?.let { drawable ->
            divider.setDrawable(drawable)
        }

        val viewManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        val order = orderModel.toAppModel()
        val leftoverProducts = order.getMaxRefundQuantities(refunds).filter { it.value > 0 }
        val filteredItems = order.items.filter { leftoverProducts.contains(it.uniqueId) }
                .map {
                    val newQuantity = leftoverProducts[it.uniqueId]
                    it.copy(
                            quantity = newQuantity ?: error("Missing product"),
                            total = it.price.times(newQuantity.toBigDecimal()),
                            totalTax = it.totalTax.divide(it.quantity.toBigDecimal(), 2, HALF_UP)
                    )
                }

        viewAdapter = ProductListAdapter(
                filteredItems,
                productImageMap,
                formatCurrencyForDisplay,
                isExpanded,
                productListener
        )

        orderListener?.let {
            if (orderModel.status == CoreOrderStatus.PROCESSING.value) {
                productList_btnFulfill.visibility = View.VISIBLE
                productList_btnDetails.visibility = View.GONE
                productList_btnDetails.setOnClickListener(null)
                productList_btnFulfill.setOnClickListener {
                    AnalyticsTracker.track(Stat.ORDER_DETAIL_FULFILL_ORDER_BUTTON_TAPPED)
                    orderListener.openOrderFulfillment(orderModel)
                }
            } else {
                productList_btnFulfill.visibility = View.GONE
                productList_btnDetails.visibility = View.VISIBLE
                productList_btnDetails.setOnClickListener {
                    AnalyticsTracker.track(Stat.ORDER_DETAIL_PRODUCT_DETAIL_BUTTON_TAPPED)
                    orderListener.openOrderProductList(orderModel)
                }
                productList_btnFulfill.setOnClickListener(null)
            }
        } ?: hideButtons()

        productList_products.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            adapter = viewAdapter
        }

        if (isExpanded) {
            productList_products.addItemDecoration(divider)
        }
    }

    fun updateView(order: WCOrderModel, listener: OrderActionListener? = null) {
        listener?.let {
            if (order.status == CoreOrderStatus.PROCESSING.value) {
                productList_btnFulfill.visibility = View.VISIBLE
                productList_btnDetails.visibility = View.GONE
                productList_btnDetails.setOnClickListener(null)
                productList_btnFulfill.setOnClickListener {
                    listener.openOrderFulfillment(order)
                }
            } else {
                productList_btnFulfill.visibility = View.GONE
                productList_btnDetails.visibility = View.VISIBLE
                productList_btnDetails.setOnClickListener {
                    listener.openOrderProductList(order)
                }
                productList_btnFulfill.setOnClickListener(null)
            }
        } ?: hideButtons()

        if (isExpanded) {
            productList_products.addItemDecoration(divider)
        }
    }

    // called when a product is fetched to ensure we show the correct product image
    fun refreshProductImages() {
        if (::viewAdapter.isInitialized) {
            viewAdapter.notifyDataSetChanged()
        }
    }

    private fun hideButtons() {
        productList_btnFulfill.setOnClickListener(null)
        productList_btnDetails.setOnClickListener(null)
        productList_btnFulfill.visibility = View.GONE
        productList_btnDetails.visibility = View.GONE
    }

    class ProductListAdapter(
        private val orderItems: List<Order.Item>,
        private val productImageMap: ProductImageMap,
        private val formatCurrencyForDisplay: (BigDecimal) -> String,
        private var isExpanded: Boolean,
        private val productListener: OrderProductActionListener?
    ) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
        class ViewHolder(val view: OrderDetailProductItemView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: OrderDetailProductItemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.order_detail_product_list_item, parent, false)
                    as OrderDetailProductItemView
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = orderItems[position]
            val productId = ProductHelper.productOrVariationId(item.productId, item.variationId)
            val productImage = productImageMap.get(productId)
            holder.view.initView(orderItems[position], productImage, isExpanded, formatCurrencyForDisplay)
            holder.view.setOnClickListener {
                AnalyticsTracker.track(ORDER_DETAIL_PRODUCT_TAPPED)
                productListener?.openOrderProductDetail(productId)
            }
        }

        override fun getItemCount() = orderItems.size
    }
}
