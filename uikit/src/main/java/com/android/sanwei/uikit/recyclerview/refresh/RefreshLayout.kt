package com.android.sanwei.uikit.recyclerview.refresh

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import com.android.sanwei.uikit.R
import com.android.sanwei.uikit.UikitProvider

class RefreshLayout : LinearLayout {

    companion object {
        const val STATE_NORMAL = 0
        const val STATE_RELEASE_TO_REFRESH = 1
        const val STATE_REFRESHING = 2
        const val STATE_COMPLETE = 3

    }
    var TIME_DELAY_COMPETE_TO_NORMAL : Long = 200
    var TIME_DELAY_RESET_TO_NORMAL : Long = 500
    var TIME_SCROLL_TIME : Long = 300

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        STATE_COMPLETE, STATE_RELEASE_TO_REFRESH, STATE_REFRESHING
    )
    annotation class REFRESH_STATUS {}

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

    var mContainer: LinearLayout? = null
    var mArrowImage: ImageView? = null
    var mProgress: ProgressBar? = null
    var mStatusText: TextView? = null

    var mLastState = STATE_NORMAL
        private set
    //arrow animation
    var mRotateUpAnim: Animation? = null
    var mRotateDownAnim: Animation? = null
    var mAnimationDuration: Long = 180

    var mMeasuredHeight: Int = 0

    //init views
    private fun init(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int? = 0,
        defStyleRes: Int? = 0
    ) {
        mContainer = LayoutInflater.from(getContext()).inflate(
            R.layout.basic_refresh_layout,
            null
        ) as LinearLayout?

        mArrowImage = mContainer?.findViewById(R.id.refresh_arrow)
        mProgress = mContainer?.findViewById(R.id.refresh_progressbar)
        mStatusText = mContainer?.findViewById(R.id.refresh_status_textview)

        val lp = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, 0)
        layoutParams = lp
        setPadding(0, 0, 0, 0)
        addView(mContainer, LayoutParams(LayoutParams.MATCH_PARENT, 0))
        gravity = Gravity.BOTTOM

        //arrowview
        mRotateUpAnim = RotateAnimation(
            0.0f,
            -180.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        mRotateUpAnim?.duration = mAnimationDuration
        mRotateUpAnim?.fillAfter = true

        mRotateDownAnim = RotateAnimation(
            -180.0f,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        mRotateDownAnim?.duration = mAnimationDuration
        mRotateDownAnim?.fillAfter = true

        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mMeasuredHeight = measuredHeight
    }

    fun getState(): Int {
        return mLastState
    }

    private fun changeState(@REFRESH_STATUS status: Int) {
        if (status == mLastState) {
            return
        }
        when (status) {
            STATE_NORMAL -> {
                mArrowImage?.visibility = View.VISIBLE
                mProgress?.visibility = View.INVISIBLE

                if (mLastState == STATE_RELEASE_TO_REFRESH) {
                    mArrowImage?.startAnimation(mRotateDownAnim)
                }
                if (mLastState == STATE_REFRESHING) {
                    mArrowImage?.clearAnimation()
                }
                mStatusText?.text = getNormalStatusText()
            }
            STATE_RELEASE_TO_REFRESH -> {
                mArrowImage?.visibility = View.VISIBLE
                mProgress?.visibility = View.INVISIBLE

                if (mLastState != STATE_RELEASE_TO_REFRESH) {
                    mArrowImage?.clearAnimation()
                    mArrowImage?.startAnimation(mRotateUpAnim)
                    mStatusText?.text = getRelaseAndRefreshText()
                }
            }
            STATE_REFRESHING -> {
                mArrowImage?.visibility = View.INVISIBLE
                mArrowImage?.clearAnimation()
                mProgress?.visibility = View.VISIBLE
                smoothScrollTo(mMeasuredHeight)
                mStatusText?.text = getInRefreshingText()
            }
            STATE_COMPLETE -> {
                mArrowImage?.visibility = View.INVISIBLE
                mProgress?.visibility = View.INVISIBLE
                mStatusText?.text = getRefreshComplete()
            }
        }

        mLastState = status
    }

    private fun setVisibleHeight(height: Int) {
        val realHeight = if (height >= 0) height else 0
        val lp = mContainer?.layoutParams ?: return
        lp.height = realHeight
        mContainer?.layoutParams = lp
    }

    fun getVisibleHeight () : Int{
        return mContainer?.layoutParams?.height ?: 0
    }

    private fun smoothScrollTo(destHeight: Int) {
        val animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight)
        animator.setDuration(TIME_SCROLL_TIME).start()
        animator.addUpdateListener { animation -> setVisibleHeight(animation.animatedValue as Int) }
        animator.start()
    }

    fun getNormalStatusText(): String {
        return UikitProvider.formatString(R.string.common_refresh) ?: "refresh"
    }

    fun getRelaseAndRefreshText(): String {
        return UikitProvider.formatString(R.string.common_release_fresh) ?: "release to refresh"
    }

    fun getInRefreshingText(): String {
        return UikitProvider.formatString(R.string.common_refreshing) ?: "refreshing"
    }

    fun getRefreshComplete(): String {
        return UikitProvider.formatString(R.string.common_refresh_complete) ?: "refresh complete"
    }

    fun onMove(delta : Float){
        if (getVisibleHeight() > 0 || delta > 0){
            setVisibleHeight((delta + getVisibleHeight()).toInt())
            if (mLastState <= STATE_RELEASE_TO_REFRESH){
                if (getVisibleHeight() > mMeasuredHeight){
                    changeState(STATE_RELEASE_TO_REFRESH)
                }else {
                    changeState(STATE_NORMAL)
                }
            }
        }
    }

    fun refreshComplete(){
        changeState(STATE_COMPLETE)
        postDelayed({ reset() }, TIME_DELAY_COMPETE_TO_NORMAL)
    }

    fun reset(){
        smoothScrollTo(0)
        postDelayed({changeState(STATE_NORMAL)}, TIME_DELAY_RESET_TO_NORMAL)
    }

    fun releaseType():Boolean{
        var isOnRefresh = false
        val height = getVisibleHeight()
        if (height == 0){
            isOnRefresh = false
        }

        if (height > mMeasuredHeight && mLastState < STATE_REFRESHING){
            changeState(STATE_REFRESHING)
            isOnRefresh = true
        }

        if (mLastState == STATE_REFRESHING && height <= mMeasuredHeight){
            //return
        }
        if (mLastState != STATE_REFRESHING){
            smoothScrollTo(0)
        }
        if (mLastState == STATE_REFRESHING){
            val destHeight = mMeasuredHeight
            smoothScrollTo(destHeight)
        }

        return  isOnRefresh
    }


}