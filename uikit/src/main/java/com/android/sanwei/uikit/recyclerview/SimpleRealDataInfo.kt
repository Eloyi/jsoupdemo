package com.android.sanwei.uikit.recyclerview

class SimpleRealDataInfo<T>(val data : T,val list : MutableList<T>) : RealDataInfo {

    override fun getOrignDataList(): MutableList<T> {
        return list
    }

    override fun getOrignPosition(): Int {
        return list.indexOf(data)
    }
}