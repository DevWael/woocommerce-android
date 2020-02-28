package co.innoshop.android.ui.orders

import co.innoshop.android.model.Order

/**
 * Interface for handling order refund actions from a child fragment.
 */
interface OrderRefundActionListener {
    fun issueOrderRefund(order: Order)
    fun showRefundDetail(orderId: Long, refundId: Long)
}
