package co.innoshop.android.di

import android.content.Context
import com.nhaarman.mockitokotlin2.spy
import co.innoshop.android.tools.NetworkStatus
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object MockedNetworkStatusModule {
    @JvmStatic
    @Provides
    @Singleton
    fun provideNetworkStatus(context: Context): NetworkStatus {
        return spy(NetworkStatus(context))
    }
}
