package co.innoshop.android.ui.refunds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.HideValidationError
import co.innoshop.android.ui.refunds.IssueRefundViewModel.IssueRefundEvent.ShowValidationError
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_refund_by_amount.*
import javax.inject.Inject

class RefundByAmountFragment : BaseFragment() {
    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var currencyFormatter: CurrencyFormatter

    private val viewModel: IssueRefundViewModel by navGraphViewModels(R.id.nav_graph_refunds) { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.fragment_refund_by_amount, container, false)
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
        issueRefund_btnNextFromAmount.setOnClickListener {
            viewModel.onNextButtonTappedFromAmounts()
        }
    }

    private fun setupObservers() {
        viewModel.refundByAmountStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.availableForRefund?.takeIfNotEqualTo(old?.availableForRefund) {
                issueRefund_txtAvailableForRefund.text = it
            }
            new.currency?.takeIfNotEqualTo(old?.currency) {
                issueRefund_refundAmount.initView(new.currency, new.decimals, currencyFormatter)
            }
            new.enteredAmount.takeIfNotEqualTo(old?.enteredAmount) {
                issueRefund_refundAmount.setValue(new.enteredAmount)
            }
            new.isNextButtonEnabled?.takeIfNotEqualTo(old?.isNextButtonEnabled) {
                issueRefund_btnNextFromAmount.isEnabled = it
            }
        }

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowValidationError -> issueRefund_refundAmountInputLayout.error = event.message
                is HideValidationError -> issueRefund_refundAmountInputLayout.error = null
                else -> event.isHandled = false
            }
        })

        issueRefund_refundAmount.value.observe(viewLifecycleOwner, Observer {
            viewModel.onManualRefundAmountChanged(it)
        })
    }
}
