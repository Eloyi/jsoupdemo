package com.android.sanwei.uikit.bottomview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.ViewAnimationUtils
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.theme.ResourceHelper

object BottomNavigationHelper {

    const val TRANSPARENT = Color.TRANSPARENT
    /**
     * Used to get Measurements for MODE_FIXED
     *
     * @param context     to fetch measurements
     * @param screenWidth total screen width
     * @param noOfTabs    no of bottom bar tabs
     * @param scrollable  is bottom bar scrollable
     * @return width of each tab
     */
    @JvmStatic
    fun getMeasurementsForFixedMode(
        context: Context,
        screenWidth: Int,
        noOfTabs: Int,
        scrollable: Boolean
    ): IntArray {
        val result = IntArray(2)
        val minWidth =
            context.resources.getDimension(R.dimen.fixed_min_width_small_views).toInt()
        val maxWidth = context.resources.getDimension(R.dimen.fixed_min_width).toInt()
        var itemWidth = screenWidth / noOfTabs
        if (itemWidth < minWidth && scrollable) {
            itemWidth = context.resources.getDimension(R.dimen.fixed_min_width).toInt()
        } else if (itemWidth > maxWidth) {
            itemWidth = maxWidth
        }
        result[0] = itemWidth
        return result
    }

    /**
     * Used to get Measurements for MODE_SHIFTING
     *
     * @param context     to fetch measurements
     * @param screenWidth total screen width
     * @param noOfTabs    no of bottom bar tabs
     * @param scrollable  is bottom bar scrollable
     * @return min and max width of each tab
     */
    @JvmStatic
    fun getMeasurementsForShiftingMode(
        context: Context,
        screenWidth: Int,
        noOfTabs: Int,
        scrollable: Boolean
    ): IntArray {
        val result = IntArray(2)
        val minWidth =
            context.resources.getDimension(R.dimen.shifting_min_width_inactive).toInt()
        val maxWidth =
            context.resources.getDimension(R.dimen.shifting_max_width_inactive).toInt()
        val minPossibleWidth = minWidth * (noOfTabs + 0.5)
        val maxPossibleWidth = maxWidth * (noOfTabs + 0.75)
        var itemWidth: Int
        var itemActiveWidth: Int
        if (screenWidth < minPossibleWidth) {
            if (scrollable) {
                itemWidth = minWidth
                itemActiveWidth = (minWidth * 1.5).toInt()
            } else {
                itemWidth = (screenWidth / (noOfTabs + 0.5)).toInt()
                itemActiveWidth = (itemWidth * 1.5).toInt()
            }
        } else if (screenWidth > maxPossibleWidth) {
            itemWidth = maxWidth
            itemActiveWidth = (itemWidth * 1.75).toInt()
        } else {
            val minPossibleWidth1 = minWidth * (noOfTabs + 0.625)
            val minPossibleWidth2 = minWidth * (noOfTabs + 0.75)
            itemWidth = (screenWidth / (noOfTabs + 0.5)).toInt()
            itemActiveWidth = (itemWidth * 1.5).toInt()
            if (screenWidth > minPossibleWidth1) {
                itemWidth = (screenWidth / (noOfTabs + 0.625)).toInt()
                itemActiveWidth = (itemWidth * 1.625).toInt()
                if (screenWidth > minPossibleWidth2) {
                    itemWidth = (screenWidth / (noOfTabs + 0.75)).toInt()
                    itemActiveWidth = (itemWidth * 1.75).toInt()
                }
            }
        }
        result[0] = itemWidth
        result[1] = itemActiveWidth
        return result
    }

    /**
     * Used to get set data to the Tab views from navigation items
     *
     * @param bottomNavigationItem holds all the data
     * @param bottomNavigationTab  view to which data need to be set
     * @param bottomNavigationBar  view which holds all the tabs
     */
    fun bindTabWithData(
        bottomNavigationItem: BottomNavigationItem,
        bottomNavigationTab: BottomNavigationTab,
        bottomNavigationBar: BottomNavigationBar,
        backgroundStyle: Int
    ): Boolean {
        var result = false
        val context = bottomNavigationBar.context
        if (TextUtils.isEmpty(bottomNavigationItem.keyTitleResource)) {
            bottomNavigationTab.setLabel(bottomNavigationItem.getTitle(context))
            bottomNavigationTab.setIcon(bottomNavigationItem.getIcon(context))
            setItemIcon(
                bottomNavigationItem,
                bottomNavigationTab,
                bottomNavigationBar,
                context
            )
            return true
        } else {
            ResourceHelper.setTextWithKey(bottomNavigationItem.keyTitleResource, bottomNavigationTab.labelView)

            bottomNavigationTab.setIconKey(bottomNavigationItem.keyIconResource)

            ResourceHelper.setDrawable(bottomNavigationItem.keyIconResource, bottomNavigationBar,
                { id ->
                    bottomNavigationItem.setIconResource(id)
                    bottomNavigationTab.setIcon(bottomNavigationItem.getIcon(context))

                    result = true
                }, { drawable ->

                    bottomNavigationItem.setIcon(drawable)
                    bottomNavigationTab.iconView.setImageDrawable(drawable)

                }
            )
        }

        setItemIcon(
            bottomNavigationItem,
            bottomNavigationTab,
            bottomNavigationBar,
            context
        )

        return result
    }

    private fun setItemIcon(
        bottomNavigationItem: BottomNavigationItem,
        bottomNavigationTab: BottomNavigationTab,
        bottomNavigationBar: BottomNavigationBar,
        context: Context
    ) {
        val activeColor = bottomNavigationItem.getActiveColor(context)
        val inActiveColor = bottomNavigationItem.getInActiveColor(context)
        if (activeColor != TRANSPARENT) {
            bottomNavigationTab.activeColor = activeColor
        } else {
            bottomNavigationTab.activeColor = bottomNavigationBar.activeColor ?: 0
        }
        if (inActiveColor != TRANSPARENT) {
            bottomNavigationTab.setInactiveColor(inActiveColor)
        } else {
            bottomNavigationTab.setInactiveColor(bottomNavigationBar.inActiveColor ?: 0)
        }
        if (bottomNavigationItem.isInActiveIconAvailable) {
            val inactiveDrawable = bottomNavigationItem.getInactiveIcon(context)
            if (inactiveDrawable != null) {
                bottomNavigationTab.setInactiveIcon(inactiveDrawable)
            }
        }
        bottomNavigationTab.setItemBackgroundColor(bottomNavigationBar.backgroundColor ?: 0)
    }

    /**
     * Used to set the ripple animation when a tab is selected
     *
     * @param clickedView       the view that is clicked (to get dimens where ripple starts)
     * @param backgroundView    temporary view to which final background color is set
     * @param bgOverlay         temporary view which is animated to get ripple effect
     * @param newColor          the new color i.e ripple color
     * @param animationDuration duration for which animation runs
     */
    @JvmStatic
    fun setBackgroundWithRipple(
        clickedView: View, backgroundView: View?,
        bgOverlay: View?, newColor: Int, animationDuration: Int
    ) {
        if (backgroundView == null){
            return
        }

        val centerX = (clickedView.x + clickedView.measuredWidth / 2).toInt()
        val centerY = clickedView.measuredHeight / 2
        val finalRadius = backgroundView.width
        backgroundView.clearAnimation()
        bgOverlay?.clearAnimation()
        val circularReveal: Animator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circularReveal = ViewAnimationUtils
                .createCircularReveal(bgOverlay, centerX, centerY, 0f, finalRadius.toFloat())
        } else {
            bgOverlay?.alpha = 0f
            circularReveal = ObjectAnimator.ofFloat(bgOverlay, "alpha", 0f, 1f)
        }
        circularReveal.duration = animationDuration.toLong()
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onCancel()
            }

            override fun onAnimationCancel(animation: Animator) {
                onCancel()
            }

            private fun onCancel() {
                backgroundView.setBackgroundColor(newColor)
                bgOverlay?.visibility = View.GONE
            }
        })
        bgOverlay?.setBackgroundColor(newColor)
        bgOverlay?.visibility = View.VISIBLE
        circularReveal.start()
    }
}