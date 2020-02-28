package co.innoshop.android.ui.refunds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import co.innoshop.android.R
import co.innoshop.android.RequestCodes
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.extensions.hide
import co.innoshop.android.extensions.navigateBackWithResult
import co.innoshop.android.extensions.show
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.ui.main.MainActivity.Companion.BackPressListener
import co.innoshop.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.ShowRefundConfirmation
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.Exit
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_refund_summary.*
import javax.inject.Inject

class RefundSummaryFragment : BaseFragment(), BackPressListener {
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: IssueRefundViewModel by navGraphViewModels(R.id.nav_graph_refunds) { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.fragment_refund_summary, container, false)
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

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.getSnack(event.message, *event.args).show()
                is Exit -> {
                    requireActivity().navigateBackWithResult(
                            RequestCodes.ORDER_REFUND,
                            Bundle(),
                            R.id.nav_host_fragment_main,
                            R.id.orderDetailFragment
                    )
                }
                is ShowRefundConfirmation -> {
                    val action = co.innoshop.android.ui.refunds.RefundSummaryFragmentDirections.actionRefundSummaryFragmentToRefundConfirmationDialog(
                            event.title, event.message, event.confirmButtonTitle
                    )
                    findNavController().navigate(action)
                }
                else -> event.isHandled = false
            }
        })

        viewModel.refundSummaryStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.isFormEnabled?.takeIfNotEqualTo(old?.isFormEnabled) {
                refundSummary_btnRefund.isEnabled = new.isFormEnabled
                refundSummary_reason.isEnabled = new.isFormEnabled
            }
            new.refundAmount?.takeIfNotEqualTo(old?.refundAmount) { refundSummary_refundAmount.text = it }
            new.previouslyRefunded?.takeIfNotEqualTo(old?.previouslyRefunded) {
                refundSummary_previouslyRefunded.text = it
            }
            new.refundMethod?.takeIfNotEqualTo(old?.refundMethod) { refundSummary_method.text = it }
            new.isMethodDescriptionVisible?.takeIfNotEqualTo(old?.isMethodDescriptionVisible) { visible ->
                if (visible)
                    refundSummary_methodDescription.show()
                else
                    refundSummary_methodDescription.hide()
            }
        }
    }

    private fun initializeViews() {
        refundSummary_btnRefund.setOnClickListener {
            viewModel.onRefundIssued(refundSummary_reason.text.toString())
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        if (viewModel.isRefundInProgress) {
            Toast.makeText(context, R.string.order_refunds_refund_in_progress, Toast.LENGTH_SHORT).show()
        } else {
            findNavController().popBackStack()
        }
        return false
    }
}
