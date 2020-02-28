package co.innoshop.android.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import co.innoshop.android.ui.main.MainActivity.NavigationResult
import kotlin.properties.Delegates

/**
 * Used for passing back some result from a fragment when using the Navigation component
 * It replaces the startActivityForResult() & setResult() call, since the Navigation component uses a single activity.
 */
fun FragmentActivity.navigateBackWithResult(requestCode: Int, result: Bundle, @IdRes navHostId: Int, @IdRes dest: Int) {
    val childFragmentManager = supportFragmentManager.findFragmentById(navHostId)?.childFragmentManager
    var backStackListener: FragmentManager.OnBackStackChangedListener by Delegates.notNull()
    backStackListener = FragmentManager.OnBackStackChangedListener {
        (childFragmentManager?.fragments?.get(0) as? NavigationResult)?.onNavigationResult(requestCode, result)
        childFragmentManager?.removeOnBackStackChangedListener(backStackListener)
    }
    childFragmentManager?.addOnBackStackChangedListener(backStackListener)
    findNavController(navHostId).popBackStack(dest, false)
}
