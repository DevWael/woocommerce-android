package co.innoshop.android.ui.prefs

import android.content.Context
import co.innoshop.android.ui.base.BasePresenter
import co.innoshop.android.ui.base.BaseView

interface PrivacySettingsContract {
    interface Presenter : BasePresenter<View> {
        fun getSendUsageStats(): Boolean
        fun setSendUsageStats(sendUsageStats: Boolean)
        fun getCrashReportingEnabled(): Boolean
        fun setCrashReportingEnabled(context: Context, enabled: Boolean)
    }

    interface View : BaseView<Presenter> {
        fun showCookiePolicy()
        fun showPrivacyPolicy()
    }
}
