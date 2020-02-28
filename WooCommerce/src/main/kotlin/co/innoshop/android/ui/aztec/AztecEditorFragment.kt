package co.innoshop.android.ui.aztec

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import co.innoshop.android.R
import co.innoshop.android.analytics.AnalyticsTracker
import co.innoshop.android.analytics.AnalyticsTracker.Stat.AZTEC_EDITOR_DONE_BUTTON_TAPPED
import co.innoshop.android.extensions.navigateBackWithResult
import co.innoshop.android.ui.base.BaseFragment
import co.innoshop.android.ui.dialog.CustomDiscardDialog
import co.innoshop.android.ui.main.MainActivity
import co.innoshop.android.ui.main.MainActivity.Companion.BackPressListener
import kotlinx.android.synthetic.main.fragment_aztec_editor.*
import org.wordpress.android.util.ActivityUtils
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText.EditorHasChanges.NO_CHANGES
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.glideloader.GlideImageLoader
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener

class AztecEditorFragment : BaseFragment(), IAztecToolbarClickListener, BackPressListener {
    companion object {
        const val TAG: String = "AztecEditorFragment"
        const val AZTEC_EDITOR_REQUEST_CODE = 3001
        const val ARG_AZTEC_EDITOR_TEXT = "editor-text"
        const val ARG_AZTEC_HAS_CHANGES = "editor-has-changes"
        private const val FIELD_IS_CONFIRMING_DISCARD = "is_confirming_discard"
        private const val FIELD_IS_HTML_EDITOR_ENABLED = "is_html_editor_enabled"
    }

    private lateinit var aztec: Aztec

    private val navArgs: co.innoshop.android.ui.aztec.AztecEditorFragmentArgs by navArgs()

    private var isConfirmingDiscard = false
    private var shouldShowDiscardDialog = true
    private var isHtmlEditorEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_aztec_editor, container, false)
    }

    override fun getFragmentTitle() = navArgs.aztecTitle

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.hideBottomNav()

        aztec = Aztec.with(visualEditor, sourceEditor, aztecToolbar, this)
                .setImageGetter(GlideImageLoader(requireContext()))

        aztec.initSourceEditorHistory()

        aztec.visualEditor.fromHtml(navArgs.aztecText)
        aztec.sourceEditor?.displayStyledAndFormattedHtml(navArgs.aztecText)

        savedInstanceState?.let { state ->
            isHtmlEditorEnabled = state.getBoolean(FIELD_IS_HTML_EDITOR_ENABLED)
            if (state.getBoolean(FIELD_IS_CONFIRMING_DISCARD)) {
                confirmDiscard()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                AnalyticsTracker.track(AZTEC_EDITOR_DONE_BUTTON_TAPPED)
                shouldShowDiscardDialog = false
                navigateBackWithResult(editorHasChanges())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FIELD_IS_CONFIRMING_DISCARD, isConfirmingDiscard)
        outState.putBoolean(FIELD_IS_HTML_EDITOR_ENABLED, isHtmlEditorEnabled)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onStop() {
        super.onStop()
        CustomDiscardDialog.onCleared()
        activity?.let {
            ActivityUtils.hideKeyboard(it)
        }
    }

    /**
     * Prevent back press in the main activity if the user made changes so we can confirm the discard
     */
    override fun onRequestAllowBackPress(): Boolean {
        return if (editorHasChanges() && shouldShowDiscardDialog) {
            confirmDiscard()
            false
        } else {
            true
        }
    }

    private fun confirmDiscard() {
        isConfirmingDiscard = true
        CustomDiscardDialog.showDiscardDialog(
                requireActivity(),
                posBtnAction = DialogInterface.OnClickListener { _, _ ->
                    isConfirmingDiscard = false
                    navigateBackWithResult(false)
                },
                negBtnAction = DialogInterface.OnClickListener { _, _ ->
                    isConfirmingDiscard = false
                })
    }

    override fun onToolbarCollapseButtonClicked() {
        // Aztec Toolbar interface methods implemented by default with Aztec. Currently not used
    }

    override fun onToolbarExpandButtonClicked() {
        // Aztec Toolbar interface methods implemented by default with Aztec. Currently not used
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
        // Aztec Toolbar interface methods implemented by default with Aztec. Currently not used
    }

    override fun onToolbarHeadingButtonClicked() {
        // Aztec Toolbar interface methods implemented by default with Aztec. Currently not used
    }

    override fun onToolbarHtmlButtonClicked() {
        aztec.toolbar.toggleEditorMode()
        isHtmlEditorEnabled = !isHtmlEditorEnabled
    }

    override fun onToolbarListButtonClicked() {
        // Aztec Toolbar interface methods implemented by default with Aztec. Currently not used
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

    private fun getEditorText(): String? {
        return if (isHtmlEditorEnabled) {
            aztec.sourceEditor?.getPureHtml(false)
        } else {
            aztec.visualEditor.toHtml()
        }
    }

    private fun editorHasChanges(): Boolean {
        val hasChanges = if (isHtmlEditorEnabled) {
            aztec.sourceEditor?.hasChanges()
        } else {
            aztec.visualEditor.hasChanges()
        }
        return hasChanges != NO_CHANGES
    }

    private fun navigateBackWithResult(hasChanges: Boolean) {
        val bundle = Bundle()
        bundle.putString(ARG_AZTEC_EDITOR_TEXT, getEditorText())
        bundle.putBoolean(ARG_AZTEC_HAS_CHANGES, hasChanges)
        requireActivity().navigateBackWithResult(
                AZTEC_EDITOR_REQUEST_CODE,
                bundle,
                R.id.nav_host_fragment_main,
                R.id.productDetailFragment
        )
    }
}
