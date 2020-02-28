package co.innoshop.android.ui.main

import co.innoshop.android.ui.orders.OrderDetailFragment

/**
 * Helper method to update the network status for the current fragment to test
 * offline scenarios
 */
fun MainActivityTestRule.getOrderDetailFragment(): OrderDetailFragment? {
    return activity.supportFragmentManager.primaryNavigationFragment?.let { navFragment ->
        navFragment.childFragmentManager.fragments[0] as? OrderDetailFragment
    }
}
