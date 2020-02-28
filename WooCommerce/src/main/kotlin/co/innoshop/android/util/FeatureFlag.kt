package co.innoshop.android.util

import android.content.Context
import co.innoshop.android.AppPrefs
import co.innoshop.android.BuildConfig

/**
 * "Feature flags" are used to hide in-progress features from release versions
 */
enum class FeatureFlag {
    PRODUCT_RELEASE_TEASER,
    ADD_EDIT_PRODUCT_RELEASE_1,
    DB_DOWNGRADE,
    PRODUCT_IMAGE_CHOOSER;
    fun isEnabled(context: Context? = null): Boolean {
        return when (this) {
            ADD_EDIT_PRODUCT_RELEASE_1 -> co.innoshop.android.BuildConfig.DEBUG
            PRODUCT_RELEASE_TEASER -> AppPrefs.isProductsFeatureEnabled()
            PRODUCT_IMAGE_CHOOSER -> co.innoshop.android.BuildConfig.DEBUG && AppPrefs.isProductsFeatureEnabled()
            DB_DOWNGRADE -> {
                co.innoshop.android.BuildConfig.DEBUG || context != null && PackageUtils.isBetaBuild(context)
            }
        }
    }
}
