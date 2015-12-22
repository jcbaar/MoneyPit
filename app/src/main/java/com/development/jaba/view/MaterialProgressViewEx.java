package com.development.jaba.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.development.jaba.moneypit.R;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Indeterminate progress view class. Simply extends the
 * {@link MaterialProgressBar} view class to add a startup delay property.
 */
public class MaterialProgressViewEx extends MaterialProgressBar {
    private final String TAG = "MaterialProgressViewEx";
    private final Handler mDelayedStart = new Handler();
    private final Runnable mDelayedRun;
    private int mStartDelay;

    public MaterialProgressViewEx(Context context) {
        this(context, null);
    }

    public MaterialProgressViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialProgressViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get out our attributes.
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.MaterialProgressViewEx,
                    0, 0);

            try {
                mStartDelay = a.getInteger(R.styleable.MaterialProgressViewEx_mpb_StartDelay, 0);
            } catch (Exception e) {
                Log.e(TAG, "Unable to load attributes");
            } finally {
                a.recycle();
            }
        }

        mDelayedRun = new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
            }
        };

        setVisibility(View.GONE);
    }

    public void setStartDelay(int delayInMs) {
        mStartDelay = delayInMs;
    }

    /**
     * Stops and hides the {@link com.development.jaba.view.MaterialProgressViewEx}.
     */
    public void stop() {
        mDelayedStart.removeCallbacks(mDelayedRun);
        setVisibility(View.GONE);
    }

    /**
     * Starts and makes visible the {@link com.development.jaba.view.MaterialProgressViewEx}.
     */
    public void start() {
        if (mStartDelay > 0) {
            mDelayedStart.postDelayed(mDelayedRun, mStartDelay);
        } else {
            setVisibility(View.VISIBLE);
        }
    }
}

