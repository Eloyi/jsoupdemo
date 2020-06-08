package com.android.sanwei.uikit.webview;

import android.util.Log;

/**
 * desc  : log工具
 */
public final class X5LogUtils {

    private static final String TAG = "X5LogUtils";
    private static boolean isLog = true;

    /**
     * 设置是否开启日志
     *
     * @param isLog 是否开启日志
     */
    public static void setIsLog(boolean isLog) {
        X5LogUtils.isLog = isLog;
    }

    public static void d(String message) {
        if (isLog) {
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if (isLog) {
            Log.i(TAG, message);
        }

    }

    public static void e(String message, Throwable throwable) {
        if (isLog) {
            Log.e(TAG, message, throwable);
        }
    }

}
