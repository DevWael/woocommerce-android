package co.innoshop.android.di

import co.innoshop.android.tools.ProductImageMap
import co.innoshop.android.tools.SelectedSite
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Singleton

@Module
class ProductImageMapModule {
    @Provides
    @Singleton
    fun provideProductImageMap(selectedSite: SelectedSite, productStore: WCProductStore) =
            ProductImageMap(selectedSite, productStore)
}
