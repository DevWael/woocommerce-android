package co.innoshop.android.ui.imageviewer

import android.view.ViewGroup
import co.innoshop.android.R
import co.innoshop.android.di.ActivityScope
import co.innoshop.android.ui.base.UIMessageResolver
import javax.inject.Inject

@ActivityScope
class ImageViewerUIMessageResolver @Inject constructor(val activity: ImageViewerActivity) : UIMessageResolver {
    override val snackbarRoot: ViewGroup by lazy {
        activity.findViewById(R.id.container) as ViewGroup
    }
}
