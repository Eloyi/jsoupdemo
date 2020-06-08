package com.android.sanwei.uikit.banner.adapter

import com.android.sanwei.uikit.banner.bean.BaseBanner
import com.android.sanwei.uikit.banner.listener.OnBannerListener

interface BannerAdapter{
    fun setOnBannerListener(listener: OnBannerListener)
    fun setDatas(datas: List<BaseBanner>?)
    fun getRealCount():Int
    fun getItemCount():Int


}