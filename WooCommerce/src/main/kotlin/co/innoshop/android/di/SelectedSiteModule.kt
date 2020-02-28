package co.innoshop.android.di

import android.content.Context
import co.innoshop.android.tools.SelectedSite
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.store.SiteStore
import javax.inject.Singleton

@Module
class SelectedSiteModule {
    @Provides
    @Singleton
    fun provideSelectedSite(context: Context, siteStore: SiteStore) = SelectedSite(context, siteStore)
}
