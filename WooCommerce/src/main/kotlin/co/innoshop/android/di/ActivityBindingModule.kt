package co.innoshop.android.di

import co.innoshop.android.support.HelpActivity
import co.innoshop.android.support.HelpModule
import co.innoshop.android.ui.aztec.AztecModule
import co.innoshop.android.ui.dashboard.DashboardModule
import co.innoshop.android.ui.imageviewer.ImageViewerActivity
import co.innoshop.android.ui.imageviewer.ImageViewerModule
import co.innoshop.android.ui.login.LoginActivity
import co.innoshop.android.ui.login.LoginNoJetpackFragmentModule
import co.innoshop.android.ui.login.MagicLinkInterceptActivity
import co.innoshop.android.ui.login.MagicLinkInterceptFragmentModule
import co.innoshop.android.ui.main.MainActivity
import co.innoshop.android.ui.main.MainModule
import co.innoshop.android.ui.mystore.MyStoreModule
import co.innoshop.android.ui.orders.OrdersModule
import co.innoshop.android.ui.prefs.AppSettingsActivity
import co.innoshop.android.ui.prefs.AppSettingsModule
import co.innoshop.android.ui.prefs.MainSettingsModule
import co.innoshop.android.ui.prefs.PrivacySettingsModule
import co.innoshop.android.ui.products.ProductsModule
import co.innoshop.android.ui.refunds.RefundsModule
import co.innoshop.android.ui.reviews.ReviewsModule
import co.innoshop.android.ui.sitepicker.SitePickerActivity
import co.innoshop.android.ui.sitepicker.SitePickerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.wordpress.android.login.di.LoginFragmentModule

@Module
abstract class ActivityBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(
            modules = [
            MainModule::class,
            DashboardModule::class,
            MyStoreModule::class,
            OrdersModule::class,
            RefundsModule::class,
            ProductsModule::class,
            ReviewsModule::class,
            SitePickerModule::class,
            AztecModule::class
    ])
    abstract fun provideMainActivityInjector(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        LoginFragmentModule::class,
        MagicLinkInterceptFragmentModule::class,
        LoginNoJetpackFragmentModule::class])
    abstract fun provideLoginActivityInjector(): LoginActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun provideMagicLinkInterceptActivityInjector(): MagicLinkInterceptActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        AppSettingsModule::class,
        MainSettingsModule::class,
        PrivacySettingsModule::class
    ])
    abstract fun provideAppSettingsActivityInjector(): AppSettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [HelpModule::class])
    abstract fun provideHelpActivity(): HelpActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SitePickerModule::class])
    abstract fun provideSitePickerActivityInjector(): SitePickerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(ImageViewerModule::class))
    abstract fun provideImageViewerActivity(): ImageViewerActivity
}
