package co.innoshop.android.di

import co.innoshop.android.ui.orders.list.OrderFetcher
import dagger.Module
import dagger.Provides
import org.wordpress.android.fluxc.Dispatcher
import javax.inject.Singleton

@Module
class OrderFetcherModule {
    @Singleton
    @Provides
    fun provideOrderFetcher(dispatcher: Dispatcher) = OrderFetcher(dispatcher)
}
