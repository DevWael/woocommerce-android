package co.innoshop.android.ui.orders.notes

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import co.innoshop.android.ui.orders.notes.OrderNoteListItem.Header
import co.innoshop.android.ui.orders.notes.OrderNoteListItem.Note
import co.innoshop.android.ui.orders.notes.OrderNoteListItem.ViewType

class OrderNotesAdapter : Adapter<OrderNoteViewHolder>() {
    private val notes = mutableListOf<OrderNoteListItem>()

    init {
        setHasStableIds(true)
    }

    fun setNotes(newList: List<OrderNoteListItem>) {
        val diffResult = DiffUtil.calculateDiff(OrderNotesDiffCallback(notes.toList(), newList))
        notes.clear()
        notes.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderNoteViewHolder {
        return when (viewType) {
            ViewType.NOTE.id -> NoteItemViewHolder(parent)
            ViewType.HEADER.id -> HeaderItemViewHolder(parent)
            else -> throw IllegalArgumentException("Unexpected view type in OrderNotesAdapter")
        }
    }

    override fun onBindViewHolder(holder: OrderNoteViewHolder, position: Int) {
        val isLast = position == notes.size - 1
        when (getItemViewType(position)) {
            ViewType.NOTE.id -> (holder as NoteItemViewHolder).bind(notes[position] as Note, isLast)
            ViewType.HEADER.id -> (holder as HeaderItemViewHolder).bind(notes[position] as Header)
            else -> throw IllegalArgumentException("Unexpected view holder in OrderNotesAdapter")
        }
    }

    override fun getItemCount() = notes.size

    override fun getItemId(position: Int): Long = notes[position].longId

    override fun getItemViewType(position: Int): Int {
        return notes[position].viewType.id
    }
}
