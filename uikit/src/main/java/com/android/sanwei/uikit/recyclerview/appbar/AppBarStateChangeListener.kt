package com.android.sanwei.uikit.recyclerview.appbar

import androidx.annotation.IntDef
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

/**
 * Appbarstatechange to fix pulltorefresh issue
 */
public abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    companion object {
        const val EXPANDED = 1
        const val COLLAPSED = 2
        const val IDLE = 3
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        EXPANDED,
        COLLAPSED,
        IDLE
    )
    annotation class STATE {}

    @STATE
    private var mCurrentState: Int = IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, p1: Int) {
        when {
            p1 == 0 -> {
                if (mCurrentState != EXPANDED) {
                    onStateChanged(appBarLayout, EXPANDED)
                }
                mCurrentState = EXPANDED
            }
            abs(p1) >= appBarLayout.totalScrollRange -> {
                if (mCurrentState != COLLAPSED) {
                    onStateChanged(appBarLayout, COLLAPSED)
                }
                mCurrentState = COLLAPSED
            }
            else -> {
                if (mCurrentState != IDLE) {
                    onStateChanged(appBarLayout, IDLE)
                }
                mCurrentState = IDLE
            }
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, @STATE state: Int)
}