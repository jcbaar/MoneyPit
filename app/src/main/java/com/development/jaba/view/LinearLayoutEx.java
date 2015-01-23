package com.development.jaba.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.development.jaba.moneypit.R;

/**
 * Simple {@link android.widget.LinearLayout} extension that will allow
 * for collapsing and expanding it's contents vertically.
 */
public class LinearLayoutEx extends LinearLayout {

    private ValueAnimator mAnimator;
    private int mDuration;
    private int mMeasuredHeight = 0;

    public LinearLayoutEx(Context context) {
        this(context, null, 0);
    }

    public LinearLayoutEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearLayoutEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get out our attributes.
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LinearLayoutEx,
                    0, 0);

            try {
                mDuration = a.getInteger(R.styleable.LinearLayoutEx_lleAnimationDuration, 100);
            } catch (Exception e) {
                Log.e("LinearLayoutEx", "Unable to load attributes");
            } finally {
                a.recycle();
            }
            attachPreDrawListener();
        }
    }


    /**
     * Sets up the {@link android.animation.ValueAnimator} for expanding the layout
     * vertically.
     */
    private void setupAnimator() {

        // We need to restrict the layout to the current width of the screen so
        // that it will correctly calculate the necessary height for the contents.
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        final int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        measure(widthSpec, heightSpec);

        // Setup the animator.
        mMeasuredHeight = getMeasuredHeight();
        mAnimator = ViewHeightAnimator(0, mMeasuredHeight);
    }

    /**
     * Attaches the {@link android.view.ViewTreeObserver.OnPreDrawListener} to the {@link View}. When
     * this event occurs we can correctly setup the {@link android.animation.ValueAnimator} since at
     * this point we know the correct layout height.
     */
    public void attachPreDrawListener() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                setVisibility(View.GONE);
                setupAnimator();
                return true;
            }
        });
    }

    /**
     * Recomputes the total height necessary to show all information.
     */
    public void recomputeHeight() {
        setVisibility(View.GONE);
        setupAnimator();
    }

    /**
     * Gets the 'expanded' state.
     *
     * @return True if the layout is expanded. False it it is not.
     */
    public boolean isExpanded() {
        return getVisibility() == View.VISIBLE;
    }

    /**
     * Toggle the expansion state of the layout.
     */
    public void toggle() {
        if (isExpanded()) {
            collapse();
        } else {
            expand();
        }
    }

    /**
     * Expands the layout if it is not yet expanded.
     */
    public void expand() {
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        setVisibility(View.VISIBLE);
        mAnimator.setDuration(mDuration);
        mAnimator.start();
    }

    /**
     * Expands the layout if it is not yet expanded. It expands the
     * layout without any animation.
     */
    public void expandNoAnim() {
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = mMeasuredHeight;
        setLayoutParams(layoutParams);
    }

    /**
     * Collapses the layout if it is not yet collapsed.
     */
    public void collapse() {
        if (getVisibility() == View.GONE) {
            return;
        }

        int finalHeight = getHeight();
        ValueAnimator animator = ViewHeightAnimator(finalHeight, 0);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                // At the end of the collapse animation we set out visibility to GONE.
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();
    }

    /**
     * Collapses the layout if it is not yet collapsed. It collapses the
     * layout without any animation.
     */
    public void collapseNoAnim() {
        if (getVisibility() == View.GONE) {
            return;
        }
        setVisibility(View.GONE);

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = 0;
        setLayoutParams(layoutParams);
    }

    /**
     * Create the {@link android.animation.ValueAnimator} for showing or hiding the
     * {@link com.development.jaba.view.LinearLayoutEx} contents in an animated way.
     *
     * @param start The start value (height) of the animation.
     * @param end   The end value (height) of the animation.
     * @return The created {@link android.animation.ValueAnimator}.
     */
    private ValueAnimator ViewHeightAnimator(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(mDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = value;
                setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
