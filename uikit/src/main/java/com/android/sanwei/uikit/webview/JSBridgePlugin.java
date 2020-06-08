package com.android.sanwei.uikit.webview;

import com.tencent.smtt.sdk.WebView;

public interface JSBridgePlugin {
    //hack onpageFinished.inject js file to html
    void hackOnPageFinished(WebView webView);
    //return true if need handle url
    Boolean hackShouldOverrideUrlLoading(String url);

    void callHandler(String handlerName, String data, CallBackFunction callBack);

    void registerHandler(String handlerName, BridgeHandler handler);

    void unRegisterHandler(String handlerName);
}
