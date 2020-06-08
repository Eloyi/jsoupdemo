package com.android.sanwei.uikit.recyclerview.adapter

interface IGroupItem {
    fun getItemParentId(): String
    fun getItemId(): String
    fun isExpired(): Boolean
    fun isTitle(): Boolean
    //this shold be jsonObject, and only data in shopping card will has value eg: {size:"small", type : "u1", color : "red"}
    fun getItemInfo(): String
    fun getSubtype() : Int
    fun setSubType(type : Int)
    fun isSameData(item : IGroupItem) : Boolean
    fun setSelectStatus(boolean: Boolean)
    fun isSelected(): Boolean
}

