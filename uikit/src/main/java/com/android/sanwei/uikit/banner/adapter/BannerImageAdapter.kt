package com.android.sanwei.uikit.banner.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider
import com.android.sanwei.uikit.banner.bean.BaseBanner
import com.android.sanwei.uikit.recyclerview.CommonViewHolder


class BannerImageAdapter(mDatas: List<BaseBanner>?=null) :
    CommonBannerAdapter<CommonViewHolder>(mDatas) {

    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    override fun onCreateHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.banner_image, parent, false)
        return CommonViewHolder(view)
    }


    override fun onBindView(holder: CommonViewHolder?, data: BaseBanner?, position: Int, size: Int) {
        val getDrawable = UikitProvider.funGetDrawable

        val imageView = holder?.getViewOrNull<ImageView>(R.id.image) ?: return

        if (!TextUtils.isEmpty(data?.imageUrl)){
            getDrawable?.invoke(imageView, data?.imageUrl!!) { it ->
                imageView?.setImageDrawable(it)
            }
        }


    }
}