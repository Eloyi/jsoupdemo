package com.android.sanwei.uikit.recyclerview.refresh

import android.os.SystemClock
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.recyclerview.BaseDataWrapper
import com.android.sanwei.uikit.recyclerview.CommonAdapter
import com.android.sanwei.uikit.recyclerview.CommonViewHolder
import com.android.sanwei.uikit.recyclerview.appbar.AppBarStateChangeListener
import com.google.android.material.appbar.AppBarLayout
import java.util.*

/**
 * refresh module handle pull to refresh
 */
class BaseRefreshModule(private val baseAdapter: CommonAdapter<*, *>) {

    var DRAG_RATE = 2

    private var uuid: UUID = UUID.randomUUID()
    private var time: Long = SystemClock.elapsedRealtime()

    private var refreshListener: OnRefreshListener? = null
    private var refreshLayout: RefreshLayout? = null
    @AppBarStateChangeListener.STATE
    private var appbarState: Int? = AppBarStateChangeListener.EXPANDED
    var enableRefresh = true

    val refreshCallback = object : RefreshCallback {
        override fun onRefreshComplete(uuid: String) {
            if (checkExiped(uuid)) return
            refreshComplete()
        }

        override fun onRefreshFailed(uuid: String) {
            if (checkExiped(uuid)) return
            refreshComplete()
        }
    }

    private var mLastY: Float = -1f

    private val mListTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event == null) {
                return false
            }
            if (mLastY == -1f) {
                mLastY = event.rawY
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLastY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - mLastY
                    mLastY = event.rawY
                    if (canRefresh()) {
                        refreshLayout?.onMove(deltaY / DRAG_RATE)

                        refreshLayout?.let {
                            if (it.getVisibleHeight() > 0 && it.getState() < RefreshLayout.STATE_REFRESHING) {
                                return true
                            }
                        }
                    }
                }
                else -> {
                    mLastY = -1f
                    if (canRefresh()) {
                        if (refreshLayout?.releaseType() == true) {
                            updateUUid()
                            refreshListener?.onRefresh(uuid.toString(), refreshCallback)
                        }
                    }
                }
            }
            return false
        }
    }

    fun enableRefresh(): Boolean {
        return enableRefresh
    }

    fun canRefresh(): Boolean {
        return enableRefresh && appbarState == AppBarStateChangeListener.EXPANDED
    }

    fun setRefreshListener(listener: OnRefreshListener?) {
        refreshListener = listener
    }

    fun checkExiped(tag: String): Boolean {
        return !TextUtils.equals(uuid.toString(), tag)
    }

    fun refreshComplete() {
        refreshLayout?.refreshComplete()
    }

    fun convertRefresh(helper: CommonViewHolder) {
        if (refreshLayout == null) {
            refreshLayout = helper.getView(R.id.refresh)
        }
    }

    fun updateUUid() {
        uuid = UUID.randomUUID()

        time = SystemClock.elapsedRealtime()
    }

    fun buildList(list: MutableList<BaseDataWrapper>): Boolean {
        if (enableRefresh() && !hasAddedRefresh(list)) {
            baseAdapter.addRefreshData()
            return true
        } else if (!enableRefresh() && hasAddedRefresh(list)) {
            baseAdapter.removeRefreshData()
            return true
        }
        return false
    }

    fun hasAddedRefresh(list: MutableList<BaseDataWrapper>): Boolean {
        if (list.isEmpty()) {
            return false
        }
        for (baseDataWrapper in list) {
            if (baseDataWrapper.type == BaseDataWrapper.REFRESH) {
                return true
            } else if (baseDataWrapper.type == BaseDataWrapper.REALDATA) {
                return false
            }
        }
        return false
    }

    fun reset() {
        refreshLayout?.reset()
    }

    internal fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setOnTouchListener(mListTouchListener)
        var appbarLayout: AppBarLayout? = null
        var parent: ViewParent? = recyclerView.parent
        while (parent != null) {
            if (parent is CoordinatorLayout) {
                break
            }
            parent = parent.parent
        }
        if (parent is CoordinatorLayout) {
            val childCount = parent.childCount
            for (item in 0..childCount) {
                val child = parent.getChildAt(item)
                if (child is AppBarLayout) {
                    appbarLayout = child
                    break
                }
            }
        }
        appbarLayout?.let {
            it.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout, state: Int) {
                    appbarState = state
                }
            })
        }
    }
}