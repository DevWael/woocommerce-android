package co.innoshop.android.ui.products

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.model.Product
import co.innoshop.android.ui.base.TopLevelFragment
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.ui.main.MainNavigationRouter
import co.innoshop.android.ui.products.ProductListAdapter.OnProductClickListener
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.ViewModelFactory
import co.innoshop.android.widgets.AlignedDividerDecoration
import co.innoshop.android.widgets.SkeletonView
import co.innoshop.android.widgets.WCEmptyView.EmptyViewType
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_product_list.*
import javax.inject.Inject

class ProductListFragment : TopLevelFragment(), OnProductClickListener,
        OnLoadMoreListener,
        OnQueryTextListener,
        OnActionExpandListener {
    companion object {
        val TAG: String = ProductListFragment::class.java.simpleName
        const val KEY_LIST_STATE = "list-state"
        const val KEY_WIP_EXPANDED = "wip_expanded"
        fun newInstance() = ProductListFragment()
    }

    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private lateinit var productAdapter: ProductListAdapter
    private var listState: Parcelable? = null

    private val viewModel: ProductListViewModel by viewModels { viewModelFactory }

    private val skeletonView = SkeletonView()

    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity()

        listState = savedInstanceState?.getParcelable(KEY_LIST_STATE)
        products_wip_card.isExpanded = savedInstanceState?.getBoolean(KEY_WIP_EXPANDED) ?: false

        productAdapter = ProductListAdapter(activity, this, this)
        productsRecycler.layoutManager = LinearLayoutManager(activity)
        productsRecycler.adapter = productAdapter
        productsRecycler.addItemDecoration(
                AlignedDividerDecoration(
                        activity,
                        DividerItemDecoration.VERTICAL, R.id.productName, clipToMargin = false
                )
        )

        productsRefreshLayout?.apply {
            setColorSchemeColors(
                    ContextCompat.getColor(activity, R.color.colorPrimary),
                    ContextCompat.getColor(activity, R.color.colorAccent),
                    ContextCompat.getColor(activity, R.color.colorPrimaryDark)
            )
            scrollUpChild = productsRecycler
            setOnRefreshListener {
                viewModel.onRefreshRequested()
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_LIST_STATE, productsRecycler.layoutManager?.onSaveInstanceState())
        outState.putBoolean(KEY_WIP_EXPANDED, products_wip_card.isExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        skeletonView.hide()
        disableSearchListeners()
        searchView = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers(viewModel)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            disableSearchListeners()
        } else {
            enableSearchListeners()
        }
    }

    override fun onReturnedFromChildFragment() {
        showOptionsMenu(true)

        if (!viewModel.isSearching()) {
            viewModel.loadProducts()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_product_list_fragment, menu)

        searchMenuItem = menu.findItem(R.id.menu_search)
        searchView = searchMenuItem?.actionView as SearchView?
        searchView?.queryHint = getString(R.string.product_search_hint)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        refreshOptionsMenu()
        super.onPrepareOptionsMenu(menu)
    }

    private fun showOptionsMenu(show: Boolean) {
        setHasOptionsMenu(show)
        if (show) {
            refreshOptionsMenu()
        }
    }

    /**
     * Use this rather than invalidateOptionsMenu() since that collapses the search menu item
     */
    private fun refreshOptionsMenu() {
        val showSearch = shouldShowSearchMenuItem()
        searchMenuItem?.let { menuItem ->
            if (menuItem.isVisible != showSearch) menuItem.isVisible = showSearch

            val isSearchActive = viewModel.viewStateLiveData.liveData.value?.isSearchActive == true
            if (menuItem.isActionViewExpanded != isSearchActive) {
                disableSearchListeners()
                if (isSearchActive) {
                    menuItem.expandActionView()
                    searchView?.setQuery(viewModel.viewStateLiveData.liveData.value?.query, false)
                } else {
                    menuItem.collapseActionView()
                }
                enableSearchListeners()
            }
        }
    }

    /**
     * Prevent search from appearing when a child fragment is active
     */
    private fun shouldShowSearchMenuItem(): Boolean {
        val isChildShowing = (activity as? MainNavigationRouter)?.isChildFragmentShowing() ?: false
        return !isChildShowing
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                AnalyticsTracker.track(Stat.PRODUCT_LIST_MENU_SEARCH_TAPPED)
                enableSearchListeners()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun closeSearchView() {
        disableSearchListeners()
        updateActivityTitle()
        searchMenuItem?.collapseActionView()
    }

    private fun disableSearchListeners() {
        searchMenuItem?.setOnActionExpandListener(null)
        searchView?.setOnQueryTextListener(null)
    }

    private fun enableSearchListeners() {
        searchMenuItem?.setOnActionExpandListener(this)
        searchView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        viewModel.onSearchRequested()
        org.wordpress.android.util.ActivityUtils.hideKeyboard(activity)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.onSearchQueryChanged(newText)
        return true
    }

    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
        viewModel.onSearchOpened()
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        viewModel.onSearchClosed()
        closeSearchView()
        return true
    }

    private fun setupObservers(viewModel: ProductListViewModel) {
        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.isSkeletonShown?.takeIfNotEqualTo(old?.isSkeletonShown) { showSkeleton(it) }
            new.isLoadingMore?.takeIfNotEqualTo(old?.isLoadingMore) { showLoadMoreProgress(it) }
            new.isRefreshing?.takeIfNotEqualTo(old?.isRefreshing) { productsRefreshLayout.isRefreshing = it }
            new.isEmptyViewVisible?.takeIfNotEqualTo(old?.isEmptyViewVisible) { isEmptyViewVisible ->
                if (isEmptyViewVisible) {
                    if (new.isSearchActive == true) {
                        empty_view.show(EmptyViewType.SEARCH_RESULTS, searchQueryOrFilter = viewModel.getSearchQuery())
                    } else {
                        empty_view.show(EmptyViewType.PRODUCT_LIST)
                    }
                } else {
                    empty_view.hide()
                }
            }
        }

        viewModel.productList.observe(viewLifecycleOwner, Observer {
            showProductList(it)
        })

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
            }
        })
    }

    override fun getFragmentTitle() = getString(R.string.products)

    override fun refreshFragmentState() {
        if (isActive) {
            viewModel.refreshProducts()
        }
    }

    override fun scrollToTop() {
        productsRecycler.smoothScrollToPosition(0)
    }

    private fun showSkeleton(show: Boolean) {
        if (show) {
            showProductWIPNoticeCard(false)
            skeletonView.show(productsRecycler, R.layout.skeleton_product_list, delayed = true)
        } else {
            skeletonView.hide()
        }
    }

    private fun showLoadMoreProgress(show: Boolean) {
        loadMoreProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showProductList(products: List<Product>) {
        productAdapter.setProductList(products)
        listState?.let {
            productsRecycler.layoutManager?.onRestoreInstanceState(it)
            listState = null
        }
        showProductWIPNoticeCard(true)
    }

    private fun showProductWIPNoticeCard(show: Boolean) {
        if (show) {
            products_wip_card.visibility = View.VISIBLE
            products_wip_card.initView()
        } else {
            products_wip_card.visibility = View.GONE
        }
    }

    override fun onProductClick(remoteProductId: Long) {
        disableSearchListeners()
        showOptionsMenu(false)
        (activity as? MainNavigationRouter)?.showProductDetail(remoteProductId)
    }

    override fun onRequestLoadMore() {
        viewModel.onLoadMoreRequested()
    }
}
