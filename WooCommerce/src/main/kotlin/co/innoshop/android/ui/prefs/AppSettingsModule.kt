package co.innoshop.android.ui.prefs

import co.innoshop.android.di.ActivityScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class AppSettingsModule {
    @ActivityScope
    @Binds
    abstract fun provideAppSettingsPresenter(appSettingsPresenter: AppSettingsPresenter):
            AppSettingsContract.Presenter
}
