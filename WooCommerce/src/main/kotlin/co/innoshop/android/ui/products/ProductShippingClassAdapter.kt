package co.innoshop.android.ui.products

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import co.innoshop.android.R
import co.innoshop.android.ui.products.ProductShippingClassAdapter.ViewHolder
import kotlinx.android.synthetic.main.product_shipping_class_item.view.*
import org.wordpress.android.fluxc.model.WCProductShippingClassModel

/**
 * RecyclerView adapter which shows a list of product shipping classes, the first of which will
 * be "No shipping class" so the user can choose to clear this value.
 */
class ProductShippingClassAdapter(
    context: Context,
    private val listener: ShippingClassAdapterListener,
    private var selectedShippingClassSlug: String
) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        private const val VT_NO_SHIPPING_CLASS = 0
        private const val VT_SHIPPING_CLASS = 1
    }

    interface ShippingClassAdapterListener {
        fun onShippingClassClicked(shippingClass: WCProductShippingClassModel?)
        fun onRequestLoadMore()
    }

    var shippingClassList: List<WCProductShippingClassModel> = ArrayList()
        set(value) {
            if (!isSameList(value)) {
                field = value
                notifyDataSetChanged()
            }
        }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val noShippingClassText: String = context.getString(R.string.product_no_shipping_class)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return if (getItemViewType(position) == VT_NO_SHIPPING_CLASS) {
            -1
        } else {
            return getShippingClassAtPosition(position)!!.remoteShippingClassId
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VT_NO_SHIPPING_CLASS
        } else {
            VT_SHIPPING_CLASS
        }
    }

    override fun getItemCount(): Int {
        return shippingClassList.size + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.product_shipping_class_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == VT_NO_SHIPPING_CLASS) {
            holder.text.text = noShippingClassText
            holder.text.isChecked = selectedShippingClassSlug.isEmpty()
        } else {
            val shippingClass = getShippingClassAtPosition(position)!!
            holder.text.text = shippingClass.name
            holder.text.isChecked = shippingClass.slug == selectedShippingClassSlug
        }

        if (position > 0 && position == itemCount - 1) {
            listener.onRequestLoadMore()
        }
    }

    private fun isSameList(classes: List<WCProductShippingClassModel>): Boolean {
        if (classes.size != shippingClassList.size) {
            return false
        }

        classes.forEach {
            if (!containsShippingClass(it)) {
                return false
            }
        }

        return true
    }

    private fun containsShippingClass(shippingClass: WCProductShippingClassModel): Boolean {
        shippingClassList.forEach {
            if (it.remoteShippingClassId == shippingClass.remoteShippingClassId) {
                return true
            }
        }
        return false
    }

    private fun getShippingClassAtPosition(position: Int): WCProductShippingClassModel? {
        return if (getItemViewType(position) == VT_NO_SHIPPING_CLASS) {
            null
        } else {
            shippingClassList[position - 1]
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: CheckedTextView = view.text

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position > -1) {
                    getShippingClassAtPosition(position)?.let { shippingClass ->
                        selectedShippingClassSlug = shippingClass.slug
                        listener.onShippingClassClicked(shippingClass)
                    } ?: listener.onShippingClassClicked(null)
                }
            }
        }
    }
}
