package co.innoshop.android.ui.mystore

import co.innoshop.android.ui.mystore.MyStoreContract.Presenter
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class MyStoreModule {
    @Binds
    abstract fun provideMyStorePresenter(dashboardPresenter: MyStorePresenter): Presenter

    @ContributesAndroidInjector
    abstract fun myStoreFragment(): MyStoreFragment
}
