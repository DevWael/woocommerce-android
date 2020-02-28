package co.innoshop.android.di

import co.innoshop.android.support.HelpActivity
import co.innoshop.android.support.HelpModule
import co.innoshop.android.ui.login.LoginActivity
import co.innoshop.android.ui.login.MagicLinkInterceptActivity
import co.innoshop.android.ui.main.MainActivity
import co.innoshop.android.ui.main.MockedMainModule
import co.innoshop.android.ui.orders.MockedOrdersModule
import co.innoshop.android.ui.prefs.AppSettingsActivity
import co.innoshop.android.ui.prefs.AppSettingsModule
import co.innoshop.android.ui.prefs.MainSettingsModule
import co.innoshop.android.ui.prefs.PrivacySettingsModule
import co.innoshop.android.ui.products.MockedProductDetailFragmentModule
import co.innoshop.android.ui.reviews.MockedReviewDetailFragmentModule
import co.innoshop.android.ui.reviews.MockedReviewListFragmentModule
import co.innoshop.android.ui.sitepicker.SitePickerActivity
import co.innoshop.android.ui.sitepicker.SitePickerModule
import co.innoshop.android.ui.stats.MockedDashboardModule
import co.innoshop.android.ui.stats.MockedMyStoreModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.wordpress.android.login.di.LoginFragmentModule

@Module
abstract class MockedActivityBindingModule {
    @co.innoshop.android.di.ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(
            MockedMainModule::class,
            MockedDashboardModule::class,
            MockedMyStoreModule::class,
            MockedOrdersModule::class,
            co.innoshop.android.di.MockedActivityBindingModule.MockedProductModule::class,
            co.innoshop.android.di.MockedActivityBindingModule.MockedReviewModule::class))
    abstract fun provideMainActivityInjector(): MainActivity

    @Module(includes = [
        MockedProductDetailFragmentModule::class
    ])
    object MockedProductModule

    @Module(includes = [
        MockedReviewListFragmentModule::class,
        MockedReviewDetailFragmentModule::class
    ])
    object MockedReviewModule

    @co.innoshop.android.di.ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(LoginFragmentModule::class))
    abstract fun provideLoginActivityInjector(): LoginActivity

    @co.innoshop.android.di.ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(SitePickerModule::class))
    abstract fun provideSitePickerActivityInjector(): SitePickerActivity

    @co.innoshop.android.di.ActivityScope
    @ContributesAndroidInjector
    abstract fun provideMagicLinkInterceptActivityInjector(): MagicLinkInterceptActivity

    @co.innoshop.android.di.ActivityScope
    @ContributesAndroidInjector(modules = [
        AppSettingsModule::class,
        MainSettingsModule::class,
        PrivacySettingsModule::class
    ])
    abstract fun provideAppSettingsActivityInjector(): AppSettingsActivity

    @co.innoshop.android.di.ActivityScope
    @ContributesAndroidInjector(modules = [HelpModule::class])
    abstract fun provideHelpActivity(): HelpActivity
}
