package com.android.sanwei.uikit.recyclerview.loadmore

import android.view.View
import com.android.sanwei.uikit.recyclerview.CommonViewHolder
import com.android.sanwei.uikit.R

class SimpleLoadMoreView : BaseLoadMoreView() {
    override fun getRootView(): Int {
        return R.layout.common_view_load_more
    }

    override fun getLoadingView(holder: CommonViewHolder): View? {
        return holder.getView(R.id.load_more_loading_view)
    }

    override fun getLoadComplete(holder: CommonViewHolder): View? {
        return holder.getView(R.id.load_more_load_complete_view)
    }

    override fun getLoadEndView(holder: CommonViewHolder): View? {
        return holder.getView(R.id.load_more_load_end_view)
    }

    override fun getLoadFailView(holder: CommonViewHolder): View? {
        return holder.getView(R.id.load_more_load_fail_view)
    }
}