package com.android.sanwei.uikit.recyclerview.refresh

interface RefreshCallback {
    fun onRefreshComplete(uuid: String)
    fun onRefreshFailed(uuid: String)
}