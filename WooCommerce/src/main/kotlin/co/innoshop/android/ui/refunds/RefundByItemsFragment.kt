package co.innoshop.android.ui.refunds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.tools.ProductImageMap
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.ShowNumberPicker
import co.innoshop.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.ShowRefundAmountDialog
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_refund_by_items.*
import kotlinx.android.synthetic.main.refund_by_items_products.*
import java.math.BigDecimal
import javax.inject.Inject

class RefundByItemsFragment : BaseFragment() {
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var currencyFormatter: CurrencyFormatter
    @Inject lateinit var imageMap: ProductImageMap

    private val viewModel: IssueRefundViewModel by navGraphViewModels(R.id.nav_graph_refunds) { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.fragment_refund_by_items, container, false)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()
        setupObservers()
    }

    private fun initializeViews() {
        issueRefund_products.layoutManager = LinearLayoutManager(context)
        issueRefund_products.setHasFixedSize(true)

        issueRefund_selectButton.setOnClickListener {
            viewModel.onSelectButtonTapped()
        }

        issueRefund_btnNextFromItems.setOnClickListener {
            viewModel.onNextButtonTappedFromItems()
        }

        issueRefund_shippingSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onRefundItemsShippingSwitchChanged(isChecked)
        }
        // TODO: Temporarily disabled, this will be used in a future release - do not remove
//        issueRefund_productsTotal.setOnClickListener {
//            viewModel.onProductRefundAmountTapped()
//        }
    }

    private fun setupObservers() {
        viewModel.refundByItemsStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.currency?.takeIfNotEqualTo(old?.currency) {
                issueRefund_products.adapter = RefundProductListAdapter(
                        currencyFormatter.buildBigDecimalFormatter(new.currency),
                        imageMap,
                        false,
                        { uniqueId -> viewModel.onRefundQuantityTapped(uniqueId) }
                )
            }
            new.isNextButtonEnabled?.takeIfNotEqualTo(old?.isNextButtonEnabled) {
                issueRefund_btnNextFromItems.isEnabled = it
            }
            new.formattedProductsRefund?.takeIfNotEqualTo(old?.formattedProductsRefund) {
                issueRefund_productsTotal.text = it
            }
            new.taxes?.takeIfNotEqualTo(old?.taxes) {
                issueRefund_taxesTotal.text = it
            }
            new.subtotal?.takeIfNotEqualTo(old?.subtotal) {
                issueRefund_subtotal.text = it
            }
            new.selectedItemsHeader?.takeIfNotEqualTo(old?.selectedItemsHeader) {
                issueRefund_selectedItems.text = it
            }
            new.selectButtonTitle?.takeIfNotEqualTo(old?.selectButtonTitle) {
                issueRefund_selectButton.text = it
            }
            // TODO: Temporarily disabled, this will be used in a future release - do not remove
//            new.isShippingRefundVisible?.takeIfNotEqualTo(old?.isShippingRefundVisible) { isVisible ->
//                if (isVisible) {
//                    issueRefund_shippingSection.expand()
//                } else {
//                    issueRefund_shippingSection.collapse()
//                }
//            }
        }

        viewModel.refundItems.observe(viewLifecycleOwner, Observer { list ->
            val adapter = issueRefund_products.adapter as RefundProductListAdapter
            adapter.update(list)
        })

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowNumberPicker -> {
                    val action = co.innoshop.android.ui.refunds.IssueRefundFragmentDirections.actionIssueRefundFragmentToRefundItemsPickerDialog(
                            getString(R.string.order_refunds_select_quantity),
                            event.refundItem.orderItem.uniqueId,
                            event.refundItem.maxQuantity,
                            event.refundItem.quantity
                    )
                    findNavController().navigate(action)
                }
                is ShowRefundAmountDialog -> {
                    val action = co.innoshop.android.ui.refunds.IssueRefundFragmentDirections.actionIssueRefundFragmentToRefundAmountDialog(
                            getString(R.string.order_refunds_products_refund),
                            event.maxRefund,
                            event.refundAmount,
                            BigDecimal.ZERO,
                            event.message
                    )
                    findNavController().navigate(action)
                }
                else -> event.isHandled = false
            }
        })
    }
}
