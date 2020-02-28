package co.innoshop.android.widgets

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import co.innoshop.android.R

open class ConfirmationDialog : DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        const val TITLE_KEY = "title"
        const val MESSAGE_KEY = "message"
        const val POSITIVE_BUTTON_KEY = "positiveButtonTitle"
        const val NEGATIVE_BUTTON_KEY = "negativeButtonTitle"
        const val RESULT_CONFIRMED = "result-confirmed"
    }

    private lateinit var headerText: TextView
    private lateinit var messageText: TextView

    private var confirmed: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.Woo_Dialog))
        val view = View.inflate(activity, R.layout.confirmation_dialog, null)

        headerText = view.findViewById(R.id.confirmationDialog_header)
        messageText = view.findViewById(R.id.confirmationDialog_message)

        val args = arguments
        val positiveButton: String
        val negativeButton: String
        if (args != null) {
            headerText.text = args.getString(TITLE_KEY, "")
            messageText.text = args.getString(MESSAGE_KEY, "")
            positiveButton = args.getString(POSITIVE_BUTTON_KEY, getString(android.R.string.ok))
            negativeButton = args.getString(NEGATIVE_BUTTON_KEY, getString(R.string.cancel))
        } else {
            positiveButton = getString(android.R.string.ok)
            negativeButton = getString(R.string.cancel)
        }

        builder.setPositiveButton(positiveButton, this)
        builder.setNegativeButton(negativeButton, this)
        builder.setView(view)

        return builder.create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        confirmed = which == DialogInterface.BUTTON_POSITIVE
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        returnResult(confirmed)

        super.onDismiss(dialog)
    }

    open fun returnResult(result: Boolean) {
        val target = targetFragment
        val resultIntent = Intent()
        arguments?.let { resultIntent.replaceExtras(it) }
        resultIntent.putExtra(RESULT_CONFIRMED, result)
        target?.onActivityResult(targetRequestCode, Activity.RESULT_OK, resultIntent)
    }
}
