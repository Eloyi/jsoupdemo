package com.android.sanwei.uikit.recyclerview.loadmore

import android.view.View
import androidx.annotation.IntDef
import com.android.sanwei.uikit.recyclerview.CommonViewHolder

abstract class BaseLoadMoreView {
    companion object {
        const val COMPLETE: Int = 1
        const val LOADING: Int = 2
        const val FAIL: Int = 3
        const val END: Int = 4
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(COMPLETE, LOADING, FAIL, END)
    annotation class LOADING_STATUS {}

    abstract fun getRootView(): Int

    abstract fun getLoadingView(holder: CommonViewHolder): View?

    abstract fun getLoadComplete(holder: CommonViewHolder): View?

    abstract fun getLoadEndView(holder: CommonViewHolder): View?

    abstract fun getLoadFailView(holder: CommonViewHolder): View?

    open fun convert(holder: CommonViewHolder, @LOADING_STATUS loadMoreStatus: Int) {
        when (loadMoreStatus) {
            COMPLETE -> {
                setVisible(getLoadingView(holder), false)
                setVisible(getLoadComplete(holder), true)
                setVisible(getLoadFailView(holder), false)
                setVisible(getLoadEndView(holder), false)
            }
            LOADING -> {
                setVisible(getLoadingView(holder), true)
                setVisible(getLoadComplete(holder), false)
                setVisible(getLoadFailView(holder), false)
                setVisible(getLoadEndView(holder), false)
            }
            FAIL -> {
                setVisible(getLoadingView(holder), false)
                setVisible(getLoadComplete(holder), false)
                setVisible(getLoadFailView(holder), true)
                setVisible(getLoadEndView(holder), false)
            }
            END -> {
                setVisible(getLoadingView(holder), false)
                setVisible(getLoadComplete(holder), false)
                setVisible(getLoadFailView(holder), false)
                setVisible(getLoadEndView(holder), true)
            }
        }
    }

    fun setVisible(view: View?, visible: Boolean) {
        view?.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


}