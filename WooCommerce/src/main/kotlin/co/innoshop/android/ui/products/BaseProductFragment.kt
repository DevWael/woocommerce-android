package co.innoshop.android.ui.products

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import co.innoshop.android.R
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.ui.dialog.CustomDiscardDialog
import co.innoshop.android.ui.main.MainActivity.Companion.BackPressListener
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.Exit
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowDiscardDialog
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.ViewModelFactory
import javax.inject.Inject

/**
 * All product related fragments should extend this class to provide a consistent method
 * of displaying snackbar and discard dialogs
 */
abstract class BaseProductFragment : BaseFragment(), BackPressListener {
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    @Inject lateinit var viewModelFactory: ViewModelFactory

    protected val viewModel: ProductDetailViewModel by navGraphViewModels(R.id.nav_graph_products) { viewModelFactory }

    private var publishMenuItem: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers(viewModel)
    }

    private fun setupObservers(viewModel: ProductDetailViewModel) {
        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is Exit -> requireActivity().onBackPressed()
                is ShowDiscardDialog -> CustomDiscardDialog.showDiscardDialog(
                        requireActivity(),
                        event.positiveBtnAction,
                        event.negativeBtnAction
                )
                else -> event.isHandled = false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        publishMenuItem = menu.findItem(R.id.menu_done)
    }

    protected fun showUpdateProductAction(show: Boolean) {
        view?.post { publishMenuItem?.isVisible = show }
    }

    protected fun enablePublishMenuItem(enable: Boolean) {
        publishMenuItem?.isEnabled = enable
    }

    override fun onStop() {
        super.onStop()
        CustomDiscardDialog.onCleared()
    }
}
