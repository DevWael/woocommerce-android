package co.innoshop.android.ui.reviews

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.innoshop.android.viewmodel.SavedStateWithArgs
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.model.ActionStatus
import co.innoshop.android.model.ProductReview
import co.innoshop.android.network.ConnectionChangeReceiver.ConnectionChangeEvent
import co.innoshop.android.push.NotificationHandler.NotificationChannelType.REVIEW
import co.innoshop.android.push.NotificationHandler.NotificationReceivedEvent
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.tools.SelectedSite
import co.innoshop.android.model.RequestResult.ERROR
import co.innoshop.android.model.RequestResult.NO_ACTION_NEEDED
import co.innoshop.android.model.RequestResult.SUCCESS
import co.innoshop.android.ui.reviews.ReviewListViewModel.ReviewListEvent.MarkAllAsRead
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.util.WooLog
import co.innoshop.android.util.WooLog.T.REVIEWS
import co.innoshop.android.viewmodel.LiveDataDelegate
import co.innoshop.android.viewmodel.MultiLiveEvent.Event
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.ScopedViewModel
import co.innoshop.android.viewmodel.SingleLiveEvent
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.action.NotificationAction.MARK_NOTIFICATIONS_READ
import org.wordpress.android.fluxc.action.WCProductAction.UPDATE_PRODUCT_REVIEW_STATUS
import org.wordpress.android.fluxc.generated.WCProductActionBuilder
import org.wordpress.android.fluxc.store.NotificationStore.OnNotificationChanged
import org.wordpress.android.fluxc.store.WCProductStore.OnProductReviewChanged
import org.wordpress.android.fluxc.store.WCProductStore.UpdateProductReviewStatusPayload

@OpenClassOnDebug
class ReviewListViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers,
    private val networkStatus: NetworkStatus,
    private val dispatcher: Dispatcher,
    private val selectedSite: SelectedSite,
    private val reviewRepository: ReviewListRepository
) : ScopedViewModel(savedState, dispatchers) {
    companion object {
        private const val TAG = "ReviewListViewModel"
    }
    private val _moderateProductReview = SingleLiveEvent<ProductReviewModerationRequest>()
    val moderateProductReview: LiveData<ProductReviewModerationRequest> = _moderateProductReview

    private val _reviewList = MutableLiveData<List<ProductReview>>()
    val reviewList: LiveData<List<ProductReview>> = _reviewList

    final val viewStateData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateData

    init {
        EventBus.getDefault().register(this)
        dispatcher.register(this)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
        dispatcher.unregister(this)
        reviewRepository.onCleanup()
    }

    /**
     * Fetch and load cached reviews from the database, then fetch fresh reviews
     * from the API.
     */
    fun start() {
        launch {
            viewState = viewState.copy(isSkeletonShown = true)

            // Initial load. Get and show reviewList from the db if any
            val reviewsInDb = reviewRepository.getCachedProductReviews()
            if (reviewsInDb.isNotEmpty()) {
                _reviewList.value = reviewsInDb
                viewState = viewState.copy(isSkeletonShown = false)
            }
            fetchReviewList(loadMore = false)
        }
    }

    /**
     * Reload reviews from the database. Useful when a change happens on the backend
     * when the list view was not visible.
     */
    fun reloadReviewsFromCache() {
        launch {
            _reviewList.value = reviewRepository.getCachedProductReviews()
        }
    }

    fun loadMoreReviews() {
        if (!reviewRepository.canLoadMore) {
            WooLog.d(REVIEWS, "$TAG : No more product reviews to load")
            return
        }

        viewState = viewState.copy(isLoadingMore = true)
        launch {
            fetchReviewList(loadMore = true)
        }
    }

    fun forceRefreshReviews() {
        viewState = viewState.copy(isRefreshing = true)
        launch {
            fetchReviewList(loadMore = false)
        }
    }

    fun checkForUnreadReviews() {
        launch {
            viewState = viewState.copy(hasUnreadReviews = reviewRepository.getHasUnreadCachedProductReviews())
        }
    }

    fun markAllReviewsAsRead() {
        if (networkStatus.isConnected()) {
            triggerEvent(MarkAllAsRead(ActionStatus.SUBMITTED))

            launch {
                when (reviewRepository.markAllProductReviewsAsRead()) {
                    ERROR -> {
                        triggerEvent(MarkAllAsRead(ActionStatus.ERROR))
                        triggerEvent(ShowSnackbar(R.string.wc_mark_all_read_error))
                    }
                    NO_ACTION_NEEDED, SUCCESS -> {
                        triggerEvent(MarkAllAsRead(ActionStatus.SUCCESS))
                        triggerEvent(ShowSnackbar(R.string.wc_mark_all_read_success))
                    }
                }
            }
        } else {
            // Network is not connected
            showOfflineSnack()
        }
    }

    // region Review Moderation
    fun submitReviewStatusChange(review: ProductReview, newStatus: ProductReviewStatus) {
        if (networkStatus.isConnected()) {
            val payload = UpdateProductReviewStatusPayload(
                    selectedSite.get(),
                    review.remoteId,
                    newStatus.toString()
            )
            dispatcher.dispatch(WCProductActionBuilder.newUpdateProductReviewStatusAction(payload))

            AnalyticsTracker.track(
                    Stat.REVIEW_ACTION,
                    mapOf(AnalyticsTracker.KEY_TYPE to newStatus.toString()))

            sendReviewModerationUpdate(ActionStatus.SUBMITTED)
        } else {
            // Network is not connected
            showOfflineSnack()
            sendReviewModerationUpdate(ActionStatus.ERROR)
        }
    }

    private fun sendReviewModerationUpdate(newRequestStatus: ActionStatus) {
        _moderateProductReview.value = _moderateProductReview.value?.apply { actionStatus = newRequestStatus }

        // If the request has been completed, set the event to null to prevent issues later.
        if (newRequestStatus.isComplete()) {
            _moderateProductReview.value = null
        }
    }
    // endregion

    private suspend fun fetchReviewList(loadMore: Boolean) {
        if (networkStatus.isConnected()) {
            when (reviewRepository.fetchProductReviews(loadMore)) {
                SUCCESS, NO_ACTION_NEEDED -> {
                    _reviewList.value = reviewRepository.getCachedProductReviews()
                }
                else -> triggerEvent(ShowSnackbar(R.string.review_fetch_error))
            }

            checkForUnreadReviews()
        } else {
            // Network is not connected
            showOfflineSnack()
        }

        viewState = viewState.copy(
                isSkeletonShown = false,
                isLoadingMore = false,
                isRefreshing = false
        )
    }

    private fun showOfflineSnack() {
        // Network is not connected
        triggerEvent(ShowSnackbar(R.string.offline_error))
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ConnectionChangeEvent) {
        if (event.isConnected) {
            // Refresh data now that a connection is active if needed
            forceRefreshReviews()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NotificationReceivedEvent) {
        if (event.channel == REVIEW) {
            // New review notification received. Request the list of reviews be refreshed.
            forceRefreshReviews()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: OnRequestModerateReviewEvent) {
        if (networkStatus.isConnected()) {
            // Send the request to the UI to show the UNDO snackbar
            _moderateProductReview.value = event.request
        } else {
            // Network not connected
            showOfflineSnack()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationChanged(event: OnNotificationChanged) {
        if (event.causeOfChange == MARK_NOTIFICATIONS_READ) {
            if (!event.isError) {
                reloadReviewsFromCache()
                checkForUnreadReviews()
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductReviewChanged(event: OnProductReviewChanged) {
        if (event.causeOfChange == UPDATE_PRODUCT_REVIEW_STATUS) {
            if (event.isError) {
                // Show an error in the UI and reload the view
                triggerEvent(ShowSnackbar(R.string.wc_moderate_review_error))
                sendReviewModerationUpdate(ActionStatus.ERROR)
            } else {
                sendReviewModerationUpdate(ActionStatus.SUCCESS)
            }
        }
    }

    @Parcelize
    data class ViewState(
        val isSkeletonShown: Boolean? = null,
        val isLoadingMore: Boolean? = null,
        val isRefreshing: Boolean? = null,
        val hasUnreadReviews: Boolean? = null
    ) : Parcelable

    sealed class ReviewListEvent : Event() {
        data class MarkAllAsRead(val status: ActionStatus) : ReviewListEvent()
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ReviewListViewModel>
}
