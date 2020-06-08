package com.android.sanwei.uikit

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.android.sanwei.uikit.theme.ILocalResourceMapping
import com.android.sanwei.uikit.theme.SWThemeResource
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebView
import java.util.concurrent.Executor

/**
 * provider data for uikit module use,
 * app project need init some necessary params
 */
object UikitProvider {

    var mainExecutorLam: (() -> Executor) ?= null
    var backgroundExecutorLam: (() -> Executor) ?= null

    var stringFormat: ((id: Int) -> String) ?= null

    var funLocalThemeMapping : (() -> ILocalResourceMapping) ?= null
    var funGetDrawable : ((View, String, ((Drawable) -> Unit)) -> Unit) ?= null
    var funGetRemoteResource : ((String) -> String?) ?= null
    var funViewAttachWindowCallback : ((SWThemeResource) -> Unit) ?= null
    var funViewDettachWindowCallback : ((SWThemeResource) -> Unit) ?= null

    internal fun getMainThreadExecutor(): Executor? {

        return mainExecutorLam?.let { it() }
    }

    internal fun getBackgroundThreadExecutor(): Executor? {

        return backgroundExecutorLam?.let { it() }
    }

    internal fun formatString(id: Int): String? {
        return stringFormat?.let { it(id) }
    }

    /**
     * set webviewcrash info if needed
     */
    fun setWebviewCrashInfo(map : MutableMap<String, String>, context: Context){
        map["x5crashInfo"] = WebView.getCrashExtraMessage(context)
    }

    fun initWebview(context: Context){
        val callback = object : QbSdk.PreInitCallback{
            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(p0: Boolean) {
                Log.e("test", " onViewInitFinished is $p0");
            }
        }
        QbSdk.initX5Environment(context , callback)
    }
}