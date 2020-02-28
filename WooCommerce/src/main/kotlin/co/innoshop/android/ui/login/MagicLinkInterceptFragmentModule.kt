package co.innoshop.android.ui.login

import co.innoshop.android.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class MagicLinkInterceptFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [MagicLinkInterceptModule::class])
    internal abstract fun magicLinkInterceptFragment(): MagicLinkInterceptFragment
}
