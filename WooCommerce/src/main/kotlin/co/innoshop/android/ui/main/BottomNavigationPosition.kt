package co.innoshop.android.ui.main

import co.innoshop.android.AppPrefs
import co.innoshop.android.R
import co.innoshop.android.ui.base.TopLevelFragment
import co.innoshop.android.ui.dashboard.DashboardFragment
import co.innoshop.android.ui.mystore.MyStoreFragment
import co.innoshop.android.ui.products.ProductListFragment
import co.innoshop.android.ui.reviews.ReviewListFragment
import co.innoshop.android.ui.orders.list.OrderListFragment

enum class BottomNavigationPosition(val position: Int, val id: Int) {
    DASHBOARD(0, R.id.dashboard),
    ORDERS(1, R.id.orders),
    PRODUCTS(2, R.id.products),
    REVIEWS(3, R.id.reviews)
}

fun findNavigationPositionById(id: Int): BottomNavigationPosition = when (id) {
    BottomNavigationPosition.DASHBOARD.id -> BottomNavigationPosition.DASHBOARD
    BottomNavigationPosition.ORDERS.id -> BottomNavigationPosition.ORDERS
    BottomNavigationPosition.PRODUCTS.id -> BottomNavigationPosition.PRODUCTS
    BottomNavigationPosition.REVIEWS.id -> BottomNavigationPosition.REVIEWS
    else -> BottomNavigationPosition.DASHBOARD
}

fun BottomNavigationPosition.getTag(): String = when (this) {
    BottomNavigationPosition.DASHBOARD -> getMyStoreTag()
    BottomNavigationPosition.ORDERS -> OrderListFragment.TAG
    BottomNavigationPosition.PRODUCTS -> ProductListFragment.TAG
    BottomNavigationPosition.REVIEWS -> ReviewListFragment.TAG
}

fun BottomNavigationPosition.createFragment(): TopLevelFragment = when (this) {
    BottomNavigationPosition.DASHBOARD -> createMyStoreFragment()
    BottomNavigationPosition.ORDERS -> OrderListFragment.newInstance()
    BottomNavigationPosition.PRODUCTS -> ProductListFragment.newInstance()
    BottomNavigationPosition.REVIEWS -> ReviewListFragment.newInstance()
}

/**
 * Temp method that returns
 * [DashboardFragment] if v4 stats api is not supported for the site
 * [MyStoreFragment] if v4 stats api is supported for the site
 */
private fun createMyStoreFragment(): TopLevelFragment {
    return if (AppPrefs.isV4StatsUISupported()) {
        MyStoreFragment.newInstance()
    } else {
        DashboardFragment.newInstance()
    }
}

/**
 * Temp method that returns
 * [DashboardFragment.TAG] if v4 stats api is not supported for the site
 * [MyStoreFragment.TAG] if v4 stats api is supported for the site
 */
private fun getMyStoreTag(): String {
    return if (AppPrefs.isV4StatsUISupported()) {
        MyStoreFragment.TAG
    } else {
        DashboardFragment.TAG
    }
}
