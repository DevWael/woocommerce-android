package co.innoshop.android.di

import android.content.Context
import co.innoshop.android.BuildConfig
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.network.UserAgent
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AppSecrets

@Module
class AppConfigModule {
    companion object {
        private const val USER_AGENT_APPNAME = "wc-android"
    }

    @Provides
    fun provideAppSecrets() = AppSecrets(co.innoshop.android.BuildConfig.OAUTH_APP_ID, co.innoshop.android.BuildConfig.OAUTH_APP_SECRET)

    @Provides
    fun provideUserAgent(appContext: Context) = UserAgent(appContext, USER_AGENT_APPNAME)
}
