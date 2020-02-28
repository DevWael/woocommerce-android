package co.innoshop.android.ui.products

import co.innoshop.android.di.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class MockedProductDetailFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [MockedProductDetailModule::class])
    abstract fun productDetailfragment(): ProductDetailFragment
}
