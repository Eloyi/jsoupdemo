package com.android.sanwei.uikit.recyclerview

import java.lang.Exception

/**
 * recyclerview datawrapper, type for itemType like refresh,header,laadmore,footer, normal data will be realData or
 * u can set multiType by override commonviewholder.getDefItemViewType()
 */
class BaseDataWrapper {
    companion object {
        const val REFRESH = 10001
        const val HEADER = 10002
        const val LOADMORE = 10003
        const val FOOTER = 10004
        const val REALDATA = 10005
        const val ERROR = 10007
    }

    val type: Int
    val data: Any?
    private val dataInfo: RealDataInfo?

    var subType : Int ?= null

    constructor(type: Int, data: Any? = null, dataInfo: RealDataInfo? = null) {
        this.type = type
        this.data = data
        this.dataInfo = dataInfo
    }

    fun getOrignPosition(): Int {
        return dataInfo?.getOrignPosition() ?: throw Exception("data error")
    }
}