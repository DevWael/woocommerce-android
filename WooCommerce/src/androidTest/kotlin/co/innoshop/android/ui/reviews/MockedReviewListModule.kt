package co.innoshop.android.ui.reviews

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.model.ProductReview
import co.innoshop.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
abstract class MockedReviewListModule {
    @Module
    companion object {
        private var reviews: List<ProductReview>? = null

        fun setMockReviews(reviewsList: List<ProductReview>) {
            this.reviews = reviewsList
        }

        @JvmStatic
        @Provides
        fun provideDefaultArgs(): Bundle? {
            return null
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(ReviewListViewModel::class)
    abstract fun bindFactory(factory: MockedReviewListViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    abstract fun bindSavedStateRegistryOwner(fragment: ReviewListFragment): SavedStateRegistryOwner
}
