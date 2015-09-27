package com.development.jaba.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.development.jaba.moneypit.R;

/**
 * Circular indeterminate progress view class.
 * <p/>
 * Code from: Antoine Merle aka 'castorflex'
 * https://gist.github.com/castorflex/4e46a9dc2c3a4245a28e
 */
public class CircularProgressView extends View {
    private final String TAG = "CircularProgressView";
    private final Handler mDelayedStart = new Handler();
    private final Runnable mDelayedRun;
    private CircularProgressDrawable mDrawable;
    private int mColor;
    private int mStartDelay;

    public CircularProgressView(Context context) {
        this(context, null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get out our attributes.
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CircularProgressView,
                    0, 0);

            try {
                mColor = a.getColor(R.styleable.CircularProgressView_cpvColor, ContextCompat.getColor(context, R.color.primaryColor));
                mStartDelay = a.getInteger(R.styleable.CircularProgressView_cpvStartDelay, 0);
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

        mDrawable = new CircularProgressDrawable(mColor, 10);
        mDrawable.setCallback(this);
        setVisibility(View.GONE);
    }

    public void setStartDelay(int delayInMs) {
        mStartDelay = delayInMs;
    }

    /**
     * Stops and hides the {@link com.development.jaba.view.CircularProgressView}.
     */
    public void stop() {
        mDelayedStart.removeCallbacks(mDelayedRun);
        setVisibility(View.GONE);
    }

    /**
     * Starts and makes visible the {@link com.development.jaba.view.CircularProgressView}.
     */
    public void start() {
        if (mStartDelay > 0) {
            mDelayedStart.postDelayed(mDelayedRun, mStartDelay);
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * Automatically starts the view animation when becoming visible or stops the view animation
     * when becoming gone or invisible.
     *
     * @param changedView The {@link android.view.View}.
     * @param visibility  The new visibility state.
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mDrawable != null) {
            if (visibility == VISIBLE) {
                mDrawable.start();
            } else {
                mDrawable.stop();
            }
        }
    }

    /**
     * Recomputes the drawable bounds.
     *
     * @param w    The new width.
     * @param h    The new Height.
     * @param oldw The old width.
     * @param oldh The old height.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
    }

    /**
     * Renders the {@link com.development.jaba.view.CircularProgressDrawable}.
     *
     * @param canvas The {@link android.graphics.Canvas} on which to render.
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mDrawable.draw(canvas);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mDrawable || super.verifyDrawable(who);
    }
}

