package com.android.sanwei.uikit.banner.indicator;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.android.sanwei.uikit.banner.config.IndicatorConfig;


public interface Indicator extends ViewPager.OnPageChangeListener {
    @NonNull
    View getIndicatorView();

    IndicatorConfig getIndicatorConfig();

    void onPageChanged(int count, int currentPosition);

}
