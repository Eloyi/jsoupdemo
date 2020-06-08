package com.android.sanwei.uikit.recyclerview.diff

import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.DiffUtil
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CommonDiffConfig<T>(
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    val mainThreadExecutor: Executor?,
    val backgroundThreadExecutor: Executor,
    val diffCallback: DiffUtil.ItemCallback<T>) {

    companion object {
        private val sExecutorLock = Any()
        private var sDiffExecutor: Executor? = null
    }

    class Builder<T>(private val mDiffCallback: DiffUtil.ItemCallback<T>) {
        private var mMainThreadExecutor: Executor? = null
        private var mBackgroundThreadExecutor: Executor? = null
        /**
         * If provided, defines the main thread executor used to dispatch adapter update
         * notifications on the main thread.
         *
         *
         * If not provided, it will default to the main thread.
         *
         * @param executor The executor which can run tasks in the UI thread.
         * @return this
         *
         * @hide
         */
        fun setMainThreadExecutor(executor: Executor?): Builder<T> {
            mMainThreadExecutor = executor
            return this
        }

        /**
         * If provided, defines the background executor used to calculate the diff between an old
         * and a new list.
         *
         *
         * If not provided, defaults to two thread pool executor, shared by all ListAdapterConfigs.
         *
         * @param executor The background executor to run list diffing.
         * @return this
         */
        fun setBackgroundThreadExecutor(executor: Executor?): Builder<T> {
            mBackgroundThreadExecutor = executor
            return this
        }


        fun build(): CommonDiffConfig<T> {
            if (mBackgroundThreadExecutor == null) {
                synchronized(sExecutorLock) {
                    if (sDiffExecutor == null) {
                        sDiffExecutor = Executors.newFixedThreadPool(2)
                    }
                }
                mBackgroundThreadExecutor = sDiffExecutor
            }
            return CommonDiffConfig(
                mMainThreadExecutor,
                mBackgroundThreadExecutor!!,
                mDiffCallback)
        }



    }
}