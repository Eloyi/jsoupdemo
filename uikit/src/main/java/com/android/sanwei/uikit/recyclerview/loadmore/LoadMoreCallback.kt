package com.android.sanwei.uikit.recyclerview.loadmore


interface LoadMoreCallback {
    fun onLoadComplete(uuid: String)
    fun onLoadFailed(uuid: String)
    fun onLoadEnd(uuid: String, gone : Boolean = true)
}