package co.innoshop.android.ui.products

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import co.innoshop.android.R

/**
 * CardView with an optional caption (title), used for product detail properties
 */
class WCProductPropertyCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : androidx.cardview.widget.CardView(context, attrs, defStyle) {
    private var view: View = View.inflate(context, R.layout.product_property_cardview, this)

    fun show(caption: String?) {
        with(view.findViewById<TextView>(R.id.cardCaptionText)) {
            if (caption.isNullOrBlank()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = caption
            }
        }
    }
}
