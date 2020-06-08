package com.android.eloy.jsoupdemo;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    public static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
