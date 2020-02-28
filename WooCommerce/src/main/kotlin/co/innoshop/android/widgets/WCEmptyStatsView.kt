package co.innoshop.android.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import co.innoshop.android.R
import co.innoshop.android.util.DateUtils
import kotlinx.android.synthetic.main.dashboard_main_stats_row.view.*
import kotlinx.android.synthetic.main.wc_empty_stats_view.view.*
import java.util.Date

class WCEmptyStatsView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : LinearLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.wc_empty_stats_view, this)
    }

    fun updateVisitorCount(visits: Int) {
        visitors_value.text = visits.toString()
        empty_stats_view_date_title.text = DateUtils.getDayOfWeekWithMonthAndDayFromDate(Date())

        // The empty view is only shown when there are no orders, which means the revenue is also 0
        orders_value.text = "0"
        revenue_value.text = "0"
    }
}
