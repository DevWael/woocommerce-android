package co.innoshop.android.ui.reviews

import co.innoshop.android.viewmodel.SavedStateWithArgs
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.util.CoroutineDispatchers

class MockedReviewDetailViewModel @AssistedInject constructor(
    dispatchers: CoroutineDispatchers,
    reviewRepository: ReviewDetailRepository,
    networkStatus: NetworkStatus,
    @Assisted arg0: SavedStateWithArgs
) : ReviewDetailViewModel(
        arg0,
        dispatchers,
        networkStatus,
        reviewRepository
) {
    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<MockedReviewDetailViewModel>
}
