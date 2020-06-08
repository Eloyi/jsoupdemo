package com.android.sanwei.uikit.webview;

/**
 *     desc  : web的接口回调，主要是视频相关回调，比如全频，取消全频，隐藏和现实webView
 */
public interface VideoWebListener {

    /**
     * 显示全屏view
     */
    void showVideoFullView();

    /**
     * 隐藏全屏view
     */
    void hindVideoFullView();

    /**
     * 显示webview
     */
    void showWebView();

    /**
     * 隐藏webview
     */
    void hindWebView();

}
