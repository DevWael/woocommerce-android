package co.innoshop.android.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import co.innoshop.android.R
import co.innoshop.android.extensions.active
import co.innoshop.android.ui.base.TopLevelFragment
import co.innoshop.android.ui.main.BottomNavigationPosition.DASHBOARD
import co.innoshop.android.ui.main.BottomNavigationPosition.ORDERS
import co.innoshop.android.ui.main.BottomNavigationPosition.REVIEWS
import co.innoshop.android.util.FeatureFlag
import co.innoshop.android.util.WooAnimUtils
import co.innoshop.android.util.WooAnimUtils.Duration
import org.wordpress.android.util.DisplayUtils
import kotlin.math.min

class MainBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr),
        OnNavigationItemSelectedListener, OnNavigationItemReselectedListener {
    private lateinit var navAdapter: NavAdapter
    private lateinit var fragmentManager: FragmentManager
    private lateinit var listener: MainNavigationListener
    private lateinit var reviewsBadgeView: View
    private lateinit var ordersBadgeView: View
    private lateinit var ordersBadgeTextView: TextView

    companion object {
        private var previousNavPos: BottomNavigationPosition? = null
        private const val ORDER_BADGE_MAX = 99
        private const val ORDER_BADGE_MAX_LABEL = "$ORDER_BADGE_MAX+"
    }

    interface MainNavigationListener {
        fun onNavItemSelected(navPos: BottomNavigationPosition)
        fun onNavItemReselected(navPos: BottomNavigationPosition)
    }

    var currentPosition: BottomNavigationPosition
        get() = findNavigationPositionById(selectedItemId)
        set(navPos) = updateCurrentPosition(navPos)

    fun init(fm: FragmentManager, listener: MainNavigationListener) {
        this.fragmentManager = fm
        this.listener = listener

        refreshProductsTab()

        navAdapter = NavAdapter()
        addTopDivider()

        // set up the bottom bar and add the badge views
        val menuView = getChildAt(0) as BottomNavigationMenuView
        val inflater = LayoutInflater.from(context)

        val ordersItemView = menuView.getChildAt(ORDERS.position) as BottomNavigationItemView
        ordersBadgeView = inflater.inflate(R.layout.order_badge_view, menuView, false)
        ordersBadgeTextView = ordersBadgeView.findViewById(R.id.textOrderCount)
        ordersItemView.addView(ordersBadgeView)

        val reviewsItemView = menuView.getChildAt(REVIEWS.position) as BottomNavigationItemView
        reviewsBadgeView = inflater.inflate(R.layout.notification_badge_view, menuView, false)
        reviewsItemView.addView(reviewsBadgeView)

        assignNavigationListeners(true)

        // Default to the dashboard position
        active(DASHBOARD.position)
    }

    private fun refreshProductsTab() {
        menu.findItem(R.id.products)?.isVisible = FeatureFlag.PRODUCT_RELEASE_TEASER.isEnabled()
        detectLabelVisibilityMode()
    }

    /**
     * When we changed the background to white, the top shadow provided by BottomNavigationView wasn't
     * dark enough to provide enough separation between the bar and the content above it. For this
     * reason we add a darker top divider here.
     */
    private fun addTopDivider() {
        val divider = View(context)
        val dividerColor = ContextCompat.getColor(context, R.color.list_divider)
        divider.setBackgroundColor(dividerColor)

        val dividerHeight = resources.getDimensionPixelSize(R.dimen.bottomm_nav_top_border)
        val dividerParams = LayoutParams(LayoutParams.MATCH_PARENT, dividerHeight)
        divider.layoutParams = dividerParams

        addView(divider)
    }

    /**
     * We want to override the bottom nav's default behavior of only showing labels for the active tab when
     * more than three tabs are showing, but we only want to do this if we know it won't cause any of the
     * tabs to wrap to more than one line.
     */
    @SuppressLint("PrivateResource")
    private fun detectLabelVisibilityMode() {
        // default to showing labels for all tabs
        labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED

        var numVisibleItems = 0
        for (index in 0 until menu.size()) {
            if (menu.getItem(index).isVisible) {
                numVisibleItems++
            }
        }

        // determine the width of a navbar item
        val displayWidth = DisplayUtils.getDisplayPixelWidth(context)
        val itemMargin = resources.getDimensionPixelSize(R.dimen.design_bottom_navigation_margin)
        val itemMaxWidth = resources.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width)
        val itemWidth = min(itemMaxWidth, (displayWidth / numVisibleItems) - (itemMargin * 3))

        // create a paint object whose text size matches the bottom navigation active text size - note that
        // we have to use the active size since it's 2sp larger than inactive
        val textPaint = Paint().also {
            it.textSize = resources.getDimension(R.dimen.design_bottom_navigation_active_text_size)
        }

        // iterate through the menu items and determine whether they can all fit their space - if any of them
        // can't, we revert to LABEL_VISIBILITY_AUTO
        val bounds = Rect()
        for (index in 0 until menu.size()) {
            val title = menu.getItem(index).title.toString()
            textPaint.getTextBounds(title, 0, title.length, bounds)
            if (bounds.width() > itemWidth) {
                labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_AUTO
                break
            }
        }
    }

    fun getFragment(navPos: BottomNavigationPosition): TopLevelFragment = navAdapter.getFragment(navPos)

    fun updatePositionAndDeferInit(navPos: BottomNavigationPosition) {
        updateCurrentPosition(navPos, true)
    }

    /**
     * For use when restoring the navigation bar after the host activity
     * state has been restored.
     */
    fun restoreSelectedItemState(itemId: Int) {
        assignNavigationListeners(false)
        selectedItemId = itemId
        assignNavigationListeners(true)
    }

    fun showReviewsBadge(show: Boolean) {
        with(reviewsBadgeView) {
            if (show && visibility != View.VISIBLE) {
                WooAnimUtils.fadeIn(this, Duration.MEDIUM)
            } else if (!show && visibility == View.VISIBLE) {
                WooAnimUtils.fadeOut(this, Duration.MEDIUM)
            }
        }
    }

    fun showOrderBadge(count: Int) {
        if (count <= 0) {
            hideOrderBadge()
            return
        }

        val label = if (count > ORDER_BADGE_MAX) ORDER_BADGE_MAX_LABEL else count.toString()
        ordersBadgeTextView.text = label
        if (ordersBadgeView.visibility != View.VISIBLE) {
            WooAnimUtils.fadeIn(ordersBadgeView, Duration.MEDIUM)
        }
    }

    /**
     * If the order badge is showing, hide the TextView which shows the order count
     */
    fun hideOrderBadgeCount() {
        if (ordersBadgeView.visibility == View.VISIBLE) {
            ordersBadgeTextView.text = null
        }
    }

    fun hideOrderBadge() {
        if (ordersBadgeView.visibility == View.VISIBLE) {
            WooAnimUtils.fadeOut(ordersBadgeView, Duration.MEDIUM)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navPos = findNavigationPositionById(item.itemId)
        currentPosition = navPos

        listener.onNavItemSelected(navPos)
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val navPos = findNavigationPositionById(item.itemId)
        listener.onNavItemReselected(navPos)
    }

    /**
     * Replaces the fragment in [DASHBOARD] based on whether the revenue stats is available
     */
    fun replaceStatsFragment() {
        val fragment = fragmentManager.findFragment(currentPosition)
        val tag = currentPosition.getTag()

        // replace the fragment
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, tag)
                .show(fragment)
                .commitAllowingStateLoss()

        // update the correct fragment in the navigation adapter
        navAdapter.replaceFragment(currentPosition, fragment)
    }

    private fun updateCurrentPosition(navPos: BottomNavigationPosition, deferInit: Boolean = false) {
        assignNavigationListeners(false)
        try {
            selectedItemId = navPos.id
        } finally {
            assignNavigationListeners(true)
        }

        val fragment = navAdapter.getFragment(navPos)
        fragment.deferInit = deferInit

        // hide previous fragment if it exists
        val fragmentTransaction = fragmentManager.beginTransaction()
        previousNavPos?.let {
            val previousFragment = navAdapter.getFragment(it)
            fragmentTransaction.hide(previousFragment)
        }

        // add the fragment if it hasn't been added yet
        val tag = navPos.getTag()
        if (fragmentManager.findFragmentByTag(tag) == null) {
            fragmentTransaction.add(R.id.container, fragment, tag)
        }

        // show the new fragment
        fragmentTransaction.show(fragment)
        fragmentTransaction.commitAllowingStateLoss()

        previousNavPos = navPos
    }

    private fun assignNavigationListeners(assign: Boolean) {
        setOnNavigationItemSelectedListener(if (assign) this else null)
        setOnNavigationItemReselectedListener(if (assign) this else null)
    }

    /**
     * Extension function for retrieving an existing fragment from the [FragmentManager]
     * if one exists, if not, create a new instance of the requested fragment.
     */
    private fun FragmentManager.findFragment(position: BottomNavigationPosition): TopLevelFragment {
        return (findFragmentByTag(position.getTag()) ?: position.createFragment()) as TopLevelFragment
    }

    // region Private Classes
    private inner class NavAdapter {
        private val fragments = SparseArray<TopLevelFragment>(BottomNavigationPosition.values().size)

        internal fun getFragment(navPos: BottomNavigationPosition): TopLevelFragment {
            fragments[navPos.position]?.let {
                return it
            }

            val fragment = fragmentManager.findFragment(navPos)
            fragments.put(navPos.position, fragment)
            return fragment
        }

        internal fun replaceFragment(navPos: BottomNavigationPosition, fragment: TopLevelFragment) =
                fragments.put(navPos.position, fragment)
    }
    // endregion
}
