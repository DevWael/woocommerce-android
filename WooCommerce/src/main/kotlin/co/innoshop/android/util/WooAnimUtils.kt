package co.innoshop.android.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation

object WooAnimUtils {
    enum class Duration {
        SHORT,
        MEDIUM,
        LONG;

        fun toMillis(context: Context): Long {
            return when (this) {
                SHORT -> context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                MEDIUM -> context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                LONG -> context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
            }
        }
    }

    private val DEFAULT_DURATION = Duration.SHORT

    fun fadeIn(target: View, animDuration: Duration = DEFAULT_DURATION) {
        with(ObjectAnimator.ofFloat(target, View.ALPHA, 0.0f, 1.0f)) {
            duration = animDuration.toMillis(target.context)
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    target.visibility = View.VISIBLE
                }
            })
            start()
        }
    }

    fun fadeOut(target: View, animDuration: Duration = DEFAULT_DURATION, endVisibility: Int = View.GONE) {
        with(ObjectAnimator.ofFloat(target, View.ALPHA, 1.0f, 0.0f)) {
            duration = animDuration.toMillis(target.context)
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    target.visibility = endVisibility
                }
            })
            start()
        }
    }

    fun scaleIn(target: View, animDuration: Duration = DEFAULT_DURATION) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f)

        with(ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY)) {
            duration = animDuration.toMillis(target.context)
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    target.visibility = View.VISIBLE
                }
            })
            start()
        }
    }

    fun scaleOut(target: View, animDuration: Duration = DEFAULT_DURATION) {
        with(getScaleOutAnim(target, animDuration)) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    target.visibility = View.GONE
                }
            })
            start()
        }
    }

    fun getScaleOutAnim(target: View, animDuration: Duration = DEFAULT_DURATION): ObjectAnimator {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f)

        with(ObjectAnimator.ofPropertyValuesHolder(target, scaleX, scaleY)) {
            duration = animDuration.toMillis(target.context)
            interpolator = AccelerateDecelerateInterpolator()
            return this
        }
    }

    fun animateBottomBar(view: View, show: Boolean, duration: Duration = DEFAULT_DURATION) {
        animateBar(view, show, false, duration)
    }

    private fun animateBar(view: View?, show: Boolean, isTopBar: Boolean, duration: Duration) {
        val newVisibility = if (show) View.VISIBLE else View.GONE

        if (view == null || view.visibility == newVisibility) {
            return
        }

        val fromY: Float
        val toY: Float
        if (isTopBar) {
            fromY = if (show) -1f else 0f
            toY = if (show) 0f else -1f
        } else {
            fromY = if (show) 1f else 0f
            toY = if (show) 0f else 1f
        }
        val animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, fromY,
                Animation.RELATIVE_TO_SELF, toY)

        val durationMillis = duration.toMillis(view.context)
        animation.duration = durationMillis

        if (show) {
            animation.interpolator = DecelerateInterpolator()
        } else {
            animation.interpolator = AccelerateInterpolator()
        }

        view.clearAnimation()
        view.startAnimation(animation)
        view.visibility = newVisibility
    }
}
