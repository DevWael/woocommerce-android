package co.innoshop.android.ui.products

import co.innoshop.android.di.FragmentScope
import co.innoshop.android.ui.products.ProductsModule.ProductDetailFragmentModule
import co.innoshop.android.ui.products.ProductsModule.ProductImagesFragmentModule
import co.innoshop.android.ui.products.ProductsModule.ProductInventoryFragmentModule
import co.innoshop.android.ui.products.ProductsModule.ProductListFragmentModule
import co.innoshop.android.ui.products.ProductsModule.ProductShippingClassFragmentModule
import co.innoshop.android.ui.products.ProductsModule.ProductShippingFragmentModule
import co.innoshop.android.ui.products.ProductsModule.ProductVariantsFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    ProductDetailFragmentModule::class,
    ProductListFragmentModule::class,
    ProductVariantsFragmentModule::class,
    ProductImagesFragmentModule::class,
    ProductInventoryFragmentModule::class,
    ProductShippingFragmentModule::class,
    ProductShippingClassFragmentModule::class
])
object ProductsModule {
    @Module
    abstract class ProductListFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductListModule::class])
        abstract fun productListFragment(): ProductListFragment
    }

    @Module
    abstract class ProductDetailFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductDetailModule::class])
        abstract fun productDetailFragment(): ProductDetailFragment
    }

    @Module
    internal abstract class ProductVariantsFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductVariantsModule::class])
        abstract fun productVariantsFragment(): ProductVariantsFragment
    }

    @Module
    internal abstract class ProductInventoryFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductInventoryModule::class])
        abstract fun productInventoryFragment(): ProductInventoryFragment
    }

    @Module
    internal abstract class ProductShippingFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductShippingModule::class])
        abstract fun productShippingFragment(): ProductShippingFragment
    }

    @Module
    internal abstract class ProductShippingClassFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductShippingClassModule::class])
        abstract fun productShippingClassFragment(): ProductShippingClassFragment
    }

    @Module
    internal abstract class ProductImagesFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ProductImagesModule::class])
        abstract fun productImagesFragment(): ProductImagesFragment
    }
}
