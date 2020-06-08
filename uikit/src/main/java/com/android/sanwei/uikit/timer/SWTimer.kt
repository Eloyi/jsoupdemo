package com.android.sanwei.uikit.timer

import java.util.*

class SWTimer {

    private val mTimer: Timer
    private val mPeriod: Long
    val mListener: TimerListener
    var mCurrentProgress: Int = 0
    var mDuration: Long? = null
    var mDate: Date? = null

    //milliseconds
    constructor(period: Long, duration: Long, listener: TimerListener) {
        mPeriod = period
        mDuration = duration
        mListener = listener
    }

    //milliseconds
    constructor(period: Long, date: Date, listener: TimerListener) {
        mPeriod = period
        mDate = date
        mListener = listener
    }

    init {
        mTimer = Timer()
    }


    fun start(progress: Int = 0) {

        mCurrentProgress = progress

        val max = getMax()

        val timeTask = object : TimerTask() {
            override fun run() {

                if (mCurrentProgress >= max) {
                    cancel()

                    mListener.onFinished()

                    return
                }

                mCurrentProgress++

                mListener.onTick(mCurrentProgress, max)
            }
        }
        mTimer.schedule(timeTask, mPeriod, mPeriod)
    }

    private fun getMax(): Int {
        mDuration?.let { return (it / 1000).toInt() }
        mDate?.let {
            val time = it.time - Date().time
            return (time / 1000).toInt()
        }
        return 0
    }

    fun cancel() {
        mTimer.cancel()
    }

    fun destroy() {
        cancel()
    }
}