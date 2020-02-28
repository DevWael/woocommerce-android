package co.innoshop.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import co.innoshop.android.R
import co.innoshop.android.di.ViewModelAssistedFactory
import co.innoshop.android.util.CoroutineDispatchers
import co.innoshop.android.viewmodel.SavedStateWithArgs
import co.innoshop.android.model.RequestResult
import co.innoshop.android.model.RequestResult.ERROR
import co.innoshop.android.model.RequestResult.NO_ACTION_NEEDED
import co.innoshop.android.model.RequestResult.RETRY
import co.innoshop.android.model.RequestResult.SUCCESS
import co.innoshop.android.viewmodel.ScopedViewModel
import co.innoshop.android.viewmodel.SingleLiveEvent
import kotlinx.coroutines.launch

class MagicLinkInterceptViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers,
    private val magicLinkInterceptRepository: MagicLinkInterceptRepository
) : ScopedViewModel(savedState, dispatchers) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isAuthTokenUpdated = MutableLiveData<Boolean>()
    val isAuthTokenUpdated: LiveData<Boolean> = _isAuthTokenUpdated

    private val _showSnackbarMessage = SingleLiveEvent<Int>()
    val showSnackbarMessage: LiveData<Int> = _showSnackbarMessage

    private val _showRetryOption = SingleLiveEvent<Boolean>()
    val showRetryOption: LiveData<Boolean> = _showRetryOption

    fun updateMagicLinkAuthToken(authToken: String) {
        launch {
            _isLoading.value = true
            handleRequestResultResponse(magicLinkInterceptRepository.updateMagicLinkAuthToken(authToken))
        }
    }

    fun fetchAccountInfo() {
        launch {
            _isLoading.value = true
            _showRetryOption.value = false
            handleRequestResultResponse(magicLinkInterceptRepository.fetchAccountInfo())
        }
    }

    private fun handleRequestResultResponse(requestResult: RequestResult) {
        _isLoading.value = false
        when (requestResult) {
            SUCCESS -> _isAuthTokenUpdated.value = true

            // Errors can occur if the auth token was not updated to the FluxC cache
            // or if the user is not logged in
            // Either way, display error message and redirect user to login screen
            ERROR -> {
                _isAuthTokenUpdated.value = false
                _showSnackbarMessage.value = R.string.magic_link_update_error
            }

            // If magic link update is successful, but account & site info could not be fetched,
            // display error message and provide option for user to retry the request
            RETRY -> {
                _showSnackbarMessage.value = R.string.magic_link_fetch_account_error
                _showRetryOption.value = true
            }
            NO_ACTION_NEEDED -> { }
        }
    }

    override fun onCleared() {
        super.onCleared()
        magicLinkInterceptRepository.onCleanup()
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<MagicLinkInterceptViewModel>
}
