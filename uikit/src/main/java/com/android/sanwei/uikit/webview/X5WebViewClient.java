package com.android.sanwei.uikit.webview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * desc  : 自定义x5的WebViewClient
 * 作用：主要辅助 WebView 处理JavaScript 的对话框、网站 Logo、网站 title、load 进度等处理
 */
public class X5WebViewClient extends WebViewClient {

    private InterWebListener webListener;
    private WebView webView;
    private Context context;
    private JSBridgePlugin mJsPlugin;
    /**
     * 是否加载完毕
     */
    private boolean isLoadFinish = false;

    /**
     * 获取是否加载完毕
     *
     * @return 布尔值
     */
    public boolean isLoadFinish() {
        return isLoadFinish;
    }

    /**
     * 设置监听时间，包括常见状态页面切换，进度条变化等
     *
     * @param listener listener
     */
    public void setWebListener(InterWebListener listener) {
        this.webListener = listener;
    }

    /**
     * 构造方法
     *
     * @param webView 需要传进来webview
     * @param context 上下文
     */
    public X5WebViewClient(WebView webView, Context context) {
        this.context = context;
        this.webView = webView;
        //将js对象与java对象进行映射
        mJsPlugin = initJsBridgePlugin(webView);

        webView.addJavascriptInterface(new ImageJavascriptInterface(context), "imagelistener");
    }

    public JSBridgePlugin initJsBridgePlugin(WebView webView){
        WebViewJSBridgePlugin plugin = new WebViewJSBridgePlugin();
        plugin.init(webView);
        return plugin;
    }

    public JSBridgePlugin getJsPlugin() {
        return mJsPlugin;
    }

    /**
     * 这个方法中可以做拦截
     * 主要的作用是处理各种通知和请求事件
     * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
     *
     * @param view view
     * @param url  链接
     * @return 是否自己处理，true表示自己处理
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //页面关闭后，直接返回，不要执行网络请求和js方法
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
        if (!activityAlive) {
            return false;
        }
        if (TextUtils.isEmpty(url)) {
            return false;
        }


        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (mJsPlugin != null){
            boolean hackResult = mJsPlugin.hackShouldOverrideUrlLoading(url);

            if (hackResult){
                return true;
            }
        }


        WebView.HitTestResult hitTestResult = null;
        if (url.startsWith("http:") || url.startsWith("https:")) {
            hitTestResult = view.getHitTestResult();
        }
        if (hitTestResult == null) {
            if (url.startsWith("sms:")||url.startsWith("smsto:")||url.startsWith("mms:")||url.startsWith("mmsto:")){
                Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(sendIntent);
                return true;
            }else if (url.startsWith("mailto:")) {
                try {
                    //打开发邮件窗口
                    Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    context.startActivity(mailIntent);
                } catch (Exception e){
                    e.printStackTrace();
                }
                //<uses-permission android:name="android.permission.SEND_TO"/>
                return true;
            }
            return false;
        }
        //HitTestResult 描述
        //WebView.HitTestResult.UNKNOWN_TYPE 未知类型
        //WebView.HitTestResult.PHONE_TYPE 电话类型
        //WebView.HitTestResult.EMAIL_TYPE 电子邮件类型
        //WebView.HitTestResult.GEO_TYPE 地图类型
        //WebView.HitTestResult.SRC_ANCHOR_TYPE 超链接类型
        //WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE 带有链接的图片类型
        //WebView.HitTestResult.IMAGE_TYPE 单纯的图片类型
        //WebView.HitTestResult.EDIT_TEXT_TYPE 选中的文字类型
        if (hitTestResult.getType() == WebView.HitTestResult.PHONE_TYPE ){
            Intent sendIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(sendIntent);
        }
        else if (hitTestResult.getType() == WebView.HitTestResult.EMAIL_TYPE){
            Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(sendIntent);
        }
        if (hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
            return false;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    /**
     * 增加shouldOverrideUrlLoading在api>=24时
     * 主要的作用是处理各种通知和请求事件
     * 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
     *
     * @param view    view
     * @param request request
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        boolean activityAlive = X5WebUtils.isActivityAlive(context);
        if (!activityAlive) {
            return false;
        }
        String url = request.getUrl().toString();
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (mJsPlugin != null){
            boolean hackResult = mJsPlugin.hackShouldOverrideUrlLoading(url);

            if (hackResult){
                return true;
            }
        }

        WebView.HitTestResult hitTestResult = null;
        if (url.startsWith("http:") || url.startsWith("https:")) {
            hitTestResult = view.getHitTestResult();
        }
        if (hitTestResult == null) {
            if (url.startsWith("sms:")||url.startsWith("smsto:")||url.startsWith("mms:")||url.startsWith("mmsto:")){
                Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(sendIntent);
                return true;
            }else if (url.startsWith("mailto:")) {
                try {
                    //打开发邮件窗口
                    Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    context.startActivity(mailIntent);
                } catch (Exception e){
                    e.printStackTrace();
                }
                //<uses-permission android:name="android.permission.SEND_TO"/>
                return true;
            }
            return false;
        }
        if (hitTestResult.getType() == WebView.HitTestResult.PHONE_TYPE ){
            Intent sendIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(sendIntent);
        }
        else if (hitTestResult.getType() == WebView.HitTestResult.EMAIL_TYPE){
            Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(sendIntent);
        }
        //HitTestResult 描述
        //WebView.HitTestResult.UNKNOWN_TYPE 未知类型
        //WebView.HitTestResult.PHONE_TYPE 电话类型
        //WebView.HitTestResult.EMAIL_TYPE 电子邮件类型
        //WebView.HitTestResult.GEO_TYPE 地图类型
        //WebView.HitTestResult.SRC_ANCHOR_TYPE 超链接类型
        //WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE 带有链接的图片类型
        //WebView.HitTestResult.IMAGE_TYPE 单纯的图片类型
        //WebView.HitTestResult.EDIT_TEXT_TYPE 选中的文字类型
        if (hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
            return false;
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    /**
     * 作用：开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应。
     *
     * @param webView view
     * @param s       s
     * @param bitmap  bitmap
     */
    @Override
    public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
        super.onPageStarted(webView, s, bitmap);
        //设定加载开始的操作
        X5LogUtils.i("-------onPageStarted-------" + s);
        if (!X5WebUtils.isConnected(webView.getContext()) && webListener != null) {
            //显示异常页面
            webListener.showErrorView(X5WebUtils.ErrorMode.NO_NET);
        }
        isLoadFinish = false;
    }

    /**
     * 当页面加载完成会调用该方法
     *
     * @param view view
     * @param url  url链接
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        X5LogUtils.i("-------onPageFinished-------" + url);
        if (!X5WebUtils.isConnected(webView.getContext()) && webListener != null) {
            //隐藏进度条方法
            webListener.hindProgressBar();
            //显示异常页面
            webListener.showErrorView(X5WebUtils.ErrorMode.NO_NET);
        }

        if (mJsPlugin != null){
            mJsPlugin.hackOnPageFinished(view);
        }
        super.onPageFinished(view, url);
        //设置网页在加载的时候暂时不加载图片
        //webView.getSettings().setBlockNetworkImage(false);
        //页面finish后再发起图片加载
        if (!webView.getSettings().getLoadsImagesAutomatically()) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        }
        //html加载完成之后，添加监听图片的点击js函数
        //addImageClickListener();
        addImageArrayClickListener(webView);
        isLoadFinish = true;
    }

    /**
     * 请求网络出现error
     * 作用：加载页面的服务器出现错误时（如404）调用。
     * App里面使用webView控件的时候遇到了诸如404这类的错误的时候，若也显示浏览器里面的那种错误提示页面就显得很丑陋，
     * 那么这个时候我们的app就需要加载一个本地的错误提示页面，即webView如何加载一个本地的页面
     * 该方法传回了错误码，根据错误类型可以进行不同的错误分类处理
     * onReceivedError只有在遇到不可用的(unrecoverable)错误时，才会被调用）
     * 当WebView加载链接www.ycdoubi.com时，"不可用"的情况有可以包括有：
     * 1.没有网络连接
     * 2.连接超时
     * 3.找不到页面www.ycdoubi.com
     *
     * @param view        view
     * @param errorCode   错误码
     * @param description description
     * @param failingUrl  失败链接
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        X5LogUtils.i("-------onReceivedError-------" + failingUrl);
        if (webListener != null) {
            if (errorCode == ERROR_TIMEOUT) {
                //网络连接超时
                webListener.showErrorView(X5WebUtils.ErrorMode.TIME_OUT);
            } else if (errorCode == ERROR_CONNECT) {
                //断网
                webListener.showErrorView(X5WebUtils.ErrorMode.NO_NET);
            }
            {
                //其他情况
                webListener.showErrorView(X5WebUtils.ErrorMode.RECEIVED_ERROR);
            }
        }
    }

    /**
     * 当缩放改变的时候会调用该方法
     *
     * @param view     view
     * @param oldScale 之前的缩放比例
     * @param newScale 现在缩放比例
     */
    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        X5LogUtils.i("-------onScaleChanged-------" + newScale);
        //视频全屏播放按返回页面被放大的问题
        if (newScale - oldScale > 7) {
            //异常放大，缩回去。
            view.setInitialScale((int) (oldScale / newScale * 100));
        }
    }

    /**
     * 6.0 之后
     * 向主机应用程序报告Web资源加载错误。这些错误通常表明无法连接到服务器。
     * 不仅为主页。因此，建议在回调过程中执行最低要求的工作。
     * 该方法传回了错误码，根据错误类型可以进行不同的错误分类处理，比如
     * onReceivedError只有在遇到不可用的(unrecoverable)错误时，才会被调用）
     * 当WebView加载链接www.ycdoubi.com时，"不可用"的情况有可以包括有：
     * 1.没有网络连接
     * 2.连接超时
     * 3.找不到页面www.ycdoubi.com
     *
     * @param view    view
     * @param request request
     * @param error   error
     */
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        X5LogUtils.i("-------onReceivedError-------" + error.getDescription().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            X5LogUtils.d("服务器异常" + error.getDescription().toString());
        }
        //ToastUtils.showToast("服务器异常6.0之后");
        //当加载错误时，就让它加载本地错误网页文件
        //mWebView.loadUrl("file:///android_asset/errorpage/error.html");
        int errorCode = error.getErrorCode();
        //获取当前的网络请求是否是为main frame创建的.
        boolean forMainFrame = request.isForMainFrame();
        boolean redirect = request.isRedirect();
        if (webListener != null) {
            if (errorCode == ERROR_TIMEOUT) {
                //网络连接超时
                webListener.showErrorView(X5WebUtils.ErrorMode.TIME_OUT);
            } else if (errorCode == ERROR_CONNECT) {
                //断网
                webListener.showErrorView(X5WebUtils.ErrorMode.NO_NET);
            }
            {
                //其他情况
                webListener.showErrorView(X5WebUtils.ErrorMode.RECEIVED_ERROR);
            }
        }
    }


    /**
     * 通知主机应用程序在加载资源时从服务器接收到HTTP错误
     *
     * @param view          view
     * @param request       request
     * @param errorResponse 错误内容
     */
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        int statusCode = errorResponse.getStatusCode();
        String reasonPhrase = errorResponse.getReasonPhrase();
        X5LogUtils.i("-------onReceivedHttpError-------" + statusCode + "-------" + reasonPhrase);
        if (statusCode == 404) {
            //用javascript隐藏系统定义的404页面信息
            //String data = "Page NO FOUND！";
            //view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
            if (webListener != null) {
                webListener.showErrorView(X5WebUtils.ErrorMode.STATE_404);
            }
        } else if (statusCode == 500) {
            //避免出现默认的错误界面
            //view.loadUrl("about:blank");
            if (webListener != null) {
                webListener.showErrorView(X5WebUtils.ErrorMode.STATE_500);
            }
        } else {
            if (webListener != null) {
                webListener.showErrorView(X5WebUtils.ErrorMode.RECEIVED_ERROR);
            }
        }
    }

    /**
     * 通知主机应用程序已自动处理用户登录请求
     *
     * @param view    view
     * @param realm   数据
     * @param account account
     * @param args    args
     */
    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
        X5LogUtils.i("-------onReceivedLoginRequest-------" + args);
    }

    /**
     * 在加载资源时通知主机应用程序发生SSL错误
     * 作用：处理https请求
     * webView加载一些别人的url时候，有时候会发生证书认证错误的情况，这时候希望能够正常的呈现页面给用户，
     * 我们需要忽略证书错误，需要调用WebViewClient类的onReceivedSslError方法，
     * 调用handler.proceed()来忽略该证书错误。
     *
     * @param view    view
     * @param handler handler
     * @param error   error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        X5LogUtils.i("-------onReceivedSslError-------" + error.getUrl());
        if (error != null) {
            String url = error.getUrl();
            if (webListener != null) {
                webListener.showErrorView(X5WebUtils.ErrorMode.SSL_ERROR);
            }
            X5LogUtils.i("onReceivedSslError----异常url----" + url);
        }
        //https忽略证书问题
        if (handler != null) {
            //表示等待证书响应
            handler.proceed();
            // handler.cancel();      //表示挂起连接，为默认方式
            // handler.handleMessage(null);    //可做其他处理
        }
    }

    /**
     * 作用：在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次。
     *
     * @param webView view
     * @param s       s
     */
    @Override
    public void onLoadResource(WebView webView, String s) {
        super.onLoadResource(webView, s);
        X5LogUtils.i("-------onLoadResource-------" + s);
    }

    /**
     * @param webView view
     * @param s       s
     */
    @Override
    public void onPageCommitVisible(WebView webView, String s) {
        super.onPageCommitVisible(webView, s);
    }

    /**
     * @param webView view
     * @param s       s
     */
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
        return super.shouldInterceptRequest(webView, s);
    }

    /**
     * @param webView            view
     * @param webResourceRequest request
     * @return
     */
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        return super.shouldInterceptRequest(webView, webResourceRequest);
    }

    /**
     * @param webView            view
     * @param webResourceRequest request
     * @param bundle             bundle
     * @return
     */
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
        return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
    }

    /**
     * @param webView  view
     * @param message  message
     * @param message1 message1
     */
    @Override
    public void onTooManyRedirects(WebView webView, Message message, Message message1) {
        super.onTooManyRedirects(webView, message, message1);
    }

    /**
     * @param webView  view
     * @param message  message
     * @param message1 message1
     */
    @Override
    public void onFormResubmission(WebView webView, Message message, Message message1) {
        super.onFormResubmission(webView, message, message1);
    }

    /**
     * @param webView view
     * @param s       s
     * @param b       b
     */
    @Override
    public void doUpdateVisitedHistory(WebView webView, String s, boolean b) {
        super.doUpdateVisitedHistory(webView, s, b);
    }

    /**
     * @param webView           view
     * @param clientCertRequest request
     */
    @Override
    public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
        super.onReceivedClientCertRequest(webView, clientCertRequest);
    }

    /**
     * @param webView         view
     * @param httpAuthHandler handler
     * @param s               s
     * @param s1              s1
     */
    @Override
    public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String s, String s1) {
        super.onReceivedHttpAuthRequest(webView, httpAuthHandler, s, s1);
    }

    /**
     * @param webView  view
     * @param keyEvent event
     * @return
     */
    @Override
    public boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent) {
        return super.shouldOverrideKeyEvent(webView, keyEvent);
    }

    /**
     * @param webView  view
     * @param keyEvent event
     * @return
     */
    @Override
    public void onUnhandledKeyEvent(WebView webView, KeyEvent keyEvent) {
        super.onUnhandledKeyEvent(webView, keyEvent);
    }

    /**
     * android与js交互：
     * 首先我们拿到html中加载图片的标签img.
     * 然后取出其对应的src属性
     * 循环遍历设置图片的点击事件
     * 将src作为参数传给java代码
     * 这个循环将所图片放入数组，当js调用本地方法时传入。
     * 当然如果采用方式一获取图片的话，本地方法可以不需要传入这个数组
     * 通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
     *
     * @param webView webview
     */
    private void addImageArrayClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "var array=new Array(); " +
                "for(var j=0;j<objs.length;j++){" +
                "    array[j]=objs[j].src; " +
                "}" +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src,array);  " +
                "    }  " +
                "}" +
                "})()");
    }

    /**
     * 通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
     *
     * @param webView webview
     */
    private void addImageClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }
}

