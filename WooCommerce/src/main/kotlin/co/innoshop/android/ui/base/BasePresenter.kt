package co.innoshop.android.ui.base

interface BasePresenter<in T> {
    fun takeView(view: T)
    fun dropView()
}
