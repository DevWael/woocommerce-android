package co.innoshop.android.ui.mystore

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import co.innoshop.android.AppUrls
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.util.ChromeCustomTabUtils
import kotlinx.android.synthetic.main.my_store_stats_reverted_notice.view.*

/**
 * Dashboard card that displays a reverted notice message if the WooCommerce Admin plugin
 * is disabled/uninstalled from a site but the v4 stats is already displayed to the user
 */
class MyStoreStatsRevertedNoticeCard @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null)
    : LinearLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.my_store_stats_reverted_notice, this)
    }

    fun initView(listener: MyStoreStatsAvailabilityListener) {
        btn_learn_more.setOnClickListener {
            AnalyticsTracker.track(Stat.DASHBOARD_NEW_STATS_REVERTED_BANNER_LEARN_MORE_TAPPED)
            ChromeCustomTabUtils.launchUrl(context, AppUrls.WOOCOMMERCE_ADMIN_PLUGIN)
        }

        btn_dismiss.setOnClickListener {
            AnalyticsTracker.track(Stat.DASHBOARD_NEW_STATS_REVERTED_BANNER_DISMISS_TAPPED)
            listener.onMyStoreStatsRevertedNoticeCardDismissed()
        }
    }
}
