package co.innoshop.android.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.ui.dialog.CustomDiscardDialog
import co.innoshop.android.ui.products.ProductShippingViewModel.ViewState
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowDiscardDialog
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.ViewModelFactory
import co.innoshop.android.widgets.WCMaterialOutlinedEditTextView
import kotlinx.android.synthetic.main.fragment_product_shipping.*
import javax.inject.Inject

/**
 * Fragment which enables updating product shipping data.
 */
class ProductShippingFragment : BaseFragment() {
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: ProductShippingViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_product_shipping, container, false)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers(viewModel)
    }

    override fun getFragmentTitle() = getString(R.string.product_shipping_settings)

    private fun setupObservers(viewModel: ProductShippingViewModel) {
        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.product?.takeIfNotEqualTo(old?.product) { showProduct(new) }
        }

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowDiscardDialog -> CustomDiscardDialog.showDiscardDialog(
                        requireActivity(),
                        event.positiveBtnAction,
                        event.negativeBtnAction
                )
            }
        })
    }

    /**
     * Shows the passed weight or dimension value in the passed view and sets the hint so it
     * includes the weight or dimension unit, ex: "Width (in)"
     */
    private fun showValue(view: WCMaterialOutlinedEditTextView, @StringRes hintRes: Int, value: Float?, unit: String?) {
        view.setText(value?.toString() ?: "")
        view.setHint(if (unit != null) {
            getString(hintRes) + " ($unit)"
        } else {
            getString(hintRes)
        })
    }

    private fun showProduct(productData: ViewState) {
        if (!isAdded) return

        showValue(product_weight, R.string.product_weight, productData.product?.weight, viewModel.weightUnit)
        showValue(product_length, R.string.product_length, productData.product?.length, viewModel.dimensionUnit)
        showValue(product_height, R.string.product_height, productData.product?.height, viewModel.dimensionUnit)
        showValue(product_width, R.string.product_width, productData.product?.width, viewModel.dimensionUnit)

        product_shipping_class_spinner.setText(productData.product?.shippingClass ?: "")
        product_shipping_class_spinner.setClickListener {
            showShippingClassFragment()
        }
    }

    private fun showShippingClassFragment() {
        val action = co.innoshop.android.ui.products.ProductShippingFragmentDirections.actionProductShippingFragmentToProductShippingClassFragment(
                shippingClassSlug = product_shipping_class_spinner.getText()
        )
        findNavController().navigate(action)
    }
}
