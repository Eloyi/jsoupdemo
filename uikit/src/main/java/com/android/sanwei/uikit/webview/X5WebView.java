package com.android.sanwei.uikit.webview;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.util.Pair;

import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.KITKAT;

public class X5WebView extends WebView {

    private OnScrollChangeListener mOnScrollChangeListener;
    private X5WebViewClient x5WebViewClient;
    private X5WebChromeClient x5WebChromeClient;

    public X5WebView(Context context, boolean b) {
        super(context, b);
        init();
    }

    public X5WebView(Context context) {
        super(context);
        init();
    }

    public X5WebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public X5WebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    public X5WebView(Context context, AttributeSet attributeSet, int i, boolean b) {
        super(context, attributeSet, i, b);
        init();
    }

    public X5WebView(Context context, AttributeSet attributeSet, int i, Map<String, Object> map, boolean b) {
        super(context, attributeSet, i, map, b);
        init();
    }



    private void init() {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        initWebViewSettings();
        x5WebViewClient = new X5WebViewClient(this, getContext());
        this.setWebViewClient(x5WebViewClient);
        x5WebChromeClient = new X5WebChromeClient(this, (Activity) getContext());
        this.setWebChromeClient(x5WebChromeClient);
        //设置可以点击
        this.getView().setClickable(true);
    }


    /**
     * 做一些公共的初始化操作
     */
    private void initWebViewSettings() {
        WebSettings ws = this.getSettings();
        // 网页内容的宽度是否可大于WebView控件的宽度
        ws.setLoadWithOverviewMode(false);
        // 保存表单数据
        ws.setSaveFormData(true);
        // 是否应该支持使用其屏幕缩放控件和手势缩放
        ws.setSupportZoom(true);
        // 设置内置的缩放控件。若为false，则该WebView不可缩放
        ws.setBuiltInZoomControls(true);
        // 隐藏原生的缩放控件
        ws.setDisplayZoomControls(false);
        // 启动应用缓存
        ws.setAppCacheEnabled(true);
        // 设置缓存模式
        // 缓存模式如下：
        // LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        // LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        // LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        // LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setAppCacheMaxSize(Long.MAX_VALUE);
        // setDefaultZoom  api19被弃用
        // 设置此属性，可任意比例缩放。
        ws.setUseWideViewPort(true);
        // 告诉WebView启用JavaScript执行。默认的是false。
        // 注意：这个很重要   如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        ws.setJavaScriptEnabled(true);
        //  页面加载好以后，再放开图片
        //ws.setBlockNetworkImage(false);
        // 使用localStorage则必须打开
        ws.setDomStorageEnabled(true);
        //防止中文乱码
        ws.setDefaultTextEncodingName("UTF -8");
        /*
         * 排版适应屏幕
         * 用WebView显示图片，可使用这个参数
         * 设置网页布局类型： 1、LayoutAlgorithm.NARROW_COLUMNS ：
         * 适应内容大小 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
         */
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        // WebView是否新窗口打开(加了后可能打不开网页)
        //ws.setSupportMultipleWindows(true);
        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //设置字体默认缩放大小
        ws.setTextZoom(100);
        // 不缩放
        this.setInitialScale(100);
        if (Build.VERSION.SDK_INT >= KITKAT) {
            //设置网页在加载的时候暂时不加载图片
            ws.setLoadsImagesAutomatically(true);
        } else {
            ws.setLoadsImagesAutomatically(false);
        }
        //默认关闭硬件加速
        setOpenLayerType(false);
        //默认不开启密码保存功能
        setSavePassword(false);
    }

    public void registerJsBridgeHandler(String handlerName, BridgeHandler handler){

        JSBridgePlugin jsBridgePlugin = getJsBridgePlugin();

        if (jsBridgePlugin == null){
            return;
        }

        jsBridgePlugin.registerHandler(handlerName, handler);
    }

    public void unRegisterJsBridgeHandler(String handlerName){
        JSBridgePlugin jsBridgePlugin = getJsBridgePlugin();

        if (jsBridgePlugin == null){
            return;
        }

        jsBridgePlugin.unRegisterHandler(handlerName);
    }

    public void callHandler(String handlerName, String data, CallBackFunction callBack){
        JSBridgePlugin jsBridgePlugin = getJsBridgePlugin();

        if (jsBridgePlugin == null){
            return;
        }

        jsBridgePlugin.callHandler(handlerName, data, callBack);
    }

    public JSBridgePlugin getJsBridgePlugin(){
        X5WebViewClient client = getX5WebViewClient();

        if (client == null){
            return null;
        }
        return client.getJsPlugin();
    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.mOnScrollChangeListener = listener;
    }

    /**
     * 获取设置的X5WebChromeClient对象
     *
     * @return X5WebChromeClient对象
     */
    public X5WebChromeClient getX5WebChromeClient() {
        return this.x5WebChromeClient;
    }

    /**
     * 获取设置的X5WebViewClient对象
     *
     * @return X5WebViewClient对象
     */
    public X5WebViewClient getX5WebViewClient() {
        return this.x5WebViewClient;
    }

    /**
     * 设置是否自定义视频视图
     *
     * @param isShowCustomVideo 设置是否自定义视频视图
     */
    public void setShowCustomVideo(boolean isShowCustomVideo) {
        getX5WebChromeClient().setShowCustomVideo(isShowCustomVideo);
    }

    /**
     * 刷新界面可以用这个方法
     */
    public void reLoadView() {
        this.reload();
    }

    /**
     * 是否开启软硬件加速
     *
     * @param layerType 布尔值
     */
    public void setOpenLayerType(boolean layerType) {
        if (layerType) {
            //开启软硬件加速，开启软硬件加速这个性能提升还是很明显的，但是会耗费更大的内存 。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
    }

    /**
     * WebView 默认开启密码保存功能，但是存在漏洞。
     * 如果该功能未关闭，在用户输入密码时，会弹出提示框，询问用户是否保存密码，如果选择”是”，
     * 密码会被明文保到 /data/data/com.package.name/databases/webview.db 中，这样就有被盗取密码的危险
     *
     * @param save
     */
    public void setSavePassword(boolean save) {
        if (save) {
            this.getSettings().setSavePassword(true);
        } else {
            this.getSettings().setSavePassword(false);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangeListener != null) {
            if (isBottom() && this.getX5WebViewClient().isLoadFinish()) {
                //处于底端
                mOnScrollChangeListener.onPageEnd(l, t, oldl, oldt);
            } else if (isTop() && this.getX5WebViewClient().isLoadFinish()) {
                //处于顶端
                mOnScrollChangeListener.onPageTop(l, t, oldl, oldt);
            } else {
                mOnScrollChangeListener.onScrollChanged(l, t, oldl, oldt);
            }
        }
    }

    public interface OnScrollChangeListener {

        void onPageEnd(int l, int t, int oldl, int oldt);

        void onPageTop(int l, int t, int oldl, int oldt);

        void onScrollChanged(int l, int t, int oldl, int oldt);

    }

    /**
     * 判断是否在顶部
     *
     * @return true表示在顶部
     */
    private boolean isTop() {
        return getScrollY() <= 0;
    }

    /**
     * 判断是否在底部
     *
     * @return true表示在底部
     */
    private boolean isBottom() {
        return getHeight() + getScrollY() >= getContentHeight() * getScale();
    }

    public void loadUrl(String s, Context context) {

        loadUrl(s);

        if (context != null) {
            CookieSyncManager.createInstance(context);
            CookieSyncManager.getInstance().sync();
        }
    }

    public void loadMethod(String methodName, List<String> params) {
        if (TextUtils.isEmpty(methodName)) {
            return;
        }
        StringBuilder builder = new StringBuilder();

        String endStr = ",";

        builder.append("javascript:");
        builder.append(methodName);
        builder.append("(\"");

        for (int i = 0; i < params.size(); i++) {
            builder.append(params.get(i));
            if (i != params.size() - 1){
                builder.append(endStr);
            }
        }

        builder.append("\")");

        loadUrl(builder.toString());
    }

}
