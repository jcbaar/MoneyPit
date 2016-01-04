package com.development.jaba.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * {@link android.support.v4.view.ViewPager} extended class which allows turning the actual
 * paging on or off.
 */
public class ViewPagerEx extends ViewPager {

    private boolean mSwipeEnabled;

    public ViewPagerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSwipeEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.mSwipeEnabled && super.onTouchEvent(event);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.mSwipeEnabled && super.onInterceptTouchEvent(event);

    }

    public void setSwipeEnabled(boolean enabled) {
        this.mSwipeEnabled = enabled;
    }

    public boolean getSwipeEnabled() {
        return this.mSwipeEnabled;
    }
}