package co.innoshop.android.ui.main

import android.view.ViewGroup
import co.innoshop.android.R
import co.innoshop.android.di.ActivityScope
import co.innoshop.android.ui.base.UIMessageResolver
import javax.inject.Inject

@ActivityScope
class MainUIMessageResolver @Inject constructor(val activity: MainActivity) : UIMessageResolver {
    override val snackbarRoot: ViewGroup by lazy {
        activity.findViewById(R.id.snack_root) as ViewGroup
    }
}
