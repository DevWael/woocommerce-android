package co.innoshop.android.ui.refunds

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.innoshop.android.viewmodel.SavedStateWithArgs
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.R
import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.model.Order
import co.innoshop.android.model.Refund
import co.innoshop.android.model.toAppModel
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.ui.refunds.RefundProductListAdapter.RefundListItem
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.viewmodel.LiveDataDelegate
import co.innoshop.android.viewmodel.ResourceProvider
import co.innoshop.android.viewmodel.ScopedViewModel
import kotlinx.android.parcel.Parcelize
import org.wordpress.android.fluxc.model.order.OrderIdentifier
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WCRefundStore
import java.math.BigDecimal
import co.innoshop.android.extensions.calculateTotals
import co.innoshop.android.extensions.isCashPayment

@OpenClassOnDebug
class RefundDetailViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers,
    orderStore: WCOrderStore,
    private val selectedSite: SelectedSite,
    private val currencyFormatter: CurrencyFormatter,
    private val resourceProvider: ResourceProvider,
    private val refundStore: WCRefundStore
) : ScopedViewModel(savedState, dispatchers) {
    final val viewStateData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateData

    private val _refundItems = MutableLiveData<List<RefundListItem>>()
    final val refundItems: LiveData<List<RefundListItem>> = _refundItems

    private lateinit var formatCurrency: (BigDecimal) -> String

    private val navArgs: co.innoshop.android.ui.refunds.RefundDetailFragmentArgs by savedState.navArgs()

    init {
        val orderModel = orderStore.getOrderByIdentifier(OrderIdentifier(selectedSite.get().id, navArgs.orderId))
        orderModel?.toAppModel()?.let { order ->
            formatCurrency = currencyFormatter.buildBigDecimalFormatter(order.currency)
            if (navArgs.refundId > 0) {
                refundStore.getRefund(selectedSite.get(), navArgs.orderId, navArgs.refundId)
                    ?.toAppModel()?.let { refund ->
                        displayRefundDetails(refund, order)
                    }
            } else {
                val refunds = refundStore.getAllRefunds(selectedSite.get(), navArgs.orderId).map { it.toAppModel() }
                displayRefundedProducts(order, refunds)
            }
        }
    }

    private fun displayRefundedProducts(order: Order, refunds: List<Refund>) {
        val groupedRefunds = refunds.flatMap { it.items }.groupBy { it.uniqueId }
        val refundedProducts = groupedRefunds.keys.mapNotNull { id ->
            order.items.firstOrNull { it.uniqueId == id }?.let { item ->
                groupedRefunds[id]?.sumBy { it.quantity }?.let { quantity ->
                    RefundListItem(item, quantity = quantity)
                }
            }
        }

        viewState = viewState.copy(
            currency = order.currency,
            screenTitle = resourceProvider.getString(R.string.orderdetail_refunded_products),
            areItemsVisible = true,
            areDetailsVisible = false
        )

        _refundItems.value = refundedProducts
    }

    private fun displayRefundDetails(refund: Refund, order: Order) {
        if (refund.items.isNotEmpty()) {
            val items = refund.items.map { refundItem ->
                RefundListItem(
                    order.items.first { it.uniqueId == refundItem.uniqueId },
                    quantity = refundItem.quantity
                )
            }

            val (subtotal, taxes) = items.calculateTotals()
            viewState = viewState.copy(
                    currency = order.currency,
                    areItemsVisible = true,
                    subtotal = formatCurrency(subtotal),
                    taxes = formatCurrency(taxes)
            )

            _refundItems.value = items
        } else {
            viewState = viewState.copy(areItemsVisible = false)
        }

        viewState = viewState.copy(
                screenTitle = "${resourceProvider.getString(R.string.order_refunds_refund)} #${refund.id}",
                refundAmount = formatCurrency(refund.amount),
                refundMethod = resourceProvider.getString(
                        R.string.order_refunds_refunded_via,
                        getRefundMethod(order, refund)),
                refundReason = refund.reason,
                areDetailsVisible = true
        )
    }

    private fun getRefundMethod(order: Order, refund: Refund): String {
        val manualRefund = resourceProvider.getString(R.string.order_refunds_manual_refund)
        return if (order.paymentMethodTitle.isNotBlank() &&
                (refund.automaticGatewayRefund || order.paymentMethod.isCashPayment))
            order.paymentMethodTitle
        else if (order.paymentMethodTitle.isNotBlank())
            "$manualRefund - ${order.paymentMethodTitle}"
        else
            manualRefund
    }

    @Parcelize
    data class ViewState(
        val screenTitle: String? = null,
        val refundAmount: String? = null,
        val subtotal: String? = null,
        val taxes: String? = null,
        val refundMethod: String? = null,
        val refundReason: String? = null,
        val currency: String? = null,
        val areItemsVisible: Boolean? = null,
        val areDetailsVisible: Boolean? = null
    ) : Parcelable

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<RefundDetailViewModel>
}
