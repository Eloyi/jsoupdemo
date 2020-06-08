package com.android.sanwei.uikit.timer

interface TimerListener {
    fun onTick(progress : Int, duration : Int)
    fun onFinished()
}