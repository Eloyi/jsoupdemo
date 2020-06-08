package com.android.sanwei.uikit.banner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.sanwei.uikit.banner.IViewHolder
import com.android.sanwei.uikit.banner.bean.BaseBanner
import com.android.sanwei.uikit.banner.listener.OnBannerListener
import com.android.sanwei.uikit.banner.util.BannerUtils
import java.util.*

/**
 * add last banner item in position 0, and add first bannber item in last position
 * banner will always start in positon 1 and loop like this 1-2-3-4-5-1A(final)-5A(first)-1-2-3-4-5
 */
abstract class CommonBannerAdapter<VH : RecyclerView.ViewHolder?>(datas: List<BaseBanner>?) :
    RecyclerView.Adapter<VH>(), IViewHolder<BaseBanner, VH>, BannerAdapter {

    protected var mDatas: MutableList<BaseBanner> = ArrayList()
    private var listener: OnBannerListener? = null

    init {
        setDatas(datas)
    }

    override fun setDatas(datas: List<BaseBanner>?) {
        var datas = datas
        mDatas.clear()
        if (datas == null) {
            datas = ArrayList()
        }
        mDatas.addAll(datas)
        val count = datas.size


        if (count > 1) {
            mDatas.add(0, datas[count - 1])
            mDatas.add(datas[0])
        }
        notifyDataSetChanged()
    }

    fun getData(position: Int): BaseBanner {
        return mDatas[position]
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val real = BannerUtils.getRealPosition(position, getRealCount())
        val data = mDatas[position]
        onBindView(holder, mDatas[position], real, getRealCount())
        if (listener != null) {
            holder?.itemView?.setOnClickListener {
                listener?.OnBannerClick(data, real)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateHolder(parent, viewType)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun getRealCount() : Int {
        val count = mDatas.size
        return if (count <= 1) count else count - 2
    }

    override fun setOnBannerListener(listener: OnBannerListener) {
        this.listener = listener
    }


}