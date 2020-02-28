package co.innoshop.android.ui.reviews

import android.os.Parcelable
import co.innoshop.android.model.ActionRequest
import co.innoshop.android.model.ActionStatus
import co.innoshop.android.model.ActionStatus.PENDING
import co.innoshop.android.model.ProductReview
import kotlinx.android.parcel.Parcelize

@Parcelize
class ProductReviewModerationRequest(
    val productReview: ProductReview,
    val newStatus: ProductReviewStatus,
    private val requestStatus: ActionStatus = PENDING
) : ActionRequest(requestStatus), Parcelable
