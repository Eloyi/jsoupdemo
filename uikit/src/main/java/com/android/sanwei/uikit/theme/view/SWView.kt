package com.android.sanwei.uikit.theme.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.android.sanwei.uikit.theme.SWResourceView

class SWView : View, SWResourceView<View> {

    override var resourceMap: MutableMap<String, String?> = mutableMapOf()

    override var t: View = this

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    fun init(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {

        initAttributes(context, attrs, defStyleAttr, defStyleRes)
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
    }


}