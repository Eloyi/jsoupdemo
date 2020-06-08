package com.android.sanwei.uikit.theme

interface ILocalResourceMapping {
    val map: MutableMap<String, String>
    val colorGroup : MutableMap<String, String>
    val drawableGroup : MutableMap<String, String>
    val stringGroup : MutableMap<String, String>

    fun getLocalValue(key : String) : String?
    fun getColorGroupValue(key : String) : String?
    fun getDrawableGroupValue(key : String) : String?
    fun getStringGroupValue(key : String) : String?
}