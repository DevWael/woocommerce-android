package co.innoshop.android.ui.main

import co.innoshop.android.di.ActivityScope
import co.innoshop.android.ui.base.UIMessageResolver
import dagger.Binds
import dagger.Module

@Module
internal abstract class MainModule {
    @ActivityScope
    @Binds
    abstract fun provideMainPresenter(mainActivityPresenter: MainPresenter): MainContract.Presenter

    @ActivityScope
    @Binds
    abstract fun provideUiMessageResolver(mainUIMessageResolver: MainUIMessageResolver): UIMessageResolver
}
