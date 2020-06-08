package com.android.sanwei.uikit.bottomview

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider.funViewAttachWindowCallback
import com.android.sanwei.uikit.UikitProvider.funViewDettachWindowCallback
import com.android.sanwei.uikit.bottomview.BottomNavigationHelper.bindTabWithData
import com.android.sanwei.uikit.bottomview.BottomNavigationHelper.getMeasurementsForFixedMode
import com.android.sanwei.uikit.bottomview.BottomNavigationHelper.getMeasurementsForShiftingMode
import com.android.sanwei.uikit.bottomview.BottomNavigationHelper.setBackgroundWithRipple
import com.android.sanwei.uikit.theme.ResourceHelper.getColor
import com.android.sanwei.uikit.theme.SWThemeResource
import com.android.sanwei.uikit.util.DisplayUtil
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.ref.WeakReference
import java.util.*

class BottomNavigationBar : FrameLayout, SWThemeResource {

    companion object {
        const val MODE_DEFAULT = 0
        const val MODE_FIXED = 1
        const val MODE_SHIFTING = 2
        const val MODE_FIXED_NO_TITLE = 3
        const val MODE_SHIFTING_NO_TITLE = 4
        const val BACKGROUND_STYLE_DEFAULT = 0
        const val BACKGROUND_STYLE_STATIC = 1
        const val BACKGROUND_STYLE_RIPPLE = 2
        private const val FAB_BEHAVIOUR_TRANSLATE_AND_STICK = 0
        private const val FAB_BEHAVIOUR_DISAPPEAR = 1
        private const val FAB_BEHAVIOUR_TRANSLATE_OUT = 2
        private val INTERPOLATOR: Interpolator =
            LinearOutSlowInInterpolator()
        private const val MIN_SIZE = 3
        private const val MAX_SIZE = 5
        private const val DEFAULT_SELECTED_POSITION = 0
        private const val DEFAULT_ANIMATION_DURATION = 200
        private const val S_ACTION_VIEWPAGER_MOVE = 1 shl 2
        private const val S_ACTION_VIEWPAGER_CLIK = 1 shl 3
    }

    @IntDef(
        MODE_DEFAULT,
        MODE_FIXED,
        MODE_SHIFTING,
        MODE_FIXED_NO_TITLE,
        MODE_SHIFTING_NO_TITLE
    )
    @Retention(RetentionPolicy.SOURCE)
    internal annotation class Mode

    @IntDef(
        BACKGROUND_STYLE_DEFAULT,
        BACKGROUND_STYLE_STATIC,
        BACKGROUND_STYLE_RIPPLE
    )
    @Retention(RetentionPolicy.SOURCE)
    internal annotation class BackgroundStyle

    @IntDef(
        FAB_BEHAVIOUR_TRANSLATE_AND_STICK,
        FAB_BEHAVIOUR_DISAPPEAR,
        FAB_BEHAVIOUR_TRANSLATE_OUT
    )
    @Retention(RetentionPolicy.SOURCE)
    internal annotation class FabBehaviour

    @Mode
    private var mMode = MODE_DEFAULT
    @BackgroundStyle
    private var mBackgroundStyle = BACKGROUND_STYLE_DEFAULT
    private var mTranslationAnimator: ViewPropertyAnimatorCompat? = null
    private var mScrollable = false
    var mBottomNavigationItems =
        ArrayList<BottomNavigationItem>()
    var mBottomNavigationTabs =
        ArrayList<BottomNavigationTab>()
    /**
     * @return current selected position
     */
    var currentSelectedPosition = DEFAULT_SELECTED_POSITION
        private set
    private var mTabSelectedListener: OnTabSelectedListener? =
        null
    private var mReloadBadgeFun :(() -> Unit) ?= null
    /**
     * @return activeColor
     */
    var activeColor: Int? = 0
    /**
     * @return in-active color
     */
    var inActiveColor: Int? = 0
    /**
     * @return background color
     */
    var backgroundColor: Int? = 0

    private var mActiveColorKey: String? = null
    private var mInActiveColorKey: String? = null
    private var mBackgroundColorKey: String? = null
    private var mBackgroundOverlay: FrameLayout? = null
    private var mContainer: FrameLayout? = null
    var mTabContainer: LinearLayout? = null
    /**
     * @return animation duration
     */
    var animationDuration = DEFAULT_ANIMATION_DURATION
        private set
    private var mRippleAnimationDuration =
        (DEFAULT_ANIMATION_DURATION * 2.5).toInt()
    private var mElevation = 0f

///////////////////////////////////////////////////////////////////////////
// Behaviour Handing Handling
///////////////////////////////////////////////////////////////////////////
    var isAutoHideEnabled = false
    var isHidden = false
        private set
    private var mViewPager: ViewPager2? = null
    private var mPageChangeListener: BottomNavigationBarOnPageChangeListener? = null
    private var actionFlag = 0


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    fun init(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {

        parseAttrs(context, attrs)
        init()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        funViewAttachWindowCallback?.invoke(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        funViewDettachWindowCallback?.invoke(this)
    }

    /**
     * This method initiates the bottomNavigationBar properties,
     * Tries to get them form XML if not preset sets them to their default values.
     *
     * @param context context of the bottomNavigationBar
     * @param attrs   attributes mentioned in the layout XML by user
     */
    private fun parseAttrs(
        context: Context,
        attrs: AttributeSet?
    ) {
        if (attrs != null) {
            val typedArray = context.theme
                .obtainStyledAttributes(attrs, R.styleable.BottomNavigationBar, 0, 0)
            mActiveColorKey =
                typedArray.getString(R.styleable.BottomNavigationBar_bnbActiveColorKey)
            mInActiveColorKey =
                typedArray.getString(R.styleable.BottomNavigationBar_bnbInactiveColorKey)
            mBackgroundColorKey =
                typedArray.getString(R.styleable.BottomNavigationBar_bnbBackgroundColorKey)

            val activeColor: Any? = getColor(mActiveColorKey, context)
            val inactiveColor: Any? = getColor(mInActiveColorKey, context)
            val backgroundColor: Any? = getColor(mBackgroundColorKey, context)

            if (activeColor != null && activeColor as Int != 0) {
                this.activeColor = activeColor
            } else {
                this.activeColor = typedArray.getColor(
                    R.styleable.BottomNavigationBar_bnbActiveColor,
                    DisplayUtil.fetchContextColor(context, R.attr.colorAccent)
                )
            }

            if (inactiveColor != null && inactiveColor as Int != 0) {
                inActiveColor = inactiveColor
            } else {
                inActiveColor = typedArray.getColor(
                    R.styleable.BottomNavigationBar_bnbInactiveColor,
                    Color.LTGRAY
                )
            }

            if (backgroundColor != null && backgroundColor as Int != 0) {
                this.backgroundColor = backgroundColor
            } else {
                this.backgroundColor = typedArray.getColor(
                    R.styleable.BottomNavigationBar_bnbBackgroundColor,
                    Color.WHITE
                )
            }
            isAutoHideEnabled =
                typedArray.getBoolean(R.styleable.BottomNavigationBar_bnbAutoHideEnabled, true)
            mElevation = typedArray.getDimension(
                R.styleable.BottomNavigationBar_bnbElevation,
                resources.getDimension(R.dimen.bottom_navigation_elevation)
            )
            setAnimationDuration(
                typedArray.getInt(
                    R.styleable.BottomNavigationBar_bnbAnimationDuration,
                    DEFAULT_ANIMATION_DURATION
                )
            )

            mMode = when (typedArray.getInt(
                R.styleable.BottomNavigationBar_bnbMode,
                MODE_DEFAULT
            )) {
                MODE_FIXED -> MODE_FIXED
                MODE_SHIFTING -> MODE_SHIFTING
                MODE_FIXED_NO_TITLE -> MODE_FIXED_NO_TITLE
                MODE_SHIFTING_NO_TITLE -> MODE_SHIFTING_NO_TITLE
                MODE_DEFAULT -> MODE_DEFAULT
                else -> MODE_DEFAULT
            }

            mBackgroundStyle = when (typedArray.getInt(
                R.styleable.BottomNavigationBar_bnbBackgroundStyle,
                BACKGROUND_STYLE_DEFAULT
            )) {
                BACKGROUND_STYLE_STATIC -> BACKGROUND_STYLE_STATIC
                BACKGROUND_STYLE_RIPPLE -> BACKGROUND_STYLE_RIPPLE
                BACKGROUND_STYLE_DEFAULT -> BACKGROUND_STYLE_DEFAULT
                else -> BACKGROUND_STYLE_DEFAULT
            }

            typedArray.recycle()
        } else {
            activeColor = DisplayUtil.fetchContextColor(context, R.attr.colorAccent)
            inActiveColor = Color.LTGRAY
            backgroundColor = Color.WHITE
            mElevation = resources.getDimension(R.dimen.bottom_navigation_elevation)
        }
    }

    /**
     * This method initiates the bottomNavigationBar and handles layout related values
     */
    private fun init() {
//        MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getContext().getResources().getDimension(R.dimen.bottom_navigation_padded_height)));
//        marginParams.setMargins(0, (int) getContext().getResources().getDimension(R.dimen.bottom_navigation_top_margin_correction), 0, 0);
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        val inflater = LayoutInflater.from(context)
        val parentView =
            inflater.inflate(R.layout.bottom_navigation_bar_container, this, true)
        mBackgroundOverlay =
            parentView.findViewById(R.id.bottom_navigation_bar_overLay)
        mContainer = parentView.findViewById(R.id.bottom_navigation_bar_container)
        mTabContainer =
            parentView.findViewById(R.id.bottom_navigation_bar_item_container)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.outlineProvider = ViewOutlineProvider.BOUNDS
        } else { //to do
        }
        ViewCompat.setElevation(this, mElevation)
        clipToPadding = false
    }
    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }
///////////////////////////////////////////////////////////////////////////
// View Data Setter methods, Called before Initialize method
///////////////////////////////////////////////////////////////////////////
    /**
     * Used to add a new tab.
     *
     * @param item bottom navigation tab details
     * @return this, to allow builder pattern
     */
    fun addItem(item: BottomNavigationItem): BottomNavigationBar {
        mBottomNavigationItems.add(item)
        return this
    }

    /**
     * Used to remove a tab.
     * you should call initialise() after this to see the results effected.
     *
     * @param item bottom navigation tab details
     * @return this, to allow builder pattern
     */
    fun removeItem(item: BottomNavigationItem?): BottomNavigationBar {
        mBottomNavigationItems.remove(item)
        return this
    }

    /**
     * @param mode any of the three Modes supported by library
     * @return this, to allow builder pattern
     */
    fun setMode(@Mode mode: Int): BottomNavigationBar {
        mMode = mode
        return this
    }

    /**
     * @param backgroundStyle any of the three Background Styles supported by library
     * @return this, to allow builder pattern
     */
    fun setBackgroundStyle(@BackgroundStyle backgroundStyle: Int): BottomNavigationBar {
        mBackgroundStyle = backgroundStyle
        return this
    }

    /**
     * @param activeColor res code for the default active color
     * @return this, to allow builder pattern
     */
    fun setActiveColor(@ColorRes activeColor: Int): BottomNavigationBar {
        this.activeColor = ContextCompat.getColor(context, activeColor)
        return this
    }

    /**
     * @param activeColorCode color code in string format for the default active color
     * @return this, to allow builder pattern
     */
    fun setActiveColor(activeColorCode: String?): BottomNavigationBar {
        activeColor = Color.parseColor(activeColorCode)
        return this
    }

    /**
     * @param inActiveColor res code for the default in-active color
     * @return this, to allow builder pattern
     */
    fun setInActiveColor(@ColorRes inActiveColor: Int): BottomNavigationBar {
        this.inActiveColor = ContextCompat.getColor(context, inActiveColor)
        return this
    }

    /**
     * @param inActiveColorCode color code in string format for the default in-active color
     * @return this, to allow builder pattern
     */
    fun setInActiveColor(inActiveColorCode: String?): BottomNavigationBar {
        inActiveColor = Color.parseColor(inActiveColorCode)
        return this
    }

    /**
     * @param backgroundColor res code for the default background color
     * @return this, to allow builder pattern
     */
    fun setBarBackgroundColor(@ColorRes backgroundColor: Int): BottomNavigationBar {
        this.backgroundColor = ContextCompat.getColor(context, backgroundColor)
        return this
    }

    /**
     * @param backgroundColorCode color code in string format for the default background color
     * @return this, to allow builder pattern
     */
    fun setBarBackgroundColor(backgroundColorCode: String?): BottomNavigationBar {
        backgroundColor = Color.parseColor(backgroundColorCode)
        return this
    }

    /**
     * will be public once all bugs are resolved.
     */
    private fun setScrollable(scrollable: Boolean): BottomNavigationBar {
        mScrollable = scrollable
        return this
    }
    ///////////////////////////////////////////////////////////////////////////
// Initialise Method
///////////////////////////////////////////////////////////////////////////
    /**
     * This method should be called at the end of all customisation method.
     * This method will take all changes in to consideration and redraws tabs.
     */
    @JvmOverloads
    fun initialise(
        selectPosition: Int = DEFAULT_SELECTED_POSITION,
        forceRefresh: Boolean = true
    ) {
        mBottomNavigationTabs.clear()
        if (!mBottomNavigationItems.isEmpty()) {
            mTabContainer?.removeAllViews()
            if (mMode == MODE_DEFAULT) {
                mMode = if (mBottomNavigationItems.size <= MIN_SIZE) {
                    MODE_FIXED
                } else {
                    MODE_SHIFTING
                }
            }
            if (mBackgroundStyle == BACKGROUND_STYLE_DEFAULT) {
                mBackgroundStyle = if (mMode == MODE_FIXED) {
                    BACKGROUND_STYLE_STATIC
                } else {
                    BACKGROUND_STYLE_RIPPLE
                }
            }
            if (mBackgroundStyle == BACKGROUND_STYLE_STATIC) {
                mBackgroundOverlay?.visibility = View.GONE
                backgroundColor?.let {
                    mContainer?.setBackgroundColor(it)
                }
            }
            val screenWidth = DisplayUtil.getScreenWidth(context)
            if (mMode == MODE_FIXED || mMode == MODE_FIXED_NO_TITLE) {
                val widths = getMeasurementsForFixedMode(
                    context,
                    screenWidth,
                    mBottomNavigationItems.size,
                    mScrollable
                )
                val itemWidth = widths[0]
                for (currentItem in mBottomNavigationItems) {
                    val bottomNavigationTab =
                        FixedBottomNavigationTab(context)
                    setUpTab(
                        mMode == MODE_FIXED_NO_TITLE,
                        bottomNavigationTab,
                        currentItem,
                        itemWidth,
                        itemWidth
                    )
                }
            } else if (mMode == MODE_SHIFTING || mMode == MODE_SHIFTING_NO_TITLE) {
                val widths = getMeasurementsForShiftingMode(
                    context,
                    screenWidth,
                    mBottomNavigationItems.size,
                    mScrollable
                )
                val itemWidth = widths[0]
                val itemActiveWidth = widths[1]
                for (currentItem in mBottomNavigationItems) {
                    val bottomNavigationTab =
                        ShiftingBottomNavigationTab(context)
                    setUpTab(
                        mMode == MODE_SHIFTING_NO_TITLE,
                        bottomNavigationTab,
                        currentItem,
                        itemWidth,
                        itemActiveWidth
                    )
                }
            }
            if (mBottomNavigationTabs.size > selectPosition) {
                selectTabInternal(selectPosition, true, false, false, forceRefresh)
            } else if (!mBottomNavigationTabs.isEmpty()) {
                selectTabInternal(0, true, false, false, forceRefresh)
            }
        }
    }

    override fun reload() {
        val activeColor: Any? = getColor(mActiveColorKey, this.context)
        val inactiveColor: Any? =
            getColor(mInActiveColorKey, this.context)
        val backgroundColor: Any? =
            getColor(mBackgroundColorKey, this.context)
        if (activeColor != null && activeColor as Int != 0) {
            this.activeColor = activeColor
        }
        if (inactiveColor != null && inactiveColor as Int != 0) {
            inActiveColor = inactiveColor
        }
        if (backgroundColor != null && backgroundColor as Int != 0) {
            this.backgroundColor = backgroundColor
        }
        initialise(currentSelectedPosition, true)

        reloadBadge()
    }

    fun reloadBadge() {
        mReloadBadgeFun?.invoke()
    }

    fun setReloadBadge(function: () -> Unit) {
        mReloadBadgeFun = function
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
// Anytime Setter methods that can be called irrespective of whether we call initialise or not
////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @param tabSelectedListener callback listener for tabs
     * @return this, to allow builder pattern
     */
    fun setTabSelectedListener(tabSelectedListener: OnTabSelectedListener?): BottomNavigationBar {
        mTabSelectedListener = tabSelectedListener
        return this
    }

    /**
     * ripple animation will be 2.5 times this animation duration.
     *
     * @param animationDuration animation duration for tab animations
     * @return this, to allow builder pattern
     */
    fun setAnimationDuration(animationDuration: Int): BottomNavigationBar {
        this.animationDuration = animationDuration
        mRippleAnimationDuration = (animationDuration * 2.5).toInt()
        return this
    }

    /**
     * Clears all stored data and this helps to re-initialise tabs from scratch
     */
    fun clearAll() {
        mTabContainer?.removeAllViews()
        mBottomNavigationTabs.clear()
        mBottomNavigationItems.clear()
        mBackgroundOverlay?.visibility = View.GONE
        mContainer?.setBackgroundColor(Color.TRANSPARENT)
        currentSelectedPosition = DEFAULT_SELECTED_POSITION
    }
    /**
     * Should be called only after initialization of BottomBar(i.e after calling initialize method)
     *
     * @param newPosition  to select a tab after bottom navigation bar is initialised
     * @param callListener should this change call listener callbacks
     */
///////////////////////////////////////////////////////////////////////////
// Setter methods that should called only after initialise is called
///////////////////////////////////////////////////////////////////////////
    /**
     * Should be called only after initialization of BottomBar(i.e after calling initialize method)
     *
     * @param newPosition to select a tab after bottom navigation bar is initialised
     */
    @JvmOverloads
    fun selectTab(newPosition: Int, callListener: Boolean = true) {
        selectTabInternal(newPosition, false, callListener, callListener, false)
    }
    ///////////////////////////////////////////////////////////////////////////
// Internal Methods of the class
///////////////////////////////////////////////////////////////////////////
    /**
     * Internal method to setup tabs
     *
     * @param isNoTitleMode       if no title mode is required
     * @param bottomNavigationTab Tab item
     * @param currentItem         data structure for tab item
     * @param itemWidth           tab item in-active width
     * @param itemActiveWidth     tab item active width
     */
    private fun setUpTab(
        isNoTitleMode: Boolean,
        bottomNavigationTab: BottomNavigationTab,
        currentItem: BottomNavigationItem,
        itemWidth: Int,
        itemActiveWidth: Int
    ) {
        bottomNavigationTab.setIsNoTitleMode(isNoTitleMode)
        bottomNavigationTab.setInactiveWidth(itemWidth)
        bottomNavigationTab.setActiveWidth(itemActiveWidth)
        bottomNavigationTab.position = mBottomNavigationItems.indexOf(currentItem)
        bottomNavigationTab.setOnClickListener { v ->
            val bottomNavigationTabView = v as BottomNavigationTab
            selectTabInternal(bottomNavigationTabView.position, false, true, false, false)
        }
        mBottomNavigationTabs.add(bottomNavigationTab)
        val initialImmediately = bindTabWithData(
            currentItem,
            bottomNavigationTab,
            this,
            mBackgroundStyle
        )
        if (initialImmediately) {
            bottomNavigationTab.initialise(mBackgroundStyle == BACKGROUND_STYLE_STATIC)
        }
        mTabContainer?.addView(bottomNavigationTab)
    }

    /**
     * Internal Method to select a tab
     *
     * @param newPosition     to select a tab after bottom navigation bar is initialised
     * @param firstTab        if firstTab the no ripple animation will be done
     * @param callListener    is listener callbacks enabled for this change
     * @param forcedSelection if bottom navigation bar forced to select tab (in this case call on selected irrespective of previous state
     */
    private fun selectTabInternal(
        newPosition: Int,
        firstTab: Boolean,
        callListener: Boolean,
        forcedSelection: Boolean,
        forceRefresh: Boolean
    ) {
        val oldPosition = currentSelectedPosition
        if (forceRefresh || oldPosition != newPosition) {
            if (mBackgroundStyle == BACKGROUND_STYLE_STATIC) {
                if (newPosition != oldPosition) {
                    mBottomNavigationTabs[currentSelectedPosition].unSelect(
                        false,
                        animationDuration
                    )
                }
                mBottomNavigationTabs[newPosition].select(true, animationDuration)
            } else if (mBackgroundStyle == BACKGROUND_STYLE_RIPPLE) {
                if (newPosition != oldPosition) {
                    mBottomNavigationTabs[currentSelectedPosition].unSelect(
                        false,
                        animationDuration
                    )
                }
                mBottomNavigationTabs[newPosition].select(false, animationDuration)
                val clickedView = mBottomNavigationTabs[newPosition]
                if (firstTab) { // Running a ripple on the opening app won't be good so on firstTab this won't run.
                    mContainer?.setBackgroundColor(clickedView.activeColor)
                    mBackgroundOverlay?.visibility = View.GONE
                } else {
                    mBackgroundOverlay?.post {
                        //                            try {
                        setBackgroundWithRipple(
                            clickedView,
                            mContainer,
                            mBackgroundOverlay,
                            clickedView.activeColor,
                            mRippleAnimationDuration
                        )
                        //                            } catch (Exception e) {
//                                mContainer.setBackgroundColor(clickedView.getActiveColor());
//                                mBackgroundOverlay.setVisibility(View.GONE);
//                            }
                    }
                }
            }
            if (oldPosition != newPosition) { //setviewpager
                if (!checkActionFlag(S_ACTION_VIEWPAGER_MOVE)) {
                    addActionFlag(S_ACTION_VIEWPAGER_CLIK)
                    mViewPager?.currentItem = newPosition
                }
            }
            currentSelectedPosition = newPosition
        }
        if (!forceRefresh && callListener) {
            sendListenerCall(oldPosition, newPosition, forcedSelection)
        }
    }

    /**
     * Internal method used to send callbacks to listener
     *
     * @param oldPosition     old selected tab position, -1 if this is first call
     * @param newPosition     newly selected tab position
     * @param forcedSelection if bottom navigation bar forced to select tab (in this case call on selected irrespective of previous state
     */
    private fun sendListenerCall(
        oldPosition: Int,
        newPosition: Int,
        forcedSelection: Boolean
    ) {
        if (mTabSelectedListener != null) { //                && oldPosition != -1) {
            if (forcedSelection) {
                mTabSelectedListener?.onTabSelected(newPosition)
            } else {
                if (oldPosition == newPosition) {
                    mTabSelectedListener?.onTabReselected(newPosition)
                } else {
                    mTabSelectedListener?.onTabSelected(newPosition)
                    if (oldPosition != -1) {
                        mTabSelectedListener?.onTabUnselected(oldPosition)
                    }
                }
            }
        }
    }
    /**
     * show BottomNavigationBar if it is hidden and hide if it is shown
     *
     * @param animate is animation enabled for toggle
     */
///////////////////////////////////////////////////////////////////////////
// Animating methods
///////////////////////////////////////////////////////////////////////////
    /**
     * show BottomNavigationBar if it is hidden and hide if it is shown
     */
    @JvmOverloads
    fun toggle(animate: Boolean = true) {
        if (isHidden) {
            show(animate)
        } else {
            hide(animate)
        }
    }
    /**
     * @param animate is animation enabled for hide
     */
    /**
     * hide with animation
     */
    @JvmOverloads
    fun hide(animate: Boolean = true) {
        isHidden = true
        setTranslationY(this.height, animate)
    }
    /**
     * @param animate is animation enabled for show
     */
    /**
     * show with animation
     */
    @JvmOverloads
    fun show(animate: Boolean = true) {
        isHidden = false
        setTranslationY(0, animate)
    }

    /**
     * @param offset  offset needs to be set
     * @param animate is animation enabled for translation
     */
    private fun setTranslationY(offset: Int, animate: Boolean) {
        if (animate) {
            animateOffset(offset)
        } else {
            if (mTranslationAnimator != null) {
                mTranslationAnimator?.cancel()
            }
            this.translationY = offset.toFloat()
        }
    }

    /**
     * Internal Method
     *
     *
     * used to set animation and
     * takes care of cancelling current animation
     * and sets duration and interpolator for animation
     *
     * @param offset translation offset that needs to set with animation
     */
    private fun animateOffset(offset: Int) {
        if (mTranslationAnimator == null) {
            mTranslationAnimator = ViewCompat.animate(this)
            mTranslationAnimator?.duration = mRippleAnimationDuration.toLong()
            mTranslationAnimator?.interpolator = INTERPOLATOR
        } else {
            mTranslationAnimator?.cancel()
        }
        mTranslationAnimator?.translationY(offset.toFloat())?.start()
    }

    fun setupWithViewPager(viewPager: ViewPager2?) {
        if (mViewPager != null) { // If we've already been setup with a ViewPager, remove us from it
            mPageChangeListener?.let {
                mViewPager?.unregisterOnPageChangeCallback(it)
            }
        }
        if (null == viewPager) {
            mViewPager = null
            return
        }
        mViewPager = viewPager
        // Add our custom OnPageChangeListener to the ViewPager
        if (mPageChangeListener == null) {
            mPageChangeListener = BottomNavigationBarOnPageChangeListener(this)
        }
        mPageChangeListener?.let {
            viewPager.registerOnPageChangeCallback(it)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
// Getters
///////////////////////////////////////////////////////////////////////////

    private fun checkActionFlag(flag: Int): Boolean {
        return actionFlag and flag > 0
    }

    private fun addActionFlag(flag: Int) {
        actionFlag = actionFlag or flag
    }

    private fun removeActionFlag(flag: Int) {
        actionFlag = actionFlag and flag.inv()
    }




    ///////////////////////////////////////////////////////////////////////////
// Listener interfaces
///////////////////////////////////////////////////////////////////////////
    /**
     * Callback interface invoked when a tab's selection state changes.
     */
    interface OnTabSelectedListener {
        /**
         * Called when a tab enters the selected state.
         *
         * @param position The position of the tab that was selected
         */
        fun onTabSelected(position: Int)

        /**
         * Called when a tab exits the selected state.
         *
         * @param position The position of the tab that was unselected
         */
        fun onTabUnselected(position: Int)

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param position The position of the tab that was reselected.
         */
        fun onTabReselected(position: Int)
    }

    /**
     * Simple implementation of the OnTabSelectedListener interface with stub implementations of each method.
     * Extend this if you do not intend to override every method of OnTabSelectedListener.
     */
    class SimpleOnTabSelectedListener :
        OnTabSelectedListener {
        override fun onTabSelected(position: Int) {}
        override fun onTabUnselected(position: Int) {}
        override fun onTabReselected(position: Int) {}
    }

    private class BottomNavigationBarOnPageChangeListener(bnve: BottomNavigationBar) :
        OnPageChangeCallback() {
        private val mBnveRef: WeakReference<BottomNavigationBar>
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(
            position: Int, positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            val bnve = mBnveRef.get()
            if (null != bnve) {
                if (bnve.checkActionFlag(S_ACTION_VIEWPAGER_CLIK)) {
                    bnve.removeActionFlag(S_ACTION_VIEWPAGER_CLIK)
                } else {
                    bnve.addActionFlag(S_ACTION_VIEWPAGER_MOVE)
                    bnve.selectTab(position)
                    bnve.removeActionFlag(S_ACTION_VIEWPAGER_MOVE)
                }
            }
        }

        init {
            mBnveRef = WeakReference(bnve)
        }
    }


}