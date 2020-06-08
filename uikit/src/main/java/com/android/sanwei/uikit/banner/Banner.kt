package com.android.sanwei.uikit.banner

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider
import com.android.sanwei.uikit.banner.adapter.BannerAdapter
import com.android.sanwei.uikit.banner.bean.BaseBanner
import com.android.sanwei.uikit.banner.config.BannerConfig
import com.android.sanwei.uikit.banner.config.IndicatorConfig
import com.android.sanwei.uikit.banner.indicator.Indicator
import com.android.sanwei.uikit.banner.listener.OnBannerListener
import com.android.sanwei.uikit.banner.util.BannerUtils
import com.android.sanwei.uikit.theme.ResourceHelper
import com.android.sanwei.uikit.theme.SWThemeResource
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.ref.WeakReference

class Banner<T> : FrameLayout, SWThemeResource {


    private var mViewPager2: ViewPager2? = null
    private var mLoopTask: AutoLoopTask? = null
    private var listener: OnBannerListener? = null
    private var pageListener: ViewPager.OnPageChangeListener? = null
    private var mAdapter: BannerAdapter? = null
    private var mIndicator: Indicator? = null
    private var mIsAutoLoop = false
    private var mDelayTime: Long = 0
    private var mCurrentPosition = 1
    private var normalWidth = 0
    private var selectedWidth = 0

    private var normalColor: Int? = null
    private var selectedColor: Int? = null

    private var normalColorKey: String? = null
    private var selectColorKey: String? = null
    private var indicatorGravity = 0
    private var indicatorSpace = 0
    private var indicatorMargin = 0
    private var indicatorMarginLeft = 0
    private var indicatorMarginTop = 0
    private var indicatorMarginRight = 0
    private var indicatorMarginBottom = 0

    companion object {
        const val TAG = "banner_log"
        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef(HORIZONTAL, VERTICAL)
    annotation class Orientation {}

    constructor(context: Context) : super(context) {
        initConstructor(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initConstructor(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initConstructor(context, attrs, defStyleAttr)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initConstructor(context, attrs, defStyleAttr, defStyleRes)
    }

    fun initConstructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        init(context)
        initTypedArray(context, attrs)
    }

    private fun init(context: Context) {
        mLoopTask = AutoLoopTask(this)
        mViewPager2 = ViewPager2(context)
        mViewPager2?.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mViewPager2?.offscreenPageLimit = 3
        mViewPager2?.registerOnPageChangeCallback(BannerOnPageChangeCallback())
        addView(mViewPager2)
    }

    private fun initTypedArray(
        context: Context,
        attrs: AttributeSet?
    ) {
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.Banner)
        mDelayTime = a.getInt(R.styleable.Banner_delay_time, BannerConfig.DELAY_TIME).toLong()
        mIsAutoLoop = a.getBoolean(R.styleable.Banner_is_auto_loop, BannerConfig.IS_AUTO_LOOP)
        normalWidth = a.getDimensionPixelSize(
            R.styleable.Banner_indicator_normal_width,
            BannerConfig.INDICATOR_NORMAL_WIDTH.toInt()
        )
        selectedWidth = a.getDimensionPixelSize(
            R.styleable.Banner_indicator_selected_width,
            BannerConfig.INDICATOR_SELECTED_WIDTH.toInt()
        )
        normalColorKey = a.getString(R.styleable.Banner_indicator_normal_color_key)
        selectColorKey = a.getString(R.styleable.Banner_indicator_selected_color_key)

        normalColor = ResourceHelper.getColor(normalColorKey, context)
        selectedColor = ResourceHelper.getColor(selectColorKey, context)

        if (normalColor == null || normalColor == 0) {
            normalColor = a.getColor(R.styleable.Banner_indicator_normal_color, 0)
        }

        if (selectedColor == null || selectedColor == 0) {
            selectedColor = a.getColor(R.styleable.Banner_indicator_selected_color, 0)
        }

        indicatorGravity =
            a.getInt(R.styleable.Banner_indicator_gravity, IndicatorConfig.Direction.CENTER)
        indicatorSpace = a.getDimensionPixelSize(R.styleable.Banner_indicator_space, 0)
        indicatorMargin = a.getDimensionPixelSize(R.styleable.Banner_indicator_margin, 0)
        indicatorMarginLeft = a.getDimensionPixelSize(R.styleable.Banner_indicator_marginLeft, 0)
        indicatorMarginTop = a.getDimensionPixelSize(R.styleable.Banner_indicator_marginTop, 0)
        indicatorMarginRight = a.getDimensionPixelSize(R.styleable.Banner_indicator_marginRight, 0)
        indicatorMarginBottom =
            a.getDimensionPixelSize(R.styleable.Banner_indicator_marginBottom, 0)
        val orientation = a.getInt(R.styleable.Banner_orientation, HORIZONTAL)
        setOrientation(orientation)
        a.recycle()
    }

    private fun initIndicatorAttr() {
        if (indicatorMargin != 0) {
            setIndicatorMargins(IndicatorConfig.Margins(indicatorMargin))
        } else if (indicatorMarginLeft != 0 || indicatorMarginTop != 0 || indicatorMarginRight != 0 || indicatorMarginBottom != 0
        ) {
            setIndicatorMargins(
                IndicatorConfig.Margins(
                    indicatorMarginLeft,
                    indicatorMarginTop,
                    indicatorMarginRight,
                    indicatorMarginBottom
                )
            )
        }
        if (indicatorSpace > 0) {
            setIndicatorSpace(indicatorSpace.toFloat())
        }
        if (indicatorGravity != IndicatorConfig.Direction.CENTER) {
            setIndicatorGravity(indicatorGravity)
        }
        normalColor?.let {
            if (it != 0) {
                setIndicatorNormalColor(it)
            }
        }

        selectedColor?.let {
            if (it != 0) {
                setIndicatorSelectedColor(it)
            }
        }

        if (normalWidth > 0) {
            setIndicatorNormalWidth(normalWidth)
        }
        if (selectedWidth > 0) {
            setIndicatorSelectedWidth(selectedWidth)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val funAttach = UikitProvider.funViewAttachWindowCallback
        funAttach?.invoke(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        val funDetach = UikitProvider.funViewDettachWindowCallback

        funDetach?.invoke(this)
    }

    override fun reload() {

        var needChange = false

        if (!TextUtils.isEmpty(normalColorKey)) {
            normalColor = ResourceHelper.getColor(normalColorKey, context)
            normalColor?.let {
                setIndicatorNormalColor(it)
            }
            needChange = true
        }

        if (!TextUtils.isEmpty(selectColorKey)) {
            selectedColor = ResourceHelper.getColor(selectColorKey, context)
            selectedColor?.let {
                setIndicatorSelectedColor(it)
            }
            needChange = true
        }

        if (needChange) {
            mIndicator?.indicatorView?.invalidate()

        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mIsAutoLoop) start()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE
        ) {
            if (mIsAutoLoop) start()
        } else if (action == MotionEvent.ACTION_DOWN) {
            if (mIsAutoLoop) stop()
        }
        return super.dispatchTouchEvent(ev)
    }

    internal inner class BannerOnPageChangeCallback : OnPageChangeCallback() {
        private fun filterPosition(position: Int): Boolean {
            return (position == 1 && mCurrentPosition == itemCount - 1
                    || position == realCount && mCurrentPosition == 0)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            if (filterPosition(position)) return
            val realPosition = BannerUtils.getRealPosition(position, realCount)
            pageListener?.onPageScrolled(realPosition, positionOffset, positionOffsetPixels)
            mIndicator?.onPageScrolled(realPosition, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            if (filterPosition(position)) {
                mCurrentPosition = position

                return
            }
            mCurrentPosition = position
            val realPosition = BannerUtils.getRealPosition(position, realCount)

            listener?.onBannerChanged(realPosition)
            pageListener?.onPageSelected(realPosition)
            mIndicator?.onPageSelected(realPosition)
        }

        override fun onPageScrollStateChanged(state: Int) {
            when (state) {
                ViewPager2.SCROLL_STATE_DRAGGING -> if (mCurrentPosition == 0) {
                    setCurrentItem(realCount, false)
                } else if (mCurrentPosition == itemCount - 1) {
                    setCurrentItem(1, false)
                }
            }
            pageListener?.onPageScrollStateChanged(state)
            mIndicator?.onPageScrollStateChanged(state)
        }
    }

    internal class AutoLoopTask(banner: Banner<*>) : Runnable {
        private val reference: WeakReference<Banner<*>> = WeakReference(banner)

        override fun run() {
            val banner = reference.get()
            if (banner != null && banner.mIsAutoLoop) {
                val count = banner.itemCount
                if (count <= 1) return
                val next = banner.getCurrentItem() % (count - 1) + 1
                if (next == 1) {
                    banner.setCurrentItem(next, false)
                    banner.post(banner.mLoopTask)
                } else {
                    banner.setCurrentItem(next, false)
                    banner.postDelayed(banner.mLoopTask, banner.mDelayTime)
                }
            }
        }

    }

    /**
     * setCurrentItem only work when viewpager has these position
     */
    private fun setCurrentItem(position: Int, smoothScroll: Boolean) {
        mViewPager2?.setCurrentItem(position, smoothScroll)
    }

    private fun getCurrentItem() :Int{
        return  mViewPager2?.currentItem ?: 0
    }

    private val itemCount: Int
        private get() = mAdapter?.getItemCount() ?: 0

    private fun initIndicator() {
        if (mIndicator == null) return
        removeIndicator()
        addView(mIndicator?.indicatorView)
        initIndicatorAttr()
        val realPosition = BannerUtils.getRealPosition(getCurrentItem(), realCount)
        mIndicator?.onPageChanged(realCount, realPosition)
    }

    fun removeIndicator() {
        removeView(mIndicator?.indicatorView)
    }

    /**
     * **********************************************************************
     * ------------------------ 对外公开API ---------------------------------*
     * **********************************************************************
     */

    val viewPager2: ViewPager2?
        get() = mViewPager2


    val indicatorConfig: IndicatorConfig?
        get() = if (mIndicator != null) {
            mIndicator?.indicatorConfig
        } else null

    /**
     * 返回banner真实总数
     *
     * @return
     */
    val realCount: Int
        get() = mAdapter?.getRealCount() ?: 0

    /**
     * 禁止手动滑动
     *
     * @param enabled true 允许，false 禁止
     * @return
     */
    fun setUserInputEnabled(enabled: Boolean): Banner<*> {
        viewPager2?.isUserInputEnabled = enabled
        return this
    }


    fun setPageTransformer(transformer: ViewPager2.PageTransformer?): Banner<*> {
        viewPager2?.setPageTransformer(transformer)
        return this
    }

    fun addItemDecoration(decor: ItemDecoration): Banner<*> {
        viewPager2?.addItemDecoration(decor)
        return this
    }

    fun addItemDecoration(decor: ItemDecoration, index: Int): Banner<*> {
        viewPager2?.addItemDecoration(decor, index)
        return this
    }

    /**
     * 重新设置banner数据，当然你也可以在你adapter中自己操作数据
     *
     * @param datas 数据集合，当传null或者datas没有数据时，banner会变成空白的，请做好占位UI处理
     * @return
     */
    fun setDatas(datas: List<BaseBanner>, initPosition: Boolean ?= false): Banner<*> {
        val adp = mAdapter
        if (adp != null) {
            adp.setDatas(datas)

            if (initPosition == true){
                setCurrentItem(mCurrentPosition, false)
            }
            val realPosition = BannerUtils.getRealPosition(getCurrentItem(), realCount)
            mIndicator?.onPageChanged(realCount, realPosition)

            stop()
            if (mIsAutoLoop) start()
        }
        return this
    }

    /**
     * 是否允许自动轮播
     *
     * @param isAutoLoop ture 允许，false 不允许
     * @return
     */
    fun isAutoLoop(isAutoLoop: Boolean) {
        mIsAutoLoop = isAutoLoop
    }

    /**
     * 设置轮播间隔时间
     *
     * @param delayTime 时间（毫秒）
     * @return
     */
    fun setDelayTime(delayTime: Long) {
        mDelayTime = delayTime
    }

    /**
     * 开始轮播
     *
     * @return
     */
    fun start() {
        if (mIsAutoLoop) {
            stop()
            postDelayed(mLoopTask, mDelayTime)
        }
    }

    /**
     * 停止轮播
     *
     * @return
     */
    fun stop() {
        removeCallbacks(mLoopTask)
    }

    /**
     * 设置banner的适配器
     *
     * @param adapter
     * @return
     */
    fun setAdapter(adapter: BannerAdapter?) {

        mAdapter = adapter
        if (adapter is RecyclerView.Adapter<*>) {
            mViewPager2?.adapter = adapter
            setCurrentItem(mCurrentPosition, false)
            initIndicator()
        }
    }

    /**
     * 设置banner轮播方向
     *
     * @param orientation [Orientation]
     * @return
     */
    fun setOrientation(@Orientation orientation: Int): Banner<*> {
        mViewPager2?.orientation = orientation
        return this
    }

    /**
     * 设置轮播指示器
     *
     * @param indicator
     */
    fun setIndicator(indicator: Indicator) {
        if (mIndicator === indicator) return
        removeIndicator()
        mIndicator = indicator
        if (mAdapter != null) {
            initIndicator()
        }
    }

    fun setIndicatorSelectedColor(@ColorInt color: Int) {
        mIndicator?.indicatorConfig?.selectedColor = color
    }

    fun setIndicatorSelectedColorRes(@ColorRes color: Int) {
        setIndicatorSelectedColor(ContextCompat.getColor(context, color))
    }

    fun setIndicatorNormalColor(@ColorInt color: Int) {
        mIndicator?.indicatorConfig?.normalColor = color
    }

    fun setIndicatorNormalColorRes(@ColorRes color: Int) {
        setIndicatorNormalColor(ContextCompat.getColor(context, color))
    }

    fun setIndicatorGravity(@IndicatorConfig.Direction gravity: Int) {
        mIndicator?.indicatorConfig?.gravity = gravity
        mIndicator?.indicatorView?.postInvalidate()
    }

    fun setIndicatorSpace(indicatorSpace: Float) {
        mIndicator?.indicatorConfig?.indicatorSpace = indicatorSpace
    }

    fun setIndicatorMargins(margins: IndicatorConfig.Margins?) {
        mIndicator?.indicatorConfig?.margins = margins
        mIndicator?.indicatorView?.requestLayout()
    }

    fun setIndicatorWidth(normalWidth: Int, selectedWidth: Int) {
        mIndicator?.indicatorConfig?.normalWidth = normalWidth.toFloat()
        mIndicator?.indicatorConfig?.selectedWidth = selectedWidth.toFloat()
    }

    fun setIndicatorNormalWidth(normalWidth: Int) {
        mIndicator?.indicatorConfig?.normalWidth = normalWidth.toFloat()
    }

    fun setIndicatorSelectedWidth(selectedWidth: Int) {
        mIndicator?.indicatorConfig?.selectedWidth = selectedWidth.toFloat()
    }


    fun setOnBannerListener(listener: OnBannerListener) {
        mAdapter?.setOnBannerListener(listener)
        this.listener = listener
    }

    /**
     * 添加viewpager切换事件
     *
     *
     * 在viewpager2中切换事件[ViewPager2.OnPageChangeCallback]是一个抽象类，
     * 为了方便使用习惯这里用的是和viewpager一样的[ViewPager.OnPageChangeListener]接口
     *
     *
     * @param pageListener
     */
    fun addOnPageChangeListener(pageListener: ViewPager.OnPageChangeListener) {
        this.pageListener = pageListener
    }

    fun destroy() {
        stop()
        mViewPager2?.adapter = null
        mViewPager2 = null
    }


}