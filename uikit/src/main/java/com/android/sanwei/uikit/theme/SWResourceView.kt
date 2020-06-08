package com.android.sanwei.uikit.theme

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider

interface SWResourceView<T : View> : SWThemeResource{

    var resourceMap: MutableMap<String, String?>

    var t: T

    companion object {
        const val RESOURCEKEY_BACKGROUND = "resourcekey_background"
        const val RESOURCEKEY_BACKGROUNDCOLOR = "resourcekey_backgroundcolor"
        const val RESOURCEKEY_SRC = "resourcekey_src"
        const val RESOURCEKEY_TEXT = "resourcekey_text"
        const val RESOURCEKEY_SHAPE = "resourcekey_shape"
        const val RESOURCEKEY_SHAPE_SOLIDCOLOR = "resourcekey_shape_solidcolor"
        const val RESOURCEKEY_SHAPE_STORKCOLOR = "resourcekey_shape_storkcolor"
        const val RESOURCEKEY_SHAPE_CORNERRADIUS = "resourcekey_shape_cornerradius"
        const val RESOURCEKEY_SHAPE_STORKRADIUS = "resourcekey_shape_storkradius"
    }

    fun initAttributes(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.BaseSWTheme, defStyleAttr, 0)

        initCommonResouces(attributes)

        attributes.recycle()

    }

    fun initImageAttributes(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.BaseSWTheme, defStyleAttr, 0)

        initCommonResouces(attributes)
        if (t is ImageView) {
            resourceMap[RESOURCEKEY_SRC] = ResourceHelper.initSrc(attributes, t as ImageView)
        }

        attributes.recycle()
    }

    fun initTextAttributes(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.BaseSWTheme, defStyleAttr, 0)

        initCommonResouces(attributes)
        if (t is TextView) {
            resourceMap[RESOURCEKEY_TEXT] = ResourceHelper.initText(attributes, t as TextView)
        }
        attributes.recycle()
    }

    fun initCommonResouces(attributes: TypedArray) {
        resourceMap[RESOURCEKEY_BACKGROUND] = ResourceHelper.initBackground(attributes, t)
        resourceMap[RESOURCEKEY_BACKGROUNDCOLOR] =
            ResourceHelper.initBackgroundColor(attributes, t)
        resourceMap[RESOURCEKEY_SHAPE] = ResourceHelper.initShape(attributes, t, resourceMap)
    }


    fun setSWBackgroundKey(key: String) {
        resourceMap[RESOURCEKEY_BACKGROUND] = key
        ResourceHelper.setBackgroundWithKey(key, t)
    }

    fun setSWBackgroundColorKey(key: String) {
        resourceMap[RESOURCEKEY_BACKGROUNDCOLOR] = key
        ResourceHelper.setBackgroundColorWithKey(key, t)
    }

    fun setSWSrc(key: String) {
        if (t is ImageView) {
            resourceMap[RESOURCEKEY_SRC] = key
            ResourceHelper.setSrcWithKey(key, t as ImageView)
        }
    }

    fun setSWText(key: String) {
        if (t is TextView) {
            resourceMap[RESOURCEKEY_TEXT] = key
            ResourceHelper.setTextWithKey(key, t as TextView)
        }
    }

    fun reloadCommon() {
        ResourceHelper.setBackgroundWithKey(resourceMap[RESOURCEKEY_BACKGROUND], t)
        ResourceHelper.setBackgroundColorWithKey(resourceMap[RESOURCEKEY_BACKGROUNDCOLOR], t)
        ResourceHelper.setShapeWithKey(resourceMap[RESOURCEKEY_SHAPE], t, resourceMap)
    }

    fun resourceViewAttachToWindow() {
        val funAttach = UikitProvider.funViewAttachWindowCallback
        funAttach?.invoke(this)
    }

    fun resourceViewDetachedFromWindow() {
        val funDetach = UikitProvider.funViewDettachWindowCallback

        funDetach?.invoke(this)
    }

    fun getResourceKeyBackground(): String? {
        return resourceMap[RESOURCEKEY_BACKGROUND]
    }

    fun getResourceKeyBackgroundColor(): String? {
        return resourceMap[RESOURCEKEY_BACKGROUNDCOLOR]
    }

    fun getResourceKeySrc(): String? {
        return resourceMap[RESOURCEKEY_SRC]
    }

    fun getResourceKeyText(): String? {
        return resourceMap[RESOURCEKEY_TEXT]
    }

    fun getResourceKeyShape(): String? {
        return resourceMap[RESOURCEKEY_SHAPE]
    }

}