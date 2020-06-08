package com.android.sanwei.uikit.recyclerview.refresh

interface OnRefreshListener{
    fun onRefresh(tag:String, callback: RefreshCallback)
}