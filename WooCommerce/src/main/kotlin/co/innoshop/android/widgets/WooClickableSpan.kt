package co.innoshop.android.widgets

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

/**
 * Custom [ClickableSpan] that removes the default text underline, as well as sets the color
 * of the text to the link color.
 */
class WooClickableSpan(val onClickListener: (view: View) -> Unit) : ClickableSpan() {
    override fun onClick(widget: View) {
        onClickListener(widget)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = false
        ds.color = ds.linkColor
    }
}
