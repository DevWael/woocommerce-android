package co.innoshop.android.ui.imageviewer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import co.innoshop.android.R
import co.innoshop.android.R.style
import co.innoshop.android.RequestCodes
import co.innoshop.android.model.Product
import co.innoshop.android.ui.base.UIMessageResolver
import co.innoshop.android.ui.imageviewer.ImageViewerFragment.Companion.ImageViewerListener
import co.innoshop.android.util.WooAnimUtils
import co.innoshop.android.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_image_viewer.*
import javax.inject.Inject

/**
 * Full-screen product image viewer with pinch-and-zoom
 */
class ImageViewerActivity : AppCompatActivity(), ImageViewerListener {
    companion object {
        private const val KEY_IMAGE_REMOTE_MEDIA_ID = "remote_media_id"
        private const val KEY_IMAGE_REMOTE_PRODUCT_ID = "remote_product_id"
        private const val KEY_TRANSITION_NAME = "transition_name"
        private const val KEY_ENABLE_REMOVE_IMAGE = "enable_remove_image"
        private const val KEY_IS_CONFIRMATION_SHOWING = "is_confirmation_showing"

        const val KEY_DID_REMOVE_IMAGE = "did_remove_image"

        private const val TOOLBAR_FADE_DELAY_MS = 2500L

        /**
         * Shows all images for the passed product in a swipeable viewPager - the viewPager
         * will automatically position itself to the passed image
         */
        fun showProductImages(
            fragment: Fragment,
            productModel: Product,
            imageModel: Product.Image,
            sharedElement: View? = null,
            enableRemoveImage: Boolean = false
        ) {
            val context = fragment.requireActivity()

            val intent = Intent(context, ImageViewerActivity::class.java).also {
                it.putExtra(KEY_IMAGE_REMOTE_PRODUCT_ID, productModel.remoteId)
                it.putExtra(KEY_IMAGE_REMOTE_MEDIA_ID, imageModel.id)
                it.putExtra(KEY_ENABLE_REMOVE_IMAGE, enableRemoveImage)
            }

            // use a shared element transition if a shared element view was passed, otherwise default to fade-in
            val options = sharedElement?.let {
                intent.putExtra(KEY_TRANSITION_NAME, it.transitionName)
                ActivityOptions.makeSceneTransitionAnimation(context, sharedElement, it.transitionName)
            } ?: ActivityOptions.makeCustomAnimation(
                    context,
                    R.anim.activity_fade_in,
                    R.anim.activity_fade_out
            )

            fragment.startActivityForResult(intent, RequestCodes.PRODUCT_IMAGE_VIEWER, options.toBundle())
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private var remoteProductId = 0L
    private var remoteMediaId = 0L
    private var enableRemoveImage = false

    private lateinit var transitionName: String
    private val viewModel: ImageViewerViewModel by viewModels { viewModelFactory }
    private lateinit var pagerAdapter: ImageViewerAdapter

    private val fadeOutToolbarHandler = Handler()
    private var canTransitionOnFinish = true
    private var didRemoveImage = false

    private var confirmationDialog: AlertDialog? = null
    private var isConfirmationShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        remoteProductId = savedInstanceState?.getLong(KEY_IMAGE_REMOTE_PRODUCT_ID)
                ?: intent.getLongExtra(KEY_IMAGE_REMOTE_PRODUCT_ID, 0L)

        remoteMediaId = savedInstanceState?.getLong(KEY_IMAGE_REMOTE_MEDIA_ID)
                ?: intent.getLongExtra(KEY_IMAGE_REMOTE_MEDIA_ID, 0L)

        enableRemoveImage = savedInstanceState?.getBoolean(KEY_ENABLE_REMOVE_IMAGE)
                ?: intent.getBooleanExtra(KEY_ENABLE_REMOVE_IMAGE, false)

        didRemoveImage = savedInstanceState?.getBoolean(KEY_DID_REMOVE_IMAGE) ?: false

        transitionName = savedInstanceState?.getString(KEY_TRANSITION_NAME)
                ?: intent.getStringExtra(KEY_TRANSITION_NAME) ?: ""
        container.transitionName = transitionName

        val toolbarColor = ContextCompat.getColor(this, R.color.black_translucent_40)
        toolbar.background = ColorDrawable(toolbarColor)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        // PhotoView doesn't play nice with shared element transitions if we rotate before exiting, so we
        // use this variable to skip the transition if the activity is re-created
        canTransitionOnFinish = savedInstanceState == null

        showToolbar(true)

        initializeViewModel()

        if (savedInstanceState?.getBoolean(KEY_IS_CONFIRMATION_SHOWING) == true) {
            confirmRemoveProductImage()
        }
    }

    private fun initializeViewModel() {
        setupObservers(viewModel)
        viewModel.start(remoteProductId)
    }

    private fun setupObservers(viewModel: ImageViewerViewModel) {
        viewModel.product.observe(this, Observer { product ->
            if (product.images.isEmpty()) {
                finishAfterTransition()
            } else {
                setupViewPager(product.images)
            }
        })

        viewModel.showSnackbarMessage.observe(this, Observer { msgId ->
            uiMessageResolver.showSnack(msgId)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.let { bundle ->
            bundle.putLong(KEY_IMAGE_REMOTE_PRODUCT_ID, remoteProductId)
            bundle.putLong(KEY_IMAGE_REMOTE_MEDIA_ID, remoteMediaId)
            bundle.putString(KEY_TRANSITION_NAME, transitionName)
            bundle.putBoolean(KEY_ENABLE_REMOVE_IMAGE, enableRemoveImage)
            bundle.putBoolean(KEY_DID_REMOVE_IMAGE, didRemoveImage)
            bundle.putBoolean(KEY_IS_CONFIRMATION_SHOWING, isConfirmationShowing)
            super.onSaveInstanceState(outState)
        }
    }

    override fun onPause() {
        confirmationDialog?.dismiss()
        super.onPause()
    }

    override fun finishAfterTransition() {
        // let the calling fragment know the product's images were changed
        if (didRemoveImage) {
            val data = Intent().also { it.putExtra(KEY_DID_REMOVE_IMAGE, true) }
            setResult(Activity.RESULT_OK, data)
        }

        if (canTransitionOnFinish) {
            super.finishAfterTransition()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (enableRemoveImage) {
            menuInflater.inflate(R.menu.menu_trash, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_trash -> {
                confirmRemoveProductImage()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            } else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onImageTapped() {
        showToolbar(true)
    }

    override fun onImageLoadError() {
        uiMessageResolver.showSnack(R.string.error_loading_image)
    }

    /**
     * Confirms that the user meant to remove this image from the product
     */
    private fun confirmRemoveProductImage() {
        isConfirmationShowing = true
        confirmationDialog = AlertDialog.Builder(ContextThemeWrapper(this, style.AppTheme))
                .setMessage(R.string.product_image_remove_confirmation)
                .setCancelable(true)
                .setPositiveButton(R.string.remove) { _, _ ->
                    removeProductImage()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    isConfirmationShowing = false
                }
                .show()
    }

    private fun removeProductImage() {
        val newImageCount = pagerAdapter.count - 1
        val currentMediaId = remoteMediaId

        // determine the image to return to when the adapter is reloaded following the image removal
        val newMediaId = if (newImageCount == 0) {
            0
        } else if (viewPager.currentItem > 0) {
            pagerAdapter.images[viewPager.currentItem - 1].id
        } else {
            pagerAdapter.images[viewPager.currentItem + 1].id
        }

        // animate the viewPager out, then remove the image and animate it back in - this gives
        // the appearance of the removed image being tossed away
        with(WooAnimUtils.getScaleOutAnim(viewPager)) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    didRemoveImage = true
                    canTransitionOnFinish = false
                }
                override fun onAnimationEnd(animation: Animator) {
                    remoteMediaId = newMediaId
                    viewModel.removeProductImage(currentMediaId)
                    // activity will finish if we removed the last image, so only scale back in
                    // if there are more images
                    if (newImageCount > 0) {
                        WooAnimUtils.scaleIn(viewPager)
                    }
                }
            })
            start()
        }
    }

    private fun showToolbar(show: Boolean) {
        if (!isFinishing) {
            // remove the current fade-out runnable and start a new one to hide the toolbar shortly after we show it
            fadeOutToolbarHandler.removeCallbacks(fadeOutToolbarRunnable)
            if (show) {
                fadeOutToolbarHandler.postDelayed(fadeOutToolbarRunnable, TOOLBAR_FADE_DELAY_MS)
            }

            if ((show && toolbar.visibility == View.VISIBLE) || (!show && toolbar.visibility != View.VISIBLE)) {
                return
            }

            @AnimRes val animRes = if (show) {
                R.anim.toolbar_fade_in_and_down
            } else {
                R.anim.toolbar_fade_out_and_up
            }

            val listener = object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    if (show) toolbar.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation) {
                    if (!show) toolbar.visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: Animation) {
                    // noop
                }
            }

            AnimationUtils.loadAnimation(this, animRes)?.let { anim ->
                anim.setAnimationListener(listener)
                toolbar.startAnimation(anim)
            }
        }
    }

    private val fadeOutToolbarRunnable = Runnable {
        showToolbar(false)
    }

    private fun setupViewPager(images: List<Product.Image>) {
        pagerAdapter = ImageViewerAdapter(supportFragmentManager, images)
        viewPager.adapter = pagerAdapter
        viewPager.pageMargin = resources.getDimensionPixelSize(R.dimen.margin_large)

        // determine the position of the original media item so we can page to it immediately
        for (index in images.indices) {
            if (remoteMediaId == images[index].id) {
                viewPager.currentItem = index
                break
            }
        }

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                showToolbar(true)
                // don't add an exit transition if the user swiped to another image
                canTransitionOnFinish = false
                // remember this image id so we can return to it upon rotation, and so
                // we use the right image if the user requests to remove it
                remoteMediaId = images[position].id
            }
        })
    }

    internal inner class ImageViewerAdapter(fm: FragmentManager, val images: List<Product.Image>) :
            FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return ImageViewerFragment.newInstance(images[position])
        }

        override fun getCount(): Int {
            return images.size
        }
    }
}
