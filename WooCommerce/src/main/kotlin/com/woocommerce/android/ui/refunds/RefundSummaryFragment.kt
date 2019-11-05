package com.woocommerce.android.ui.refunds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.ui.base.UIMessageResolver
import com.woocommerce.android.extensions.navigateBackWithResult
import com.woocommerce.android.ui.main.MainActivity.Companion.BackPressListener
import com.woocommerce.android.ui.orders.OrderDetailFragment.Companion.REFUND_REQUEST_CODE
import com.woocommerce.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.ExitAfterRefund
import com.woocommerce.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.ShowSnackbar
import com.woocommerce.android.viewmodel.ViewModelFactory
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_refund_summary.*
import javax.inject.Inject

class RefundSummaryFragment : DaggerFragment(), BackPressListener {
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: IssueRefundViewModel by activityViewModels { viewModelFactory }

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

        initializeViewModel()
    }

    private fun initializeViewModel() {
        initializeViews()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.event.observe(this, Observer { event ->
            when (event) {
                is ShowSnackbar -> {
                    if (event.undoAction == null) {
                        uiMessageResolver.showSnack(event.message)
                    } else {
                        val snackbar = uiMessageResolver.getUndoSnack(
                                event.message,
                                "",
                                actionListener = View.OnClickListener { event.undoAction.invoke() }
                        )
                        snackbar.addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                viewModel.onProceedWithRefund()
                            }
                        })
                        snackbar.show()
                    }
                }
                is ExitAfterRefund -> {
                    requireActivity().navigateBackWithResult(
                            REFUND_REQUEST_CODE,
                            Bundle(),
                            R.id.nav_host_fragment_main,
                            R.id.orderDetailFragment
                    )
                }
                else -> event.isHandled = false
            }
        })

        viewModel.refundSummaryStateLiveData.observe(this) {
            refundSummary_btnRefund.isEnabled = it.isFormEnabled
            refundSummary_reason.isEnabled = it.isFormEnabled
            refundSummary_refundAmount.text = it.refundAmount
            refundSummary_previouslyRefunded.text = it.previouslyRefunded
            refundSummary_method.text = it.refundMethod
            refundSummary_methodDescription.visibility = if (it.isMethodDescriptionVisible) View.VISIBLE else View.GONE
        }
    }

    private fun initializeViews() {
        refundSummary_btnRefund.setOnClickListener {
            viewModel.onRefundConfirmed(refundSummary_reason.text.toString())
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        findNavController().popBackStack(R.id.orderDetailFragment, false)
        return false
    }
}
