package co.innoshop.android.ui.reviews

import co.innoshop.android.viewmodel.SavedStateWithArgs
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.util.CoroutineDispatchers
import org.wordpress.android.fluxc.Dispatcher

class MockedReviewListViewModel @AssistedInject constructor(
    dispatchers: CoroutineDispatchers,
    reviewRepository: ReviewListRepository,
    networkStatus: NetworkStatus,
    dispatcher: Dispatcher,
    selectedSite: SelectedSite,
    @Assisted arg0: SavedStateWithArgs
) : ReviewListViewModel(
        arg0,
        dispatchers,
        networkStatus,
        dispatcher,
        selectedSite,
        reviewRepository
) {
    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<MockedReviewListViewModel>
}
