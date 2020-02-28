package co.innoshop.android.di

import android.app.Application
import co.innoshop.android.push.FCMServiceModule
import co.innoshop.android.ui.login.LoginAnalyticsModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import org.wordpress.android.fluxc.module.ReleaseBaseModule
import org.wordpress.android.fluxc.module.ReleaseNetworkModule
import org.wordpress.android.fluxc.module.ReleaseOkHttpClientModule
import org.wordpress.android.fluxc.module.ReleaseWCNetworkModule
import org.wordpress.android.login.di.LoginServiceModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
        AndroidInjectionModule::class,
        co.innoshop.android.di.ThreadModule::class,
        co.innoshop.android.di.MockedViewModelAssistedFactoriesModule::class,
        co.innoshop.android.di.ApplicationModule::class,
        co.innoshop.android.di.AppConfigModule::class,
        ReleaseBaseModule::class,
        ReleaseNetworkModule::class,
        ReleaseWCNetworkModule::class,
        ReleaseOkHttpClientModule::class,
        co.innoshop.android.di.MockedActivityBindingModule::class,
        co.innoshop.android.di.MockedSelectedSiteModule::class,
        FCMServiceModule::class,
        LoginAnalyticsModule::class,
        LoginServiceModule::class,
        co.innoshop.android.di.MockedNetworkStatusModule::class,
        co.innoshop.android.di.MockedCurrencyModule::class,
        co.innoshop.android.di.SupportModule::class,
        co.innoshop.android.di.OrderFetcherModule::class])
interface AppComponentTest : co.innoshop.android.di.AppComponent {
    @Component.Builder
    interface Builder : co.innoshop.android.di.AppComponent.Builder {
        @BindsInstance
        override fun application(application: Application): co.innoshop.android.di.AppComponentTest.Builder

        override fun build(): co.innoshop.android.di.AppComponentTest
    }
}
