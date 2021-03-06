package com.android.sanwei.uikit.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;


/**
 * desc  : 自定义全频播放视频的时候，视频的负空间
 */
class FullscreenHolder extends FrameLayout {

    public FullscreenHolder(Context ctx) {
        super(ctx);
        setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
    }

    /**
     * 设置自己处理触摸时间
     *
     * @param event event
     * @return
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

}
