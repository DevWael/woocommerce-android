package co.innoshop.android.ui.products

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import co.innoshop.android.R

class WCProductPropertyEditableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {
    private var editableText: EditText

    // Flag to check if [EditText] already has a [EditText.doAfterTextChanged] defined to avoid multiple callbacks
    private var isTextChangeListenerActive: Boolean = false

    init {
        with(View.inflate(context, R.layout.product_property_editable_view, this)) {
            editableText = findViewById(R.id.editText)
        }
    }

    fun show(hint: String, detail: String?) {
        if (detail.isNullOrEmpty()) {
            editableText.hint = hint
        } else {
            editableText.setText(detail)
            editableText.setSelection(detail.length)
        }
    }

    fun setOnTextChangedListener(cb: (text: Editable?) -> Unit) {
        if (!isTextChangeListenerActive) {
            isTextChangeListenerActive = true
            editableText.doAfterTextChanged { cb(it) }
        }
    }
}
