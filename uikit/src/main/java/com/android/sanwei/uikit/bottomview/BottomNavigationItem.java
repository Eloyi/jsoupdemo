package com.android.sanwei.uikit.bottomview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.android.sanwei.uikit.theme.ResourceHelper;


public class BottomNavigationItem {

    private int mIconResource;
    Drawable mIcon;

    private int mInactiveIconResource;
    private Drawable mInactiveIcon;
    private boolean inActiveIconAvailable = false;

    private int mTitleResource;
    private String mTitle;

    private int mActiveColorResource;
    private String mActiveColorCode;
    private int mActiveColor;

    private int mInActiveColorResource;
    private String mInActiveColorCode;
    private int mInActiveColor;

    private String mActiveColorKey;
    private String mInActiveColorKey;

    private String keyIconResource;
    private String keyTitleResource;

    public BottomNavigationItem(String keyIconResource, String keyTitleResource) {
        this.keyIconResource = keyIconResource;
        this.keyTitleResource = keyTitleResource;
    }

    public BottomNavigationItem(@DrawableRes int mIconResource, @NonNull String mTitle) {
        this.mIconResource = mIconResource;
        this.mTitle = mTitle;
    }

    /**
     * @param mIcon  drawable icon for the Tab.
     * @param mTitle title for the Tab.
     */
    public BottomNavigationItem(Drawable mIcon, @NonNull String mTitle) {
        this.mIcon = mIcon;
        this.mTitle = mTitle;
    }

    /**
     * @param mIcon          drawable icon for the Tab.
     * @param mTitleResource resource for the title.
     */
    public BottomNavigationItem(Drawable mIcon, @StringRes int mTitleResource) {
        this.mIcon = mIcon;
        this.mTitleResource = mTitleResource;
    }

    /**
     * @param mIconResource  resource for the Tab icon.
     * @param mTitleResource resource for the title.
     */
    public BottomNavigationItem(@DrawableRes int mIconResource, @StringRes int mTitleResource) {
        this.mIconResource = mIconResource;
        this.mTitleResource = mTitleResource;
    }

    /**
     * By default library will switch the color of icon provided (in between active and in-active icons)
     * This method is used, if people need to set different icons for active and in-active modes.
     *
     * @param mInactiveIcon in-active drawable icon
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setInactiveIcon(Drawable mInactiveIcon) {
        if (mInactiveIcon != null) {
            this.mInactiveIcon = mInactiveIcon;
            inActiveIconAvailable = true;
        }
        return this;
    }

    /**
     * By default library will switch the color of icon provided (in between active and in-active icons)
     * This method is used, if people need to set different icons for active and in-active modes.
     *
     * @param mInactiveIconResource resource for the in-active icon.
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setInactiveIconResource(@DrawableRes int mInactiveIconResource) {
        this.mInactiveIconResource = mInactiveIconResource;
        inActiveIconAvailable = true;
        return this;
    }


    /**
     * @param colorResource resource for active color
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setActiveColorResource(@ColorRes int colorResource) {
        this.mActiveColorResource = colorResource;
        return this;
    }

    /**
     * @param colorCode color code for active color
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setActiveColor(@Nullable String colorCode) {
        this.mActiveColorCode = colorCode;
        return this;
    }

    /**
     * @param color active color
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setActiveColor(int color) {
        this.mActiveColor = color;
        return this;
    }

    /**
     * @param colorResource resource for in-active color
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setInActiveColorResource(@ColorRes int colorResource) {
        this.mInActiveColorResource = colorResource;
        return this;
    }

    /**
     * @param colorCode color code for in-active color
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setInActiveColor(@Nullable String colorCode) {
        this.mInActiveColorCode = colorCode;
        return this;
    }

    /**
     * @param color in-active color
     * @return this, to allow builder pattern
     */
    public BottomNavigationItem setInActiveColor(int color) {
        this.mInActiveColor = color;
        return this;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Library only access method
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param context to fetch drawable
     * @return icon drawable
     */
    Drawable getIcon(Context context) {
        if (this.mIconResource != 0) {
            return ContextCompat.getDrawable(context, this.mIconResource);
        } else {
            return this.mIcon;
        }
    }

    public void setIconResource(int mIconResource) {
        this.mIconResource = mIconResource;
    }

    public void setIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    /**
     * @param context to fetch resource
     * @return title string
     */
    String getTitle(Context context) {
        if (this.mTitleResource != 0) {
            return context.getString(this.mTitleResource);
        } else {
            return this.mTitle;
        }
    }

    /**
     * @param context to fetch resources
     * @return in-active icon drawable
     */
    Drawable getInactiveIcon(Context context) {
        if (this.mInactiveIconResource != 0) {
            return ContextCompat.getDrawable(context, this.mInactiveIconResource);
        } else {
            return this.mInactiveIcon;
        }
    }

    /**
     * @return if in-active icon is set
     */
    boolean isInActiveIconAvailable() {
        return inActiveIconAvailable;
    }

    /**
     * @param context to fetch color
     * @return active color (or) -1 if no color is specified
     */
    int getActiveColor(Context context) {
        if (TextUtils.isEmpty(mActiveColorKey)){
            Object object = ResourceHelper.INSTANCE.getColor(mActiveColorKey, context);

            if (object != null && (int)object != 0){
                return (int) object;
            }
        }

        if (this.mActiveColorResource != 0) {
            return ContextCompat.getColor(context, mActiveColorResource);
        } else if (!TextUtils.isEmpty(mActiveColorCode)) {
            return Color.parseColor(mActiveColorCode);
        } else if (this.mActiveColor != 0) {
            return mActiveColor;
        } else {
            return BottomNavigationHelper.TRANSPARENT;
        }
    }

    /**
     * @param context to fetch color
     * @return in-active color (or) -1 if no color is specified
     */
    int getInActiveColor(Context context) {

        if (TextUtils.isEmpty(mInActiveColorKey)){
            Object object = ResourceHelper.INSTANCE.getColor(mInActiveColorKey, context);

            if (object != null && (int)object != 0){
                return (int) object;
            }
        }

        if (this.mInActiveColorResource != 0) {
            return ContextCompat.getColor(context, mInActiveColorResource);
        } else if (!TextUtils.isEmpty(mInActiveColorCode)) {
            return Color.parseColor(mInActiveColorCode);
        } else if (this.mInActiveColor != 0) {
            return mInActiveColor;
        } else {
            return BottomNavigationHelper.TRANSPARENT;
        }
    }

    public String getKeyIconResource() {
        return keyIconResource;
    }

    public void setKeyIconResource(String keyIconResource) {
        this.keyIconResource = keyIconResource;
    }

    public String getKeyTitleResource() {
        return keyTitleResource;
    }

    public void setKeyTitleResource(String keyTitleResource) {
        this.keyTitleResource = keyTitleResource;
    }

    public void setActiveColorKey(String mInActiveColorResourceKey) {
        this.mActiveColorKey = mInActiveColorResourceKey;
    }

    public void setInActiveColorKey(String mInActiveColorKey) {
        this.mInActiveColorKey = mInActiveColorKey;
    }
}
