package co.innoshop.android.ui.refunds

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.savedstate.SavedStateRegistryOwner
import co.innoshop.android.R
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
abstract class RefundSummaryModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideDefaultArgs(): Bundle? {
            return Bundle()
        }

        @JvmStatic
        @Provides
        fun provideSavedStateRegistryOwner(fragment: RefundSummaryFragment): SavedStateRegistryOwner {
            return fragment.findNavController().getBackStackEntry(R.id.nav_graph_refunds)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(IssueRefundViewModel::class)
    abstract fun bindFactory(factory: IssueRefundViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
