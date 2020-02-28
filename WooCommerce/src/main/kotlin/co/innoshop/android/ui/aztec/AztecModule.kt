package co.innoshop.android.ui.aztec

import co.innoshop.android.di.FragmentScope
import co.innoshop.android.ui.aztec.AztecModule.AztecEditorFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    AztecEditorFragmentModule::class
])
object AztecModule {
    @Module
    abstract class AztecEditorFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [AztecEditorModule::class])
        abstract fun aztecEditorFragment(): AztecEditorFragment
    }
}
