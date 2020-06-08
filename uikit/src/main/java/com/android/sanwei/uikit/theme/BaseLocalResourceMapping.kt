package com.android.sanwei.uikit.theme

import android.content.Context
import com.android.sanwei.uikit.R


open class BaseLocalResourceMapping(val context: Context) : ILocalResourceMapping {

    override val map: MutableMap<String, String> = mutableMapOf()
    override val colorGroup: MutableMap<String, String> = mutableMapOf()
    override val drawableGroup: MutableMap<String, String> = mutableMapOf()
    override val stringGroup: MutableMap<String, String> = mutableMapOf()

    init {
        map[context.getString(R.string.key_color_badge)] = "badge_red"
        map[context.getString(R.string.key_color_badge_text)] = "color_badge_text"
    }

    override fun getLocalValue(key: String): String? {

        return map[key]
    }

    override fun getColorGroupValue(key: String): String? {

        return colorGroup[key]

    }

    override fun getDrawableGroupValue(key: String): String? {
        return drawableGroup[key]

    }

    override fun getStringGroupValue(key: String): String? {
        return stringGroup[key]
    }

}