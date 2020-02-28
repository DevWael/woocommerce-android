package co.innoshop.android.ui.imageviewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.annotations.OpenClassOnDebug
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.media.ProductImagesService.Companion.OnProductImageUploaded
import co.innoshop.android.model.Product
import co.innoshop.android.tools.NetworkStatus
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.viewmodel.SavedStateWithArgs
import co.innoshop.android.viewmodel.ScopedViewModel
import co.innoshop.android.viewmodel.SingleLiveEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@OpenClassOnDebug
class ImageViewerViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers,
    private val repository: ImageViewerRepository,
    private val networkStatus: NetworkStatus
) : ScopedViewModel(savedState, dispatchers) {
    private var remoteProductId = 0L

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _showSnackbarMessage = SingleLiveEvent<Int>()
    val showSnackbarMessage: LiveData<Int> = _showSnackbarMessage

    private val _exit = SingleLiveEvent<Unit>()
    val exit: LiveData<Unit> = _exit

    init {
        EventBus.getDefault().register(this)
    }

    fun start(remoteProductId: Long) {
        this.remoteProductId = remoteProductId
        loadProduct()
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun loadProduct() {
        _product.value = repository.getProduct(remoteProductId)
    }

    fun removeProductImage(remoteMediaId: Long) {
        if (!checkNetwork()) {
            return
        }

        if (repository.removeProductImage(remoteProductId, remoteMediaId)) {
            AnalyticsTracker.track(Stat.PRODUCT_IMAGE_REMOVED)
            // reload the product to reflect the removed image
            loadProduct()
        } else {
            _showSnackbarMessage.value = R.string.product_image_error_removing
        }
    }

    private fun checkNetwork(): Boolean {
        if (networkStatus.isConnected()) {
            return true
        }
        _showSnackbarMessage.value = R.string.network_activity_no_connectivity
        return false
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: OnProductImageUploaded) {
        if (remoteProductId == event.remoteProductId) {
            if (event.isError) {
                _showSnackbarMessage.value = R.string.product_image_error_removing
            } else {
                loadProduct()
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ImageViewerViewModel>
}
