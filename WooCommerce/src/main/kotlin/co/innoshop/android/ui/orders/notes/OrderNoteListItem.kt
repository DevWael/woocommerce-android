package co.innoshop.android.ui.orders.notes

import co.innoshop.android.model.OrderNote
import co.innoshop.android.ui.orders.notes.OrderNoteListItem.ViewType.HEADER
import co.innoshop.android.ui.orders.notes.OrderNoteListItem.ViewType.NOTE

sealed class OrderNoteListItem(val viewType: ViewType) {
    class Header(val text: String) : OrderNoteListItem(HEADER)
    class Note(val note: OrderNote, override val longId: Long = note.remoteNoteId) : OrderNoteListItem(NOTE)

    open val longId: Long
        get() = hashCode().toLong()

    enum class ViewType(val id: Int) {
        HEADER(0),
        NOTE(1)
    }
}
