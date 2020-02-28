package co.innoshop.android.ui.refunds

import android.app.Dialog
import android.os.Bundle
import androidx.navigation.navGraphViewModels
import co.innoshop.android.R
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.util.CurrencyFormatter
import co.innoshop.android.viewmodel.ViewModelFactory
import co.innoshop.android.widgets.CurrencyAmountDialog
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import java.math.BigDecimal
import javax.inject.Inject

class RefundAmountDialog : CurrencyAmountDialog(), HasAndroidInjector {
    @Inject lateinit var currencyFormatter: CurrencyFormatter
    @Inject internal lateinit var childInjector: DispatchingAndroidInjector<Any>
    @Inject lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: IssueRefundViewModel by navGraphViewModels(R.id.nav_graph_refunds) { viewModelFactory }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        AndroidSupportInjection.inject(this)

        viewModel.productsRefundLiveData.observe(this) { old, new ->
            new.takeIfNotEqualTo(old?.currency) {
                initializeCurrencyEditText(new.currency ?: "", new.decimals, currencyFormatter)
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    override fun returnResult(enteredAmount: BigDecimal) {
        viewModel.onProductsRefundAmountChanged(enteredAmount)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return childInjector
    }
}
