package co.innoshop.android.ui.refunds

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import dagger.Module
import dagger.Binds
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
abstract class RefundDetailModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideDefaultArgs(fragment: RefundDetailFragment): Bundle? {
            return fragment.arguments
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(RefundDetailViewModel::class)
    abstract fun bindFactory(factory: RefundDetailViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    abstract fun bindSavedStateRegistryOwner(fragment: RefundDetailFragment): SavedStateRegistryOwner
}
