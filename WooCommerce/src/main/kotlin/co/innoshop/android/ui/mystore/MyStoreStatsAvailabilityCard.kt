package co.innoshop.android.ui.mystore

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.util.WooAnimUtils
import kotlinx.android.synthetic.main.my_store_stats_availability_notice.view.*

class MyStoreStatsAvailabilityCard @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null)
    : LinearLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.my_store_stats_availability_notice, this)
    }

    fun initView(listener: MyStoreStatsAvailabilityListener) {
        my_store_availability_viewMore.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                WooAnimUtils.fadeIn(my_store_availability_morePanel)
            } else {
                WooAnimUtils.fadeOut(my_store_availability_morePanel)
            }
        }

        btn_try.setOnClickListener {
            AnalyticsTracker.track(Stat.DASHBOARD_NEW_STATS_AVAILABILITY_BANNER_TRY_TAPPED)
            listener.onMyStoreStatsAvailabilityAccepted()
        }

        btn_no_thanks.setOnClickListener {
            AnalyticsTracker.track(Stat.DASHBOARD_NEW_STATS_AVAILABILITY_BANNER_CANCEL_TAPPED)
            listener.onMyStoreStatsAvailabilityRejected()
        }
    }
}
