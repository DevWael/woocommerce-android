package co.innoshop.android.ui.products

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
abstract class ProductShippingClassModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideDefaultArgs(fragment: ProductShippingClassFragment): Bundle? {
            return fragment.arguments
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(ProductShippingClassViewModel::class)
    abstract fun bindFactory(factory: ProductShippingClassViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    abstract fun bindSavedStateRegistryOwner(fragment: ProductShippingClassFragment): SavedStateRegistryOwner
}
