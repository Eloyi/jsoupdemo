package com.android.sanwei.uikit.recyclerview.adapter

import android.text.TextUtils
import com.android.sanwei.uikit.UiKitConstants

class FooterGroupItemImpl(
    val parentId: String,
    val expired: Boolean,
    val subType: Int
) : IGroupItem {
    var select : Boolean = false
    override fun getItemParentId(): String {
        return parentId
    }

    override fun getItemId(): String {
        return parentId + UiKitConstants.GROUP_ADAPTER_FOOTER_SUFFIX
    }

    override fun isExpired(): Boolean {
        return expired
    }

    override fun isTitle(): Boolean {
        return true
    }

    override fun getItemInfo(): String {
        return ""
    }

    override fun getSubtype(): Int {
        return subType
    }

    override fun setSubType(type: Int) {

    }

    //title only need check id, if data need check id,parentid,params...
    override fun isSameData(item: IGroupItem): Boolean {
        return TextUtils.equals(getItemId(), item.getItemId())
    }

    override fun setSelectStatus(status: Boolean) {
        select = status
    }

    override fun isSelected(): Boolean {
        return select
    }
}