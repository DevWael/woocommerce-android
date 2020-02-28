package co.innoshop.android.ui.refunds

import android.app.Dialog
import android.os.Bundle
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import co.innoshop.android.R
import co.innoshop.android.viewmodel.ViewModelFactory
import co.innoshop.android.widgets.NumberPickerDialog
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RefundItemsPickerDialog : NumberPickerDialog(), HasAndroidInjector {
    @Inject internal lateinit var childInjector: DispatchingAndroidInjector<Any>
    @Inject lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: IssueRefundViewModel by navGraphViewModels(R.id.nav_graph_refunds) { viewModelFactory }
    private val navArgs: co.innoshop.android.ui.refunds.RefundItemsPickerDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        AndroidSupportInjection.inject(this)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun returnResult(selectedValue: Int) {
        viewModel.onRefundQuantityChanged(navArgs.uniqueId, selectedValue)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return childInjector
    }
}
