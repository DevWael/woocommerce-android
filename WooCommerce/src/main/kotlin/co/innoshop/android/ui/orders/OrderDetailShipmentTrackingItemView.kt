package co.innoshop.android.ui.orders

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.util.ChromeCustomTabUtils
import co.innoshop.android.util.DateUtils
import co.innoshop.android.util.WooLog
import co.innoshop.android.widgets.AppRatingDialog
import kotlinx.android.synthetic.main.order_detail_shipment_tracking_item.view.*
import org.wordpress.android.fluxc.model.WCOrderShipmentTrackingModel

class OrderDetailShipmentTrackingItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.order_detail_shipment_tracking_item, this)
    }

    private var listener: OrderShipmentTrackingActionListener? = null
    private lateinit var uiMessageResolver: UIMessageResolver

    fun initView(
        item: WCOrderShipmentTrackingModel,
        uiMessageResolver: UIMessageResolver,
        isOrderDetail: Boolean,
        shipmentTrackingActionListener: OrderShipmentTrackingActionListener?
    ) {
        this.listener = shipmentTrackingActionListener
        this.uiMessageResolver = uiMessageResolver

        tracking_type.text = item.trackingProvider
        tracking_number.text = item.trackingNumber
        tracking_dateShipped.text = context.getString(
                R.string.order_shipment_tracking_shipped_date,
                DateUtils.getLocalizedLongDateString(context, item.dateShipped))

        tracking_copyNumber.setOnClickListener {
            copyTrackingNumber(item)
        }

        if (isOrderDetail) {
            tracking_btnTrack.visibility = View.VISIBLE
            tracking_btnTrack.setOnClickListener {
                showTrackingOrDeleteOptionPopup(item)
            }
        } else {
            tracking_btnTrack.visibility = View.GONE
            tracking_btnDelete.visibility = View.VISIBLE
            tracking_btnDelete.setOnClickListener {
                AppRatingDialog.incrementInteractions()
                listener?.deleteOrderShipmentTracking(item)
            }
        }
    }

    private fun showTrackingOrDeleteOptionPopup(item: WCOrderShipmentTrackingModel) {
        val popup = PopupMenu(context, tracking_btnTrack)
        popup.menuInflater.inflate(R.menu.menu_order_detail_shipment_tracking_actions, popup.menu)

        /**
         * Track shipment menu option is only displayed if the tracking link
         * is not empty
         */
        if (item.trackingLink.isNotEmpty()) {
            popup.menu.findItem(R.id.menu_track_shipment)?.isVisible = true
            popup.menu.findItem(R.id.menu_track_shipment)?.setOnMenuItemClickListener {
                AnalyticsTracker.track(Stat.ORDER_DETAIL_TRACK_PACKAGE_BUTTON_TAPPED)
                ChromeCustomTabUtils.launchUrl(context, item.trackingLink)
                AppRatingDialog.incrementInteractions()
                true
            }
        }

        popup.menu.findItem(R.id.menu_copy_tracking)?.setOnMenuItemClickListener {
            copyTrackingNumber(item)
            true
        }

        popup.menu.findItem(R.id.menu_delete_shipment)?.setOnMenuItemClickListener {
            listener?.deleteOrderShipmentTracking(item)
            AppRatingDialog.incrementInteractions()
            true
        }

        popup.show()
    }

    fun copyTrackingNumber(item: WCOrderShipmentTrackingModel) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(
                    context.getString(R.string.order_shipment_tracking_number),
                    item.trackingNumber))
            uiMessageResolver.showSnack(R.string.order_shipment_tracking_number_clipboard)
        } catch (e: Exception) {
            WooLog.e(WooLog.T.UTILS, e)
            uiMessageResolver.showSnack(R.string.error_copy_to_clipboard)
        }
    }
}
