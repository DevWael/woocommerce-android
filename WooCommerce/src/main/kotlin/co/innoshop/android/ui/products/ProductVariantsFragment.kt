package co.innoshop.android.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat
import co.innoshop.android.di.GlideApp
import co.innoshop.android.extensions.takeIfNotEqualTo
import co.innoshop.android.model.ProductVariant
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.util.WooAnimUtils
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.Exit
import co.innoshop.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import co.innoshop.android.viewmodel.ViewModelFactory
import co.innoshop.android.widgets.AlignedDividerDecoration
import co.innoshop.android.widgets.SkeletonView
import kotlinx.android.synthetic.main.fragment_product_variants.*
import javax.inject.Inject

class ProductVariantsFragment : BaseFragment(), OnLoadMoreListener {
    companion object {
        const val TAG: String = "ProductVariantsFragment"
    }

    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: ProductVariantsViewModel by viewModels { viewModelFactory }
    private lateinit var productVariantsAdapter: ProductVariantsAdapter

    private val skeletonView = SkeletonView()

    private val navArgs: co.innoshop.android.ui.products.ProductVariantsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_product_variants, container, false)
    }

    override fun onDestroyView() {
        // hide the skeleton view if fragment is destroyed
        skeletonView.hide()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel()
    }

    private fun initializeViewModel() {
        setupObservers(viewModel)
        viewModel.start(navArgs.remoteProductId)
    }

    private fun setupObservers(viewModel: ProductVariantsViewModel) {
        viewModel.viewStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.isSkeletonShown?.takeIfNotEqualTo(old?.isSkeletonShown) { showSkeleton(it) }
            new.isRefreshing?.takeIfNotEqualTo(old?.isRefreshing) { productVariantsRefreshLayout.isRefreshing = it }
            new.isLoadingMore?.takeIfNotEqualTo(old?.isLoadingMore) { showLoadMoreProgress(it) }
            new.isEmptyViewVisible?.takeIfNotEqualTo(old?.isEmptyViewVisible) { isEmptyViewVisible ->
                if (isEmptyViewVisible) {
                    WooAnimUtils.fadeIn(empty_view)
                    empty_view?.button?.visibility = View.GONE
                } else {
                    WooAnimUtils.fadeOut(empty_view)
                }
            }
        }

        viewModel.productVariantList.observe(viewLifecycleOwner, Observer {
            showProductVariants(it)
        })

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is Exit -> activity?.onBackPressed()
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity()

        productVariantsAdapter = ProductVariantsAdapter(activity, GlideApp.with(this), this)
        with(productVariantsList) {
            layoutManager = LinearLayoutManager(activity)
            adapter = productVariantsAdapter
            addItemDecoration(AlignedDividerDecoration(
                    activity, DividerItemDecoration.VERTICAL, R.id.variantOptionName, clipToMargin = false
            ))
        }

        productVariantsRefreshLayout?.apply {
            setColorSchemeColors(
                    ContextCompat.getColor(activity, R.color.colorPrimary),
                    ContextCompat.getColor(activity, R.color.colorAccent),
                    ContextCompat.getColor(activity, R.color.colorPrimaryDark)
            )
            scrollUpChild = productVariantsList
            setOnRefreshListener {
                AnalyticsTracker.track(Stat.PRODUCT_VARIANTS_PULLED_TO_REFRESH)
                viewModel.refreshProductVariants(navArgs.remoteProductId)
            }
        }
    }

    override fun getFragmentTitle() = getString(R.string.product_variations)

    override fun onRequestLoadMore() {
        viewModel.onLoadMoreRequested(navArgs.remoteProductId)
    }

    private fun showSkeleton(show: Boolean) {
        if (show) {
            skeletonView.show(productVariantsList, R.layout.skeleton_product_list, delayed = true)
        } else {
            skeletonView.hide()
        }
    }

    private fun showLoadMoreProgress(show: Boolean) {
        loadMoreProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showProductVariants(productVariants: List<ProductVariant>) {
        productVariantsAdapter.setProductVariantList(productVariants)
    }
}
