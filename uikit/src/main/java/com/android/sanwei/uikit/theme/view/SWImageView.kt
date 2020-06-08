package com.android.sanwei.uikit.theme.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.android.sanwei.uikit.theme.ResourceHelper
import com.android.sanwei.uikit.theme.SWResourceView

class SWImageView : AppCompatImageView,
    SWResourceView<ImageView> {

    override var resourceMap: MutableMap<String, String?> = mutableMapOf()
    override var t: ImageView = this

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    fun init(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        initImageAttributes(context, attrs, defStyleAttr, defStyleRes)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        resourceViewAttachToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        resourceViewDetachedFromWindow()
    }

    override fun reload() {
        reloadCommon()
        ResourceHelper.setSrcWithKey(
            resourceMap[SWResourceView.RESOURCEKEY_SRC],
            this
        )
    }
}