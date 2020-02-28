package co.innoshop.android.di

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import co.innoshop.android.tools.SelectedSite
import dagger.Module
import dagger.Provides
import org.mockito.ArgumentMatchers.anyInt
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore
import javax.inject.Singleton

@Module
object MockedSelectedSiteModule {
    private var siteModel: SiteModel? = null

    fun setSiteModel(siteModel: SiteModel) {
        co.innoshop.android.di.MockedSelectedSiteModule.siteModel = siteModel
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideSelectedSite(context: Context): SelectedSite {
        val mockSiteStore = mock<SiteStore>()
        // Create and return a fake SiteModel from any ID that is given to SiteStore.getSiteByLocalId()
        whenever(mockSiteStore.getSiteByLocalId((anyInt()))).thenAnswer { invocation ->
            SiteModel().apply { id = invocation.arguments[0] as Int }
        }

        val selectedSite = SelectedSite(context, mockSiteStore)
        co.innoshop.android.di.MockedSelectedSiteModule.siteModel?.let {
            selectedSite.set(it)
        }
        return selectedSite
    }
}
