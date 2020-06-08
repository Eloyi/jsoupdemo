package com.android.sanwei.uikit.recyclerview.diff

import androidx.recyclerview.widget.ListUpdateCallback
import com.android.sanwei.uikit.recyclerview.CommonAdapter

class DiffListUpdateCallback (private val mAdapter : CommonAdapter<*, *>) : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        val head = mAdapter.getDataPosition()

        mAdapter.notifyItemRangeInserted(position + head, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        val head = mAdapter.getDataPosition()
        mAdapter.notifyItemRangeRemoved(position + head, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val head = mAdapter.getDataPosition()
        mAdapter.notifyItemMoved(fromPosition + head, toPosition + head)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        val head = mAdapter.getDataPosition()
        mAdapter.notifyItemRangeChanged(position + head, count, payload)
    }
}