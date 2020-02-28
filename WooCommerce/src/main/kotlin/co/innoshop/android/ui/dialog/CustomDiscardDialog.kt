package co.innoshop.android.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface.OnClickListener
import co.innoshop.android.R.string
import java.lang.ref.WeakReference

/**
 * Used to display discard dialog across the app.
 * Currently used in Products and Orders
 */
object CustomDiscardDialog {
    // Weak ref to avoid leaking the context
    private var dialogRef: WeakReference<AlertDialog>? = null

    fun showDiscardDialog(
        activity: Activity,
        posBtnAction: (OnClickListener)? = null,
        negBtnAction: (OnClickListener)? = null
    ) {
        dialogRef?.get()?.let {
            // Dialog is already present
            return
        }

        val builder = AlertDialog.Builder(activity)
                .setMessage(activity.applicationContext.getString(string.discard_message))
                .setCancelable(true)
                .setPositiveButton(string.discard, posBtnAction)
                .setNegativeButton(string.keep_editing, negBtnAction)
                .setOnDismissListener { onCleared() }

        dialogRef = WeakReference(builder.show())
    }

    fun onCleared() {
        dialogRef?.get()?.dismiss()
        dialogRef?.clear()
    }
}
