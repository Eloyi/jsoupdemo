package com.android.sanwei.uikit.recyclerview.loadmore

import android.os.SystemClock
import android.text.TextUtils
import com.android.sanwei.uikit.recyclerview.BaseDataWrapper
import com.android.sanwei.uikit.recyclerview.CommonAdapter
import com.android.sanwei.uikit.recyclerview.CommonViewHolder
import java.util.*

/**
 * Load more module setting must after setNewData
 */
class BaseLoadMoreModule(private val baseAdapter: CommonAdapter<*, *>)  {

    var TIME_DELAY_ENABLE_LOAD : Long= 1000
    //use uuid control which time start loadmore and which data can be received
    private var uuid: UUID = UUID.randomUUID()
    private var time: Long = SystemClock.elapsedRealtime()

    private var loadmoreView: BaseLoadMoreView? = SimpleLoadMoreView()

    private var loadmoreListener: OnLoadMoreListener? = null

    var loadMoreStatus = BaseLoadMoreView.COMPLETE
        private set

    var isLoadEndMoreGone: Boolean = true
        private set

    var enableLoadMoreEndClick = false

    var isAutoLoadMore = true

    val loadmoreCallback = object : LoadMoreCallback {
        override fun onLoadComplete(uuid: String) {
            if (checkExiped(uuid)) return
            loadComplete()
        }

        override fun onLoadFailed(uuid: String) {
            if (checkExiped(uuid)) return
            loadFailed()
        }

        override fun onLoadEnd(uuid: String, gone: Boolean) {
            if (checkExiped(uuid)) return
            loadEnd(gone)
        }
    }

    val isLoading: Boolean
        get() {
            return loadMoreStatus == BaseLoadMoreView.LOADING
        }

    fun getLayoutId(): Int? {
        return loadmoreView?.getRootView()
    }

    fun buildList(list: MutableList<BaseDataWrapper>): Boolean {

        val hasLoadMore = hasAddedLoadMore()
        val needDisplay = needLoadMoreDisplay()

        if (needDisplay && !hasLoadMore) {
            list.add(BaseDataWrapper(BaseDataWrapper.LOADMORE))
            return true
        } else if (!needDisplay && hasLoadMore) {
            list.removeAt(list.size - 1)
            return true
        }

        return false
    }

    fun convertLoadMore(helper: CommonViewHolder) {

        helper.itemView.setOnClickListener {
            loadMoreStatus = BaseLoadMoreView.LOADING
            loadmoreView?.convert(helper, loadMoreStatus)
            updateUUid()
            loadmoreListener?.onLoadMore(uuid.toString(), loadmoreCallback)
        }

        if (SystemClock.elapsedRealtime() - time < TIME_DELAY_ENABLE_LOAD){

            loadmoreView?.convert(helper, loadMoreStatus)

            return
        }

        updateUUid()

        loadMoreStatus = BaseLoadMoreView.LOADING

        loadmoreView?.convert(helper, loadMoreStatus)

        loadmoreListener?.onLoadMore(uuid.toString(), loadmoreCallback)
    }

    fun updateUUid()
    {
        uuid = UUID.randomUUID()

        time = SystemClock.elapsedRealtime()
    }

    fun hasAddedLoadMore(): Boolean {
        var list = baseAdapter.list

        if (list.isEmpty()) return false

        val length = list.size - 1

        for (i in length downTo 0) {
            val type = list.get(i).type

            if (type == BaseDataWrapper.LOADMORE) {
                return true
            } else if (type == BaseDataWrapper.REALDATA) {
                return false
            }
        }
        return false
    }

    /**
     * you need make sure list notify immediately after call reset
     */
    internal fun reset() {
        loadMoreStatus = BaseLoadMoreView.COMPLETE

        updateUUid()
    }

    fun needLoadMoreDisplay(): Boolean {
        if (loadMoreStatus != BaseLoadMoreView.END)
        {
            return true
        }else if (!isLoadEndMoreGone){
            return true
        }
        return false
    }

    fun setOnLoadMoreListener(listener: OnLoadMoreListener?) {
        this.loadmoreListener = listener
    }

    fun setLoadMoreView(view : BaseLoadMoreView){
        if (!hasAddedLoadMore())
        {
            loadmoreView = view
        }else {
            refreshLoadMoveView()
        }
    }

    fun checkExiped(tag : String) : Boolean{
        return !TextUtils.equals(uuid.toString(), tag)
    }

    fun refreshLoadMoveView()
    {
        updateUUid()

        baseAdapter.notifyItemChanged(baseAdapter.list.size - 1)
    }

    fun loadComplete() {

        if (!hasAddedLoadMore()){
            return
        }

        loadMoreStatus = BaseLoadMoreView.COMPLETE

        refreshLoadMoveView()
    }

    fun loadFailed() {
        if (!hasAddedLoadMore()){
            return
        }

        loadMoreStatus = BaseLoadMoreView.FAIL

        refreshLoadMoveView()

    }

    fun loadEnd(gone : Boolean) {
        if (!hasAddedLoadMore()){
            return
        }
        isLoadEndMoreGone = gone

        loadMoreStatus = BaseLoadMoreView.END

        if (gone){
            baseAdapter.deleteListData(baseAdapter.itemCount - 1)
        }else {
            refreshLoadMoveView()
        }
    }

}