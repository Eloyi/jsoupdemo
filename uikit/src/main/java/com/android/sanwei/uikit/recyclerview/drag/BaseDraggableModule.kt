package com.android.sanwei.uikit.recyclerview.drag

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.sanwei.uikit.recyclerview.CommonAdapter
import com.android.sanwei.uikit.recyclerview.CommonViewHolder

import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import com.android.sanwei.uikit.R

/**
 * drag and swipe module, can handle swap data by drag, and remove data by swipe
 */
class BaseDraggableModule(private val baseAdapter: CommonAdapter<*, *>) {

    companion object {
        private const val NO_TOGGLE_VIEW = 0
    }

    var isDragEnabled = false
    var isSwipeEnabled = false
    var toggleViewId = NO_TOGGLE_VIEW

    lateinit var itemTouchHelper: ItemTouchHelper
    lateinit var itemTouchHelperCallback: DragAndSwipeCallback

    protected var mOnToggleViewTouchListener: View.OnTouchListener? = null
    protected var mOnToggleViewLongClickListener: View.OnLongClickListener? = null
    protected var mOnItemDragListener: OnItemDragListener? = null
    protected var mOnItemSwipeListener: OnItemSwipeListener? = null

    init {
        initItemTouch()
    }

    private fun initItemTouch() {
        itemTouchHelperCallback = DragAndSwipeCallback(this)
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
    }

    internal fun initView(holder: CommonViewHolder) {
        if (isDragEnabled) {
            if (hasToggleView()) {
                val toggleView = holder.itemView.findViewById<View>(toggleViewId)
                if (toggleView != null) {
                    toggleView.setTag(R.id.BaseAdapter_viewholder_support, holder)
                    if (isDragOnLongPressEnabled) {
                        toggleView.setOnLongClickListener(mOnToggleViewLongClickListener)
                    } else {
                        toggleView.setOnTouchListener(mOnToggleViewTouchListener)
                    }
                }
            }
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * Is there a toggle view which will trigger drag event.
     */
    fun hasToggleView(): Boolean {
        return toggleViewId != NO_TOGGLE_VIEW
    }

    /**
     * Set the drag event should be trigger on long press.
     * Work when the toggleViewId has been set.
     *
     */
    var isDragOnLongPressEnabled = true
        set(value) {
            field = value
            if (value) {
                mOnToggleViewTouchListener = null
                mOnToggleViewLongClickListener = OnLongClickListener { v ->
                    if (isDragEnabled) {
                        itemTouchHelper.startDrag(v.getTag(R.id.BaseAdapter_viewholder_support) as RecyclerView.ViewHolder)
                    }
                    true
                }
            } else {
                mOnToggleViewTouchListener = OnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && !isDragOnLongPressEnabled) {
                        if (isDragEnabled) {
                            itemTouchHelper.startDrag(v.getTag(R.id.BaseAdapter_viewholder_support) as RecyclerView.ViewHolder)
                        }
                        true
                    } else {
                        false
                    }
                }
                mOnToggleViewLongClickListener = null
            }
        }


    protected fun getViewHolderPosition(viewHolder: RecyclerView.ViewHolder): Int {
        var result = viewHolder.adapterPosition
        return result - baseAdapter.getDataPosition()
    }

    /************************* Drag *************************/

    fun onItemDragStart(viewHolder: RecyclerView.ViewHolder) {
        mOnItemDragListener?.onItemDragStart(viewHolder, getViewHolderPosition(viewHolder))
    }

    fun onItemDragMoving(source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) {
        val from = getViewHolderPosition(source)
        val to = getViewHolderPosition(target)
        if (inRange(from) && inRange(to)) {
            baseAdapter.swapData(from, to)
        }
        mOnItemDragListener?.onItemDragMoving(source, from, target, to)
    }

    fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder) {
        mOnItemDragListener?.onItemDragEnd(viewHolder, getViewHolderPosition(viewHolder))
    }

    /************************* Swipe *************************/

    fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder) {
        if (isSwipeEnabled) {
            mOnItemSwipeListener?.onItemSwipeStart(viewHolder, getViewHolderPosition(viewHolder))
        }
    }

    fun onItemSwipeClear(viewHolder: RecyclerView.ViewHolder) {
        if (isSwipeEnabled) {
            mOnItemSwipeListener?.clearView(viewHolder, getViewHolderPosition(viewHolder))
        }
    }

    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder) {
        val pos = getViewHolderPosition(viewHolder)
        if (inRange(pos)) {
            baseAdapter.deletData(pos)
            if (isSwipeEnabled) {
                mOnItemSwipeListener?.onItemSwiped(viewHolder, pos)
            }
        }
    }

    fun onItemSwiping(
        canvas: Canvas?,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        isCurrentlyActive: Boolean
    ) {
        if (isSwipeEnabled) {
            mOnItemSwipeListener?.onItemSwipeMoving(canvas, viewHolder, dX, dY, isCurrentlyActive)
        }
    }

    private fun inRange(position: Int): Boolean {
        return position >= 0 && position < baseAdapter.data.size
    }

    fun setOnItemDragListener(onItemDragListener: OnItemDragListener?) {
        this.mOnItemDragListener = onItemDragListener
    }

    fun setOnItemSwipeListener(onItemSwipeListener: OnItemSwipeListener?) {
        this.mOnItemSwipeListener = onItemSwipeListener
    }


}