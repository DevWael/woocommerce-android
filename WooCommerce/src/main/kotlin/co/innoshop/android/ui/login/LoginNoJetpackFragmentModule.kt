package co.innoshop.android.ui.login

import co.innoshop.android.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class LoginNoJetpackFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [LoginNoJetpackModule::class])
    internal abstract fun loginNoJetpackFragment(): LoginNoJetpackFragment
}
