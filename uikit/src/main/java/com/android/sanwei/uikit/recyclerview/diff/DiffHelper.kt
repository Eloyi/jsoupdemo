package com.android.sanwei.uikit.recyclerview.diff

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.android.sanwei.uikit.recyclerview.CommonAdapter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor

class DiffHelper<T>(
    private val adapter: CommonAdapter<T, *>,
    private val config: CommonDiffConfig<T>
) {
    private val mUpdateCallback: ListUpdateCallback = DiffListUpdateCallback(adapter)

    private var mMainThreadExecutor: Executor

    private class MainThreadExecutor internal constructor() : Executor {
        val mHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mHandler.post(command)
        }
    }

    init {
        mMainThreadExecutor = config.mainThreadExecutor ?: MainThreadExecutor()
    }

    private val mListeners: MutableList<ListChangeListener<T>> = CopyOnWriteArrayList()

    private var mMaxScheduledGeneration = 0

    fun submitList(newList: MutableList<T>?, commitCallback: Runnable? = null) {
        // incrementing generation means any currently-running diffs are discarded when they finish
        val runGeneration: Int = ++mMaxScheduledGeneration
        if (newList == adapter.data) {
            // nothing to do (Note - still had to inc generation, since may have ongoing work)
            commitCallback?.run()
            return
        }
        val oldList = adapter.data
        // fast simple remove all
        if (newList == null) {
            val countRemoved: Int = adapter.data.size
            adapter.data = arrayListOf()
            adapter.writeDataInList()
            // notify last, after list is updated
            mUpdateCallback.onRemoved(0, countRemoved)
            onCurrentListChanged(oldList, commitCallback)
            return
        }
        // fast simple first insert
        if (adapter.data.isEmpty()) {
            adapter.data = newList
            adapter.writeDataInList()
            // notify last, after list is updated
            mUpdateCallback.onInserted(0, newList.size)
            onCurrentListChanged(oldList, commitCallback)
            return
        }

        config.backgroundThreadExecutor.execute {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return oldList.size
                }

                override fun getNewListSize(): Int {
                    return newList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return if (oldItem != null && newItem != null) {
                        config.diffCallback.areItemsTheSame(oldItem, newItem)
                    } else oldItem == null && newItem == null
                    // If both items are null we consider them the same.
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    if (oldItem != null && newItem != null) {
                        return config.diffCallback.areContentsTheSame(oldItem, newItem)
                    }
                    if (oldItem == null && newItem == null) {
                        return true
                    }
                    throw AssertionError()
                }

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    if (oldItem != null && newItem != null) {
                        return config.diffCallback.getChangePayload(oldItem, newItem)
                    }
                    throw AssertionError()
                }
            })
            mMainThreadExecutor.execute {
                if (mMaxScheduledGeneration == runGeneration) {
                    latchList(newList, result, commitCallback)
                }
            }
        }
    }

    private fun latchList(
        newList: MutableList<T>,
        diffResult: DiffUtil.DiffResult,
        commitCallback: Runnable?
    ) {
        val previousList: List<T> = adapter.data

        adapter.data = newList
        adapter.writeDataInList()

        diffResult.dispatchUpdatesTo(mUpdateCallback)
        onCurrentListChanged(previousList, commitCallback)
    }

    private fun onCurrentListChanged(
        previousList: List<T>,
        commitCallback: Runnable?
    ) {
        for (listener in mListeners) {
            listener.onCurrentListChanged(previousList, adapter.data)
        }
        commitCallback?.run()
    }

    fun addListListener(listener: ListChangeListener<T>) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeListListener(listener: ListChangeListener<T>) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener)
        }
    }

    fun clearAllListListener() {
        mListeners.clear()
    }
}