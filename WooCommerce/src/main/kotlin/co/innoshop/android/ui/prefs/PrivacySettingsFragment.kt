package co.innoshop.android.ui.prefs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.innoshop.android.AppUrls
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRIVACY_SETTINGS_COLLECT_INFO_TOGGLED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRIVACY_SETTINGS_CRASH_REPORTING_TOGGLED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRIVACY_SETTINGS_PRIVACY_POLICY_LINK_TAPPED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRIVACY_SETTINGS_SHARE_INFO_LINK_TAPPED
import co.innoshop.android.analytics.AnalyticsTracker.Stat.PRIVACY_SETTINGS_THIRD_PARTY_TRACKING_INFO_LINK_TAPPED
import co.innoshop.android.util.AnalyticsUtils
import co.innoshop.android.util.ChromeCustomTabUtils
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_settings_privacy.*
import javax.inject.Inject

class PrivacySettingsFragment : androidx.fragment.app.Fragment(), PrivacySettingsContract.View {
    companion object {
        const val TAG = "privacy-settings"
    }

    @Inject lateinit var presenter: PrivacySettingsContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_privacy, container, false)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.takeView(this)

        switchSendStats.isChecked = presenter.getSendUsageStats()
        switchSendStats.setOnClickListener {
            AnalyticsTracker.track(PRIVACY_SETTINGS_COLLECT_INFO_TOGGLED, mapOf(
                    AnalyticsTracker.KEY_STATE to AnalyticsUtils.getToggleStateLabel(switchSendStats.isChecked)))
            presenter.setSendUsageStats(switchSendStats.isChecked)
        }

        buttonLearnMore.setOnClickListener {
            AnalyticsTracker.track(PRIVACY_SETTINGS_SHARE_INFO_LINK_TAPPED)
            showCookiePolicy()
        }
        buttonPrivacyPolicy.setOnClickListener {
            AnalyticsTracker.track(PRIVACY_SETTINGS_PRIVACY_POLICY_LINK_TAPPED)
            showPrivacyPolicy()
        }
        buttonTracking.setOnClickListener {
            AnalyticsTracker.track(PRIVACY_SETTINGS_THIRD_PARTY_TRACKING_INFO_LINK_TAPPED)
            showCookiePolicy()
        }

        switchCrashReporting.isChecked = presenter.getCrashReportingEnabled()
        switchCrashReporting.setOnClickListener {
            AnalyticsTracker.track(
                    PRIVACY_SETTINGS_CRASH_REPORTING_TOGGLED, mapOf(
                    AnalyticsTracker.KEY_STATE to AnalyticsUtils.getToggleStateLabel(switchCrashReporting.isChecked)))
            presenter.setCrashReportingEnabled(activity!!, switchCrashReporting.isChecked)
        }
    }

    override fun onDestroyView() {
        presenter.dropView()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)

        activity?.setTitle(R.string.privacy_settings)
    }

    override fun onStart() {
        super.onStart()
        ChromeCustomTabUtils.connect(
                activity as Context,
                AppUrls.AUTOMATTIC_PRIVACY_POLICY,
                arrayOf(AppUrls.AUTOMATTIC_COOKIE_POLICY)
        )
    }

    override fun onStop() {
        super.onStop()
        ChromeCustomTabUtils.disconnect(activity as Context)
    }

    override fun showCookiePolicy() {
        ChromeCustomTabUtils.launchUrl(activity as Context, AppUrls.AUTOMATTIC_COOKIE_POLICY)
    }

    override fun showPrivacyPolicy() {
        ChromeCustomTabUtils.launchUrl(activity as Context, AppUrls.AUTOMATTIC_PRIVACY_POLICY)
    }
}
