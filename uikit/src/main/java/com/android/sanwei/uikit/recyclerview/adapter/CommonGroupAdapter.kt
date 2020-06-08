package com.android.sanwei.uikit.recyclerview.adapter

import android.text.TextUtils
import com.android.sanwei.uikit.UiKitConstants
import com.android.sanwei.uikit.recyclerview.CommonAdapter
import com.android.sanwei.uikit.recyclerview.CommonViewHolder

/**
 * group adapter has these items
 *
 * group title
 * group content
 * group footer
 * expired title
 * expired content
 * expired footer
 * recommend item
 */

open class CommonGroupAdapter<T : IGroupItem, VH : CommonViewHolder>(
    layout: Int,
    enableLoadmore: Boolean = false,
    enableRefresh: Boolean = false
) :
    CommonAdapter<IGroupItem, VH>(
        layout,
        enableLoadMoreModule = enableLoadmore,
        enableRefreshModule = enableRefresh
    ) {

    var orignGroupItems: MutableList<T>? = null
    //for save groupItem & groupTitle
    var filterGroupItems: MutableList<IGroupItem> = mutableListOf()
    //for display groupItem & groupTitlez
    var collapseGroupItems: MutableList<IGroupItem> = mutableListOf()

    var orignRecommandItems: MutableList<T>? = null

    companion object {
        val SUB_TYPE_GROUP_TITLE = 20001
        val SUB_TYPE_CONTENT = 20002
        val SUB_TYPE_CONTENT_FOOTER = 20003
        val SUB_TYPE_INVALID_GROUP_TITLE = 20004
        val SUB_TYPE_INVALID_GROUP_CONTENT = 20005
        val SUB_TYPE_RECOMMAND_TITLE = 20006
        val SUB_TYPE_RECOMMAND_CONTENT = 20007
    }

    //data type .
    override fun convert(helper: VH, item: IGroupItem?) {
        //wait real adapter to implement this
    }

    // only remommand item use count
    override fun getItemSpanCount(type: Int, spanCount: Int): Int {
        return when (type) {
            SUB_TYPE_GROUP_TITLE -> spanCount
            SUB_TYPE_CONTENT -> spanCount
            SUB_TYPE_CONTENT_FOOTER -> spanCount
            SUB_TYPE_INVALID_GROUP_TITLE -> spanCount
            SUB_TYPE_INVALID_GROUP_CONTENT -> spanCount
            SUB_TYPE_RECOMMAND_TITLE -> spanCount
            SUB_TYPE_RECOMMAND_CONTENT -> 1
            else -> 1
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        val item = data[position]

        return item.getSubtype()
    }

    fun refreshGroupItems(items: MutableList<T>) {
        //filter data
        data.clear()

        orignGroupItems = items

        val list = filterGroupItem(items)

        filterGroupItems.clear()

        filterGroupItems.addAll(list)

        collapseGroupItems.clear()

        collapseGroupItems.addAll(list)

        orignRecommandItems?.let {
            //add recommand title
            list.add(
                TitleGroupItemImpl(
                    "recommand_id",
                    "recommand_group",
                    false,
                    SUB_TYPE_RECOMMAND_TITLE
                )
            )

            list.addAll(it)
        }

        data = list

        writeDataInList()

        notifyDataSetChanged()
    }

    fun refreshRecommandItem(items: MutableList<T>) {
        //filter data
        orignRecommandItems = items

        //get shoping car item, notifydatasetchanged
        var position = 0

        for ((index, value) in data.reversed().withIndex()) {
            val subType = value.getSubtype()
            if (subType != SUB_TYPE_RECOMMAND_TITLE && subType != SUB_TYPE_RECOMMAND_CONTENT) {
                position = index
                break
            } else {
                data.remove(value)
            }
        }

        //add recommand title
        data.add(
            TitleGroupItemImpl(
                "recommand_id",
                "recommand_group",
                false,
                SUB_TYPE_RECOMMAND_TITLE
            )
        )

        //wrapper recommand content value
        items.forEach {
            it.setSubType(SUB_TYPE_RECOMMAND_CONTENT)
        }

        data.addAll(items)

        writeDataInList()

        //FIXME for invoid recyclerview IndexOutOfBoundsException
        notifyDataSetChanged()
//        notifyItemRangeChanged(position, items.size + 1)

    }

    fun addRecommandItem(items: MutableList<T>) {

        val position = data.size

        if (orignRecommandItems == null) {
            orignRecommandItems = items
        } else {

            orignRecommandItems?.addAll(items)
        }
        //wrapper recommand content value
        items.forEach {
            it.setSubType(SUB_TYPE_RECOMMAND_CONTENT)
        }

        data.addAll(items)

        writeDataInList()

        notifyItemRangeChanged(position, items.size)
    }

    //TODO
    fun addGroupItem(item: IGroupItem) {

    }

    /**
     * add or remove must be in expand mode
     *
     * remove single groupItem,
     * when only has one item then remove whole group
     */
    fun removeGroupItem(item: IGroupItem) {

        if (!isInExpandMode()) {
            return
        }

        //remove must be expand status
        val parentId = item.getItemParentId()
        var parentPosition: Int = 0
        var childPosition: Int = 0
        var childSize: Int = 0
        var footerSize = 0
        //find item group, if item group only has one item
        for ((index, value) in filterGroupItems.withIndex()) {
            val id = value.getItemId()
            val parent = value.getItemParentId()
            if (isParentGroupItem(item, value)) {
                parentPosition = index
            } else if (isGroupData(parentId, value)) {

                if (value.isSameData(item)) {
                    childPosition = index
                }

                if (!isFooterItem(value.getItemId())) {
                    childSize++
                } else {
                    footerSize++
                }

                if (childPosition > 0 && childSize > 1) {
                    break
                }
            }
        }

        if (childPosition <= 0) {
            //can't find child item
            return
        }

        if (childSize > 1) {
            //only remove one data if has multi child
            filterGroupItems.removeAt(childPosition)

            if (collapseGroupItems.size > 0) {
                collapseGroupItems.removeAt(childPosition)
            }

            deletData(childPosition)

        } else {
            //remove title & data if only has one data
            filterGroupItems.subList(parentPosition, parentPosition + 2 + footerSize).clear()

            if (collapseGroupItems.size > 0) {
                collapseGroupItems.subList(parentPosition, parentPosition + 2 + footerSize).clear()
            }

            deleteDataRange(parentPosition, 2 + footerSize)
        }
    }

    /**
     * add or remove must be in expand mode
     * remove groupTitle & groupContent
     */
    fun removeGroup(groupId: String) {

        if (!isInExpandMode()) {
            return
        }

        var parentPosition: Int = 0
        var childSize: Int = 0

        //find item group, if item group only has one item
        for ((index, value) in filterGroupItems.withIndex()) {
            val id = value.getItemId()
            val parent = value.getItemParentId()
            if (TextUtils.equals(id, groupId)) {
                parentPosition = index
            } else if (isGroupData(groupId, value)) {
                childSize++
            }
        }

        if (parentPosition < 0 || childSize <= 0) {
            //can't find group item
            return
        }

        filterGroupItems.subList(parentPosition, parentPosition + childSize + 1).clear()

        if (collapseGroupItems.size > 0) {
            collapseGroupItems.subList(parentPosition, parentPosition + childSize + 1).clear()
        }

        deleteDataRange(parentPosition, childSize + 1)
    }

    fun selectItem(item: IGroupItem, status: Boolean) {
        if (!isInExpandMode()) {
            return
        }

        //remove must be expand status
        val parentId = item.getItemParentId()
        var parentPosition: Int = 0
        var childPosition: Int = 0
        var childSize: Int = 0
        var footerSize = 0
        var parentItem: IGroupItem? = null
        var isSelectAll = true

        //find item group, if item group only has one item
        for ((index, value) in filterGroupItems.withIndex()) {
            val id = value.getItemId()
            val parent = value.getItemParentId()
            if (isParentGroupItem(item, value)) {
                parentPosition = index
                parentItem = value
            } else if (isGroupData(parentId, value)) {

                if (value.isSameData(item)) {
                    childPosition = index
                    value.setSelectStatus(status)
                }

                if (!isFooterItem(value.getItemId())) {
                    childSize++
                    if (!value.isSelected()) {
                        isSelectAll = false
                    }
                } else {
                    footerSize++
                }
            }
        }

        if (childPosition <= 0) {
            //can't find child item
            return
        }

        if (parentPosition < 0) {
            return
        }

        if (parentItem == null) {
            return
        }

        if (isSelectAll xor parentItem.isSelected()) {
            parentItem.setSelectStatus(isSelectAll)
            notifyRealListItemRangeChanged(parentPosition, childSize + 1)
        } else {
            notifyRealListItemChanged(childPosition)
        }

    }

    fun selectGroup(groupId: String, status: Boolean) {
        //change all group child status & norifydatasetchange

        if (!isInExpandMode()) {
            return
        }

        var parentPosition: Int = 0
        var childSize: Int = 0
        var footerSize = 0
        var parentItem: IGroupItem? = null

        //find item group, if item group only has one item
        for ((index, value) in filterGroupItems.withIndex()) {
            val id = value.getItemId()
            val parent = value.getItemParentId()
            if (TextUtils.equals(id, groupId)) {
                parentPosition = index
                parentItem = value
            } else if (isGroupData(groupId, value)) {

                if (!isFooterItem(value.getItemId())) {
                    value.setSelectStatus(status)
                    childSize++
                } else {
                    footerSize++
                }
            }
        }

        if (parentPosition < 0) {
            return
        }

        if (parentItem == null) {
            return
        }

        parentItem.setSelectStatus(status)
        notifyRealListItemRangeChanged(parentPosition, childSize + 1)
    }

    fun selectAllValid(status: Boolean) {
        var count = 0
        for ((index, value) in filterGroupItems.withIndex()) {
            if (!value.isExpired()) {
                count++
                value.setSelectStatus(status)
            }
        }

        notifyRealListItemRangeChanged(0, count)
    }

    /**
     * collapse group
     * actullay is remove...
     */
    fun collapseGroup(groupId: String) {

        var parentPosition: Int = 0
        var childSize: Int = 0

        if (collapseGroupItems.isEmpty()) {
            collapseGroupItems.addAll(filterGroupItems)
        }

        //find item group, if item group only has one item
        for ((index, value) in collapseGroupItems.withIndex()) {
            val id = value.getItemId()
            val parent = value.getItemParentId()
            if (TextUtils.equals(id, groupId)) {
                parentPosition = index
            } else if (isGroupData(groupId, value)) {
                if (!isFooterItem(value.getItemId())) {
                    childSize++
                }
            }
        }

        if (parentPosition < 0 || childSize <= 0) {
            //can't find group item
            return
        }

        collapseGroupItems.subList(parentPosition + 1, parentPosition + childSize + 1).clear()

        deleteDataRange(parentPosition + 1, childSize)
    }

    private fun isGroupData(groupId: String, item: IGroupItem): Boolean {
        if (TextUtils.equals(groupId, UiKitConstants.GROUP_ADAPTER_INVALID_GROUP_ID)) {
            //item isExpired & item parent don't have valid item
            if (item.getSubtype() == SUB_TYPE_GROUP_TITLE || item.getSubtype() == SUB_TYPE_INVALID_GROUP_TITLE){
                return false
            }
            val parent = item.getItemParentId()
            val isValidGroup = isValidGroup(parent)
            if (isValidGroup) {
                return false
            }
            return true
        }
        return TextUtils.equals(item.getItemParentId(), groupId)
    }

    private fun isValidGroup(parentId: String): Boolean {
        filterGroupItems.forEach {
            if (TextUtils.equals(it.getItemId(), parentId)) {
                return !it.isExpired()
            }
        }
        return false
    }

    private fun isParentGroupItem(actionItem: IGroupItem, filterItem: IGroupItem): Boolean {

        if (actionItem.isExpired()) {
            return TextUtils.equals(
                UiKitConstants.GROUP_ADAPTER_INVALID_GROUP_ID,
                filterItem.getItemId()
            )
        }
        return TextUtils.equals(actionItem.getItemParentId(), filterItem.getItemId())
    }

    private fun isFooterItem(id: String): Boolean {
        return id.endsWith(UiKitConstants.GROUP_ADAPTER_FOOTER_SUFFIX)
    }

    /**
     * expand group
     */
    fun expand(groupId: String) {

        var parentPosition: Int = 0
        var childSize: Int = 0

        if (collapseGroupItems.isEmpty() || collapseGroupItems.size == filterGroupItems.size) {
            //no need expand
            return
        }

        //find item group, if item group only has one item
        for ((index, value) in collapseGroupItems.withIndex()) {
            val id = value.getItemId()
            val parent = value.getItemParentId()
            if (TextUtils.equals(id, groupId)) {
                parentPosition = index
            } else if (isGroupData(groupId, value)) {
                if (!isFooterItem(value.getItemId())) {
                    childSize++
                }
            }
        }

        if (parentPosition < 0) {
            //can't find group item
            return
        }

        if (childSize > 0) {
            // no need expand there has item in group...
            return
        }

        //insert Value

        val insertList = getGroupItem(groupId)

        collapseGroupItems.addAll(parentPosition + 1, insertList)

        addItemsData(parentPosition + 1, insertList)
    }

    fun isInExpandMode(): Boolean {

        if (collapseGroupItems.isEmpty()) {
            return true
        }
        return collapseGroupItems.size == filterGroupItems.size
    }

    fun getGroupItem(groupId: String, includeFooter: Boolean = false): MutableList<IGroupItem> {
        val result = mutableListOf<IGroupItem>()
        filterGroupItems.forEach {
            if (isGroupData(groupId, it)) {
                if (includeFooter || !isFooterItem(it.getItemId())) {
                    result.add(it)
                }
            }
        }
        return result
    }

    //    TODO
    fun expandAll() {

    }

    private fun filterGroupItem(items: List<IGroupItem>): MutableList<IGroupItem> {
        var groupPosition = 0
        //get valid group, valid group will show group title, and wrap group item content
        val mGroupMap = mutableMapOf<String, GroupResult>()

        for (item in items) {
            val itemGroupName = item.getItemParentId()
            val itemValid = !item.isExpired()
            if (mGroupMap.containsKey(itemGroupName)) {
                val groupResult = mGroupMap[itemGroupName]
                val groupValid = groupResult?.isValid
                if (groupValid != null && groupValid) {
                    groupResult.addItem(item)
                    continue
                } else {
                    //groupResult is false before
                    groupResult?.isValid = itemValid
                    groupResult?.addItem(item)
                }
            } else {
                groupPosition++
                val newGroupResult = GroupResult(groupPosition, itemGroupName, itemValid)
                newGroupResult.addItem(item)
                mGroupMap[itemGroupName] = newGroupResult
            }
        }

        // create real data list, insert group title, expired title
        val validResultList = mutableListOf<IGroupItem>()
        val inValidResultList = mutableListOf<IGroupItem>()

        //TODO sort....
        mGroupMap.forEach {
            val groupName = it.key
            val groupResult = it.value
            if (groupResult.isValid) {
                //add title
                validResultList.add(
                    TitleGroupItemImpl(
                        groupName,
                        "",
                        false
                        , SUB_TYPE_GROUP_TITLE
                    )
                )
                groupResult.list.forEach { groupItem ->
                    groupItem.setSubType(SUB_TYPE_CONTENT)
                }
                validResultList.addAll(groupResult.list)

                validResultList.add(
                    FooterGroupItemImpl(
                        groupName,
                        false,
                        SUB_TYPE_CONTENT_FOOTER
                    )
                )
            } else {
                groupResult.list.forEach { groupItem ->
                    groupItem.setSubType(SUB_TYPE_INVALID_GROUP_CONTENT)
                }
                inValidResultList.addAll(groupResult.list)
            }
        }

        //create result data
        val resultList = mutableListOf<IGroupItem>()

        resultList.addAll(validResultList)

        // insert invalid item title if has invalid value

        if (inValidResultList.size > 0) {

            resultList.add(
                TitleGroupItemImpl(
                    UiKitConstants.GROUP_ADAPTER_INVALID_GROUP_ID,
                    "",
                    true,
                    SUB_TYPE_INVALID_GROUP_TITLE
                )
            )

            resultList.addAll(inValidResultList)

            resultList.add(FooterGroupItemImpl(UiKitConstants.GROUP_ADAPTER_INVALID_GROUP_ID, true, SUB_TYPE_CONTENT_FOOTER))
        }


        return resultList
    }


}

private class GroupResult(val weight: Int, val groupName: String, var isValid: Boolean) {
    val list = mutableListOf<IGroupItem>()

    fun addItem(item: IGroupItem) {
        list.add(item)
    }
}