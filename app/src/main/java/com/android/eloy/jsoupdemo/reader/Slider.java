package com.android.eloy.jsoupdemo.reader;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * @author yuyh.
 * @date 17/3/24.
 */

public interface Slider {

    void bind(ReadView readView);

    void initListener(OnPageStateChangedListener listener);

    boolean onTouchEvent(MotionEvent event);

    void computeScroll();

    void onDraw(Canvas canvas);
}
