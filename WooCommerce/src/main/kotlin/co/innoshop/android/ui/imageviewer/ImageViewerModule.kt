package co.innoshop.android.ui.imageviewer

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import co.innoshop.android.di.ActivityScope
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
internal abstract class ImageViewerModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideDefaultArgs(): Bundle? {
            return null
        }
    }

    @ActivityScope
    @Binds
    abstract fun provideUiMessageResolver(uiIMessageResolver: ImageViewerUIMessageResolver): UIMessageResolver

    @Binds
    @IntoMap
    @ViewModelKey(ImageViewerViewModel::class)
    abstract fun bindFactory(factory: ImageViewerViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    abstract fun bindSavedStateRegistryOwner(fragment: ImageViewerActivity): SavedStateRegistryOwner
}
