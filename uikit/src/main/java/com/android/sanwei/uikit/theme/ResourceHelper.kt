package com.android.sanwei.uikit.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider

object ResourceHelper {

    fun initBackgroundColor(attributes: TypedArray, view: View): String? {
        val key = attributes.getString(R.styleable.BaseSWTheme_theme_backgroundColor)

        setBackgroundColorWithKey(key, view)
        return key
    }

    fun setBackgroundColorWithKey(key: String?, view: View) {

        key?.let {
            val colorId = getColor(key, view.context)

            colorId?.let {
                view.setBackgroundColor(colorId)
            }
        }

    }

    fun initBackground(attributes: TypedArray, view: View): String? {
        val key = attributes.getString(R.styleable.BaseSWTheme_theme_background)
        setBackgroundWithKey(key, view)
        return key
    }

    fun setBackgroundWithKey(key: String?, view: View) {

        key?.let {

            setDrawable(it, view,
                { id ->
                    view.setBackgroundResource(id)
                }, { drawable ->
                    view.background = drawable
                }
            )
        }
    }

    fun initSrc(attributes: TypedArray, view: ImageView): String? {
        val key = attributes.getString(R.styleable.BaseSWTheme_theme_src)

        setSrcWithKey(key, view)
        return key
    }

    fun initShape(attributes: TypedArray, view: View, map: MutableMap<String, String?>): String? {
        //first check shape has drawable in dynamic or local resource, if not have this, use native builder
        val key = attributes.getString(R.styleable.BaseSWTheme_theme_shape) ?: return null

        var shapeResource = queryDynamicaShapeValue(key)

        if (shapeResource == null) {
            shapeResource = getLocalValue(key)
        }

        if (shapeResource != null) {
            //load local drawable file, ignore dynamic color or other settings
            view.setBackgroundResource(
                view.resources.getIdentifier(
                    shapeResource,
                    "drawable",
                    view.context.packageName
                )
            )
            return key
        }

        // use native code and dynamic settins to build a simple shape file

        val solidColor = attributes.getString(R.styleable.BaseSWTheme_theme_shape_solidcolor)
        val strokeColor = attributes.getString(R.styleable.BaseSWTheme_theme_shape_strokecolor)

        if (solidColor == null || strokeColor == null) {
            return null
        }

        map[SWResourceView.RESOURCEKEY_SHAPE_SOLIDCOLOR] = solidColor
        map[SWResourceView.RESOURCEKEY_SHAPE_STORKCOLOR] = strokeColor

        //radius not include in this theme resource control
        val cornerRadius = attributes.getFloat(R.styleable.BaseSWTheme_theme_shape_cornerradius, 0F)
        val strokeRadius = attributes.getInt(R.styleable.BaseSWTheme_theme_shape_strokeradius, 2)

        map[SWResourceView.RESOURCEKEY_SHAPE_CORNERRADIUS] = cornerRadius.toString()
        map[SWResourceView.RESOURCEKEY_SHAPE_STORKRADIUS] = strokeRadius.toString()

        val solidColorValue = getColor(solidColor, view.context)

        val storkeColorValue = getColor(strokeColor, view.context)

        if (solidColorValue == null || storkeColorValue == null) {
            return null
        }

        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(solidColorValue)

        gradientDrawable.cornerRadius = cornerRadius
        gradientDrawable.setStroke(strokeRadius, storkeColorValue)

        view.background = gradientDrawable

        return key
    }

    fun setSrcWithKey(key: String?, view: ImageView) {
        key?.let {

            setDrawable(it, view,
                { id ->
                    view.setImageResource(id)
                }, { drawable ->
                    view.setImageDrawable(drawable)
                }
            )
        }
    }

    fun initText(attributes: TypedArray, view: TextView): String? {
        val key = attributes.getString(R.styleable.BaseSWTheme_theme_text)

        setTextWithKey(key, view)
        return key
    }

    fun setTextWithKey(key: String?, view: TextView) {

        key?.let {
            setText(it, view)
        }
    }

    fun setShapeWithKey(key: String?, view: View, map: MutableMap<String, String?>) {
        key?.let {

            var shapeResource = queryDynamicaShapeValue(key)

            if (shapeResource == null) {
                shapeResource = getLocalValue(key)
            }

            if (shapeResource != null) {
                //load local drawable file, ignore dynamic color or other settings
                view.setBackgroundResource(
                    view.resources.getIdentifier(
                        shapeResource,
                        "drawable",
                        view.context.packageName
                    )
                )
                return
            }

            val solidColor = map[SWResourceView.RESOURCEKEY_SHAPE_SOLIDCOLOR]
            val strokeColor = map[SWResourceView.RESOURCEKEY_SHAPE_STORKCOLOR]
            val cornerRadius = map[SWResourceView.RESOURCEKEY_SHAPE_CORNERRADIUS]
            val strokeRadius = map[SWResourceView.RESOURCEKEY_SHAPE_STORKRADIUS]

            if (solidColor == null || strokeColor == null) {
                return
            }

            var solidColorValue = getColor(solidColor, view.context)

            var storkeColorValue = getColor(strokeColor, view.context)

            if (solidColorValue == null || storkeColorValue == null) {
                return
            }

            val gradientDrawable = GradientDrawable()
            gradientDrawable.setColor(solidColorValue)

            gradientDrawable.cornerRadius = cornerRadius?.toFloat() ?: 0F
            gradientDrawable.setStroke(strokeRadius?.toInt() ?: 2, storkeColorValue)

            view.background = gradientDrawable
        }
    }

    private fun getLocalValue(key: String?): String? {
        if (key == null) return null
        val mapping = UikitProvider.funLocalThemeMapping?.invoke()
        return mapping?.getLocalValue(key)
    }

    private fun getColorGroup(key: String?): String? {
        if (key == null) return null
        val mapping = UikitProvider.funLocalThemeMapping?.invoke()
        return mapping?.getColorGroupValue(key)
    }

    private fun getDrawableGroup(key: String?): String? {
        if (key == null) return null
        val mapping = UikitProvider.funLocalThemeMapping?.invoke()
        return mapping?.getDrawableGroupValue(key)
    }

    private fun getStringGroup(key: String?): String? {
        if (key == null) return null
        val mapping = UikitProvider.funLocalThemeMapping?.invoke()
        return mapping?.getStringGroupValue(key)
    }

    fun getColor(key: String?, context: Context): Int? {

        var remoteValue: String? = queryDynamicColorValue(key)
        var localResourceId: Int? = null

        if (remoteValue == null) {
            localResourceId = getLocalColorId(key, context)

            //if can't get both target remote and local resource,get group value

            if (localResourceId == null) {
                val group = getColorGroup(key)
                group?.let {
                    remoteValue = queryDynamicColorValue(group)

                    if (remoteValue == null) {
                        localResourceId = getLocalColorId(group, context)
                    }
                }
            }
        }

        if (remoteValue != null) {

            return Color.parseColor(remoteValue)

        } else if (localResourceId != null && localResourceId != 0) {

            return context.resources.getColor(localResourceId!!)
        }
        return null
    }


    fun setDrawable(
        key: String,
        view: View,
        callbackWithId: ((Int) -> Unit),
        callbackWithDrawable: ((Drawable) -> Unit)
    ) {

        var remoteValue: String? = queryDynamicSrcValue(key)
        var localResourceId: Int? = null

        if (remoteValue == null) {
            localResourceId = getLocalDrawableId(key, view)

            //if can't get both target remote and local resource,get group value

            if (localResourceId == null) {
                val group = getDrawableGroup(key)
                group?.let {
                    remoteValue = queryDynamicSrcValue(group)

                    if (remoteValue == null) {
                        localResourceId = getLocalDrawableId(group, view)
                    }
                }
            }
        }

        //TODO check is startwith http or not
        if (remoteValue != null) {

            val getDrwable = UikitProvider.funGetDrawable

            getDrwable?.invoke(view, remoteValue!!) { drawable ->
                callbackWithDrawable.invoke(drawable)
            }

        } else if (localResourceId != null && localResourceId != 0) {

            localResourceId?.let { id ->
                callbackWithId.invoke(id)
            }
        }
    }

    private fun setText(key: String, view: TextView) {

        var remoteValue: String? = queryDynamicSrcValue(key)
        var localResourceId: Int? = null

        if (remoteValue == null) {
            localResourceId = getLocalTextId(key, view)

            //if can't get both target remote and local resource,get group value

            if (localResourceId == null) {
                val group = getStringGroup(key)
                group?.let {
                    remoteValue = queryDynamicTextValue(group)
                    if (remoteValue == null) {
                        localResourceId = getLocalTextId(group, view)
                    }
                }
            }
        }

        if (remoteValue != null) {
            view.text = remoteValue

        } else if (localResourceId != null && localResourceId != 0) {
            view.setText(localResourceId!!)
        }
    }


    private fun getLocalDrawableId(key: String?, view: View): Int? {

        val value = getLocalValue(key) ?: return null

        return view.resources.getIdentifier(
            value,
            "drawable",
            view.context.packageName
        )
    }

    private fun getLocalTextId(key: String?, view: View): Int? {

        val value = getLocalValue(key) ?: return null

        return view.resources.getIdentifier(
            value,
            "string",
            view.context.packageName
        )
    }

    private fun getLocalColorId(key: String?, context: Context): Int? {

        val value = getLocalValue(key) ?: return null

        return context.resources.getIdentifier(
            value,
            "color",
            context.packageName
        )
    }


    private fun queryDynamicBackgroundValue(key: String?): String? {
        if (key == null) return null
        val method = UikitProvider.funGetRemoteResource
        return method?.invoke(key)
    }

    private fun queryDynamicColorValue(key: String?): String? {
        if (key == null) return null
        val method = UikitProvider.funGetRemoteResource
        return method?.invoke(key)
    }

    private fun queryDynamicSrcValue(key: String?): String? {
        if (key == null) return null
        val method = UikitProvider.funGetRemoteResource
        return method?.invoke(key)
    }

    private fun queryDynamicTextValue(key: String?): String? {
        if (key == null) return null
        val method = UikitProvider.funGetRemoteResource
        return method?.invoke(key)
    }

    private fun queryDynamicaShapeValue(key: String?): String? {
        if (key == null) return null
        val method = UikitProvider.funGetRemoteResource
        return method?.invoke(key)
    }


}