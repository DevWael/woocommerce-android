package co.innoshop.android.ui.prefs

import co.innoshop.android.ui.base.BasePresenter
import co.innoshop.android.ui.base.BaseView

interface MainSettingsContract {
    interface Presenter : BasePresenter<View> {
        fun getUserDisplayName(): String
        fun getStoreDomainName(): String
        fun hasMultipleStores(): Boolean
    }

    interface View : BaseView<Presenter> {
        fun showDeviceAppNotificationSettings()
    }
}
