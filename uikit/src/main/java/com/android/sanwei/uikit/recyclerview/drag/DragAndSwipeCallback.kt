package com.android.sanwei.uikit.recyclerview.drag

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.sanwei.uikit.recyclerview.BaseDataWrapper
import com.android.sanwei.uikit.R

class DragAndSwipeCallback(private val draggableModule: BaseDraggableModule) : ItemTouchHelper.Callback() {

    var mMoveThreshold: Float = 0.1f
    var mSwipeThreshold: Float = 0.7f

    var mDragMoveFlags =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    var mSwipeMoveFlags = ItemTouchHelper.START

    override fun isLongPressDragEnabled(): Boolean {
        return draggableModule.isDragEnabled && !draggableModule.hasToggleView()
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return draggableModule.isSwipeEnabled
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder == null) {
            super.onSelectedChanged(viewHolder, actionState)
            return
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && !isViewCreateByAdapter(viewHolder)) {
            draggableModule.onItemDragStart(viewHolder)
            viewHolder.itemView.setTag(R.id.BaseAdapter_dragging_support, true)
        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && !isViewCreateByAdapter(
                viewHolder
            )
        ) {
            draggableModule.onItemSwipeStart(viewHolder)
            viewHolder.itemView.setTag(R.id.BaseAdapter_swiping_support, true)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (isViewCreateByAdapter(viewHolder)) {
            return
        }
        if (viewHolder.itemView.getTag(R.id.BaseAdapter_dragging_support) != null && viewHolder.itemView.getTag(
                R.id.BaseAdapter_dragging_support
            ) as Boolean
        ) {
            draggableModule.onItemDragEnd(viewHolder)
            viewHolder.itemView.setTag(R.id.BaseAdapter_dragging_support, false)
        }
        if (viewHolder.itemView.getTag(R.id.BaseAdapter_swiping_support) != null && viewHolder.itemView.getTag(
                R.id.BaseAdapter_swiping_support
            ) as Boolean
        ) {
            draggableModule.onItemSwipeClear(viewHolder)
            viewHolder.itemView.setTag(R.id.BaseAdapter_swiping_support, false)
        }
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (isViewCreateByAdapter(viewHolder)) {
            return makeMovementFlags(0, 0)
        }
        return makeMovementFlags(mDragMoveFlags, mSwipeMoveFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return viewHolder.itemViewType == target.itemViewType
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        draggableModule.onItemDragMoving(viewHolder, target)
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE
            && !isViewCreateByAdapter(viewHolder)
        ) {
            val itemView = viewHolder.itemView
            c.save()
            if (dX > 0) {
                c.clipRect(
                    itemView.left.toFloat(), itemView.top.toFloat(),
                    itemView.left + dX, itemView.bottom.toFloat()
                )
                c.translate(itemView.left.toFloat(), itemView.top.toFloat())
            } else {
                c.clipRect(
                    itemView.right + dX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat()
                )
                c.translate(itemView.right + dX, itemView.top.toFloat())
            }
            draggableModule.onItemSwiping(c, viewHolder, dX, dY, isCurrentlyActive)
            c.restore()
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (!isViewCreateByAdapter(viewHolder)) {
            draggableModule.onItemSwiped(viewHolder)
        }
    }

    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return mMoveThreshold
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return mSwipeThreshold
    }

    fun setSwipeMoveFlags(swipeMoveFlags: Int) {
        mSwipeMoveFlags = swipeMoveFlags
    }

    private fun isViewCreateByAdapter(viewHolder: RecyclerView.ViewHolder?): Boolean {
        val type = viewHolder?.itemViewType
        return type == BaseDataWrapper.HEADER || type == BaseDataWrapper.LOADMORE || type == BaseDataWrapper.FOOTER
    }
}