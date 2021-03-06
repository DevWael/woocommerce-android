package co.innoshop.android.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.innoshop.android.AppPrefs
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat.SETTINGS_BETA_FEATURES_NEW_STATS_UI_TOGGLED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.SETTINGS_BETA_FEATURES_PRODUCTS_TOGGLED
import co.innoshop.android.ui.prefs.MainSettingsFragment.AppSettingsListener
import co.innoshop.android.util.AnalyticsUtils
import kotlinx.android.synthetic.main.fragment_settings_beta.*

class BetaFeaturesFragment : Fragment() {
    companion object {
        const val TAG = "beta-features"
    }

    private lateinit var settingsListener: AppSettingsListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_beta, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is AppSettingsListener) {
            settingsListener = activity as AppSettingsListener
        } else {
            throw ClassCastException(context.toString() + " must implement AppSettingsListener")
        }

        // display the Stats section only if the wc-admin is installed/active on a site
        if (AppPrefs.isUsingV4Api()) {
            switchStatsV4UI.visibility = View.VISIBLE
            switchStatsV4UI.isChecked = AppPrefs.isV4StatsUIEnabled()
            switchStatsV4UI.setOnCheckedChangeListener { _, isChecked ->
                AnalyticsTracker.track(
                        SETTINGS_BETA_FEATURES_NEW_STATS_UI_TOGGLED, mapOf(
                        AnalyticsTracker.KEY_STATE to AnalyticsUtils.getToggleStateLabel(switchStatsV4UI.isChecked)))
                settingsListener.onV4StatsOptionChanged(isChecked)
            }
        } else {
            switchStatsV4UI.visibility = View.GONE
        }

        switchProductsUI.isChecked = AppPrefs.isProductsFeatureEnabled()
        switchProductsUI.setOnCheckedChangeListener { _, isChecked ->
            AnalyticsTracker.track(
                    SETTINGS_BETA_FEATURES_PRODUCTS_TOGGLED, mapOf(
                    AnalyticsTracker.KEY_STATE to AnalyticsUtils.getToggleStateLabel(switchProductsUI.isChecked)))
            settingsListener.onProductsFeatureOptionChanged(isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)

        activity?.setTitle(R.string.beta_features)
    }
}
