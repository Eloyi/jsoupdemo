package com.android.sanwei.uikit.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider
import com.android.sanwei.uikit.recyclerview.diff.CommonDiffCallback
import com.android.sanwei.uikit.recyclerview.diff.CommonDiffConfig
import com.android.sanwei.uikit.recyclerview.diff.DiffHelper
import com.android.sanwei.uikit.recyclerview.drag.BaseDraggableModule
import com.android.sanwei.uikit.recyclerview.loadmore.BaseLoadMoreModule
import com.android.sanwei.uikit.recyclerview.refresh.BaseRefreshModule
import java.lang.ref.WeakReference
import java.util.*

/**
 *common adapter handle refresh, drag, loadmore,header,foot, diff action
 *
 * list for all recyclerview item, wrapped by BaseDataWrapper, include refresh,header,data,footer,
 * loadmore,etc.. outside like project can't direct handle this list.
 * data for real data,outside can use addData(),deletData(),setNewData(),swapData()
 */
abstract class CommonAdapter<T, VH : CommonViewHolder>(
    @LayoutRes private val layoutRes: Int,
    private var enableDragModule: Boolean = false,
    private var enableLoadMoreModule: Boolean = false,
    private var enableRefreshModule: Boolean = false
) :
    RecyclerView.Adapter<VH>() {

    //CommonList
    var list = mutableListOf<BaseDataWrapper>()
    //real data
    var data: MutableList<T> = mutableListOf()

    private var enableHeader: Boolean = false
    private var enableFooter: Boolean = false
    private var enableLoadMore: Boolean = false
    private var headerId: Int? = null
    private var footerId: Int? = null

    protected lateinit var context: Context
        private set

    lateinit var weakRecyclerView: WeakReference<RecyclerView>

    var draggableModule: BaseDraggableModule? = null

    var loadmoreModule: BaseLoadMoreModule? = null

    var refreshModule: BaseRefreshModule? = null

    private var mDiffHelper: DiffHelper<T>? = null

    init {
        if (enableDragModule) {
            draggableModule = BaseDraggableModule(this)
        }
        if (enableLoadMoreModule) {
            loadmoreModule = BaseLoadMoreModule(this)
        }
        if (enableRefreshModule) {
            refreshModule = BaseRefreshModule(this)
        }

        //init base diffutil settings
        val diffCallback = object : CommonDiffCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return diffAreItemsTheSame(oldItem, newItem)
            }
        }

        val config = CommonDiffConfig.Builder(diffCallback)
            .setMainThreadExecutor(UikitProvider.getMainThreadExecutor())
            .setBackgroundThreadExecutor(UikitProvider.getBackgroundThreadExecutor()).build()

        setDiffConfig(config)
    }

    /**
     * =============================================================================================
     * protected method need override
     * =============================================================================================
     */
    protected abstract fun convert(helper: VH, item: T?)

    protected open fun convert(helper: VH, item: T, payloads: List<Any>) {}

    protected open fun diffAreItemsTheSame(oldItem: T, newItem: T): Boolean {
        return false
    }

    protected open fun getLoadMoreLayout(): Int? {
        return loadmoreModule?.getLayoutId()
    }

    protected open fun convertLoadMore(helper: VH, wrapper: BaseDataWrapper) {
        loadmoreModule?.convertLoadMore(helper)
    }

    protected open fun convertRefresh(helper: VH, wrapper: BaseDataWrapper) {
        refreshModule?.convertRefresh(helper)
    }

    protected open fun convertHeader(helper: VH, wrapper: BaseDataWrapper) {}

    protected open fun convertFooter(helper: VH, wrapper: BaseDataWrapper) {}

    protected open fun onItemViewHolderCreated(viewHolder: VH, viewType: Int) {}

    protected open fun getDefViewHolderLayout(viewType: Int): Int? {
        return layoutRes
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        var layoutId = layoutRes
        when (viewType) {
            BaseDataWrapper.REFRESH -> {
                layoutId = R.layout.common_refresh_layout
            }
            BaseDataWrapper.LOADMORE -> {
                layoutId = getLoadMoreLayout() ?: layoutRes
            }
            BaseDataWrapper.HEADER -> {
                layoutId = headerId ?: layoutRes
            }
            BaseDataWrapper.FOOTER -> {
                layoutId = footerId ?: layoutRes
            }
            BaseDataWrapper.REALDATA -> {
                layoutId = layoutRes
            }
            else -> {
                layoutId = getDefViewHolderLayout(viewType) ?: layoutId
            }
        }
        val view: View = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        val viewHolder = CommonViewHolder(view) as VH

        if (viewType == BaseDataWrapper.REALDATA) {
            draggableModule?.initView(viewHolder)
            onItemViewHolderCreated(viewHolder, viewType)
        }
        return viewHolder
    }


    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder.itemViewType) {
            BaseDataWrapper.REFRESH -> {
                convertRefresh(holder, list[position])
            }
            BaseDataWrapper.LOADMORE -> {
                convertLoadMore(holder, list[position])
            }
            BaseDataWrapper.HEADER -> {
                convertHeader(holder, list[position])
            }
            BaseDataWrapper.FOOTER -> {
                convertFooter(holder, list[position])
            }
            else -> {
                convert(holder, list[position].data as T)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        weakRecyclerView = WeakReference(recyclerView)
        this.context = recyclerView.context
        draggableModule?.attachToRecyclerView(recyclerView)
        refreshModule?.attachToRecyclerView(recyclerView)

        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (val type = getItemViewType(position)) {
                        BaseDataWrapper.REFRESH -> manager.spanCount
                        BaseDataWrapper.HEADER -> manager.spanCount
                        BaseDataWrapper.FOOTER -> manager.spanCount
                        BaseDataWrapper.LOADMORE -> manager.spanCount
                        else -> getItemSpanCount(type, manager.spanCount)
                    }
                }
            }
        }
    }

    open fun getItemSpanCount(type: Int, spanCount: Int): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        val wrapper = list[position]

        return when (wrapper.type) {
            BaseDataWrapper.REALDATA -> {
                getDefItemViewType(wrapper.getOrignPosition())
            }
            else -> wrapper.type
        }
    }

    protected open fun getDefItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    private fun buildList(checkDataDetail: Boolean = false): Boolean {
        var resultChanged: Boolean = false

        var currentPosition = 0

        resultChanged = refreshModule?.buildList(list) ?: false

        currentPosition = if (refreshModule?.hasAddedRefresh(list) == true) 1 else 0

        if (enableHeader && !hasAddedHeader()) {
            addHeaderData(currentPosition)
            resultChanged = true
        } else if (!enableHeader && hasAddedHeader()) {
            removeHeaderData(currentPosition)
            resultChanged = true
        }

        currentPosition += (if (hasAddedHeader()) 1 else 0)

        val oldhasFooter = hasAddedFooter()

        //sync realdata with common list
        if (!hasSameData(currentPosition, oldhasFooter, checkDataDetail)) {
            writeDataInList(currentPosition)
            resultChanged = true
        }

        currentPosition += data.size

        if (enableFooter && !hasAddedFooter()) {
            addFooterData(currentPosition)
            resultChanged = true
        } else if (!enableFooter && hasAddedFooter()) {
            removeFooterData(currentPosition)
            resultChanged = true
        }

        val result: Boolean = loadmoreModule?.buildList(list) ?: false

        if (result) {
            resultChanged = true
        }

        return resultChanged
    }

    fun initialize() {
        val dataChanged = buildList()
        if (dataChanged) {
            notifyDataSetChanged()
        }
    }

    fun enableHeader(enable: Boolean, resId: Int? = null) {
        enableHeader = enable
        headerId = resId
        //rebuildDataList And refresh
        val dataChanged = buildList()
        if (dataChanged) {
            notifyDataSetChanged()
        }
    }

    fun enableFooter(enable: Boolean, resId: Int? = null) {
        enableFooter = enable
        footerId = resId
        //rebuildDataList And refresh
        val dataChanged = buildList()
        if (dataChanged) {
            notifyDataSetChanged()
        }
    }

    private fun hasAddedHeader(): Boolean {
        if (list.isEmpty()) {
            return false
        }
        for (baseDataWrapper in list) {
            if (baseDataWrapper.type == BaseDataWrapper.HEADER) {
                return true
            } else if (baseDataWrapper.type == BaseDataWrapper.REALDATA) {
                return false
            }
        }
        return false
    }

    private fun hasAddedFooter(): Boolean {
        if (list.isEmpty()) return false

        val length = list.size - 1

        for (i in length downTo 0) {
            val type = list.get(i).type

            if (type == BaseDataWrapper.FOOTER) {
                return true
            } else if (type == BaseDataWrapper.REALDATA) {
                return false
            }
        }
        return false
    }

    internal fun hasSameData(
        extraPosition: Int,
        hasFooter: Boolean,
        checkDataDetail: Boolean
    ): Boolean {
        var extraSize = extraPosition
        var extraHeaderRefreshSize = extraPosition

        if (hasFooter) {
            extraSize += 1
        }
        if (list.size != data.size + extraSize) {
            return false
        }

        if (!checkDataDetail) {
            return true
        }

        list.forEachIndexed { index, baseDataWrapper ->
            val type = baseDataWrapper.type
            if (type == BaseDataWrapper.REALDATA) {
                if (list.get(index).data != data.get(index - extraHeaderRefreshSize)) {
                    return false
                }
            }
        }
        return true
    }

    internal fun getDataPosition(): Int {
        var currentPosition = 0

        currentPosition += if (refreshModule?.hasAddedRefresh(list) == true) 1 else 0
        currentPosition += if (hasAddedHeader()) 1 else 0

        return currentPosition
    }

    internal fun writeDataInList(position: Int = getDataPosition(), filterOldData : Boolean = true, inserData : MutableList<T> = data) {
        //remove old data
        if (filterOldData){
            list = list.filter { it.type != BaseDataWrapper.REALDATA }.toMutableList()
        }

        inserData.forEachIndexed { index, t ->
            list.add(
                index + position,
                BaseDataWrapper(BaseDataWrapper.REALDATA, t, SimpleRealDataInfo(t, data))
            )
        }
    }

    fun setNewData(newData: MutableList<T>?) {
        data = newData ?: mutableListOf()
        loadmoreModule?.reset()
        refreshModule?.reset()
        val dataChanged = buildList(true)
        if (dataChanged) {
            notifyDataSetChanged()
        }
    }

    fun setDiffNewData(newData: MutableList<T>?) {
        if (data.isEmpty()) {
            setNewData(newData)
            return
        }
        loadmoreModule?.reset()
//        refreshModule?.reset()

        mDiffHelper?.submitList(newData, Runnable {
            buildList(true)
        })
    }

    fun addData(newData: MutableList<T>?) {
        newData?.let {
            data.addAll(newData)
            loadmoreModule?.reset()
            refreshModule?.reset()
            val dataChanged = buildList(true)
            if (dataChanged) {
                notifyDataSetChanged()
            }
        }
    }

    fun addItemsData(position: Int, insertList: MutableList<T>) {
        data.addAll(position, insertList)

        val listInsertPosition = position + getDataPosition()

        writeDataInList(listInsertPosition, filterOldData = false, inserData = insertList)

        notifyItemRangeInserted(listInsertPosition, insertList.size)

    }

    fun deleteDataRange(start: Int, count: Int) {
        data.subList(start, start + count).clear()

        var listRemovePosition = start

        listRemovePosition += getDataPosition()

        deleteListRangeData(listRemovePosition, count)
    }

    fun deletData(position: Int) {
        data.removeAt(position)

        var listRemovePosition = position

        listRemovePosition += getDataPosition()

        deleteListData(listRemovePosition)
    }

    internal fun deleteListData(position: Int) {

        list.removeAt(position)

        notifyItemRemoved(position)
    }

    internal fun deleteListRangeData(position: Int, count: Int) {

        list.subList(position, position + count).clear()

        notifyItemRangeRemoved(position, count)
    }

    internal fun notifyRealListItemChanged(position: Int){
        var listPosition = position

        listPosition += getDataPosition()

        notifyItemChanged(listPosition)
    }
    internal fun notifyRealListItemRangeChanged(position: Int, count: Int){
        var listPosition = position

        listPosition += getDataPosition()

        notifyItemRangeChanged(listPosition, count)
    }

    fun swapData(from: Int, to: Int) {
        val extraPosition = getDataPosition()

        if (from < to) {
            for (i in from until to) {
                Collections.swap(data, i, i + 1)
                Collections.swap(list, i + extraPosition, i + 1 + extraPosition)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(data, i, i - 1)
                Collections.swap(list, i + extraPosition, i - 1 + extraPosition)
            }
        }
        notifyItemMoved(from + extraPosition, to + extraPosition)
    }

    fun setDiffConfig(config: CommonDiffConfig<T>) {
        mDiffHelper = DiffHelper(this, config)
    }

    fun setDiffCallback(diffCallback: DiffUtil.ItemCallback<T>) {
        this.setDiffConfig(CommonDiffConfig.Builder(diffCallback).build())
    }

    internal fun addRefreshData() {
        list.add(0, BaseDataWrapper(BaseDataWrapper.REFRESH))
    }

    internal fun removeRefreshData() {
        list.removeAt(0)
    }

    private fun addHeaderData(position: Int) {

        list.add(position, BaseDataWrapper(BaseDataWrapper.HEADER))
    }

    private fun removeHeaderData(position: Int) {

        list.removeAt(position)
    }

    private fun addFooterData(position: Int) {
        list.add(position, BaseDataWrapper(BaseDataWrapper.FOOTER))

    }

    private fun removeFooterData(position: Int) {
        list.removeAt(position)
    }

}