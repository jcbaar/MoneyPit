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
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.development.jaba.moneypit.R;

/**
 * Simple {@link android.widget.LinearLayout} extension that will allow
 * for collapsing and expanding it's contents vertically.
 */
public class LinearLayoutEx extends LinearLayout {

    private ValueAnimator mAnimatorExpand;
    private ValueAnimator mAnimatorCollapse;
    private int mDuration;
    private int mMeasuredHeight = 0;
    private ExpansionStateListener mEsListener;

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
     * Sets the {@link com.development.jaba.view.LinearLayoutEx.ExpansionStateListener}.
     *
     * @param listener The {@link com.development.jaba.view.LinearLayoutEx.ExpansionStateListener} or null to clear it.
     */
    public void setExpansionStateListener(ExpansionStateListener listener) {
        mEsListener = listener;
    }

    /**
     * Sets up the {@link android.animation.ValueAnimator} for expanding the layout
     * vertically.
     */
    private void setupAnimators() {

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

        // Setup the animators.
        mMeasuredHeight = getMeasuredHeight();
        mAnimatorExpand = ViewHeightAnimator(0, mMeasuredHeight);
        mAnimatorExpand.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mEsListener != null) {
                    mEsListener.OnExpansionStateChanged(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mAnimatorCollapse = ViewHeightAnimator(mMeasuredHeight, 0);
        mAnimatorCollapse.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                // At the end of the collapse animation we set out visibility to GONE.
                setVisibility(View.GONE);
                if (mEsListener != null) {
                    mEsListener.OnExpansionStateChanged(false);
                }
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
                setupAnimators();
                return true;
            }
        });
    }

    /**
     * Recomputes the total height necessary to show all information.
     */
    public void recomputeHeight() {
        setVisibility(View.GONE);
        setupAnimators();
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
     * Toggle the expansion state of the layout without animation.
     */
    public void toggleNoAnim() {
        if(isExpanded()) {
            collapseNoAnim();
        }
        else {
            expandNoAnim();
        }
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
        mAnimatorExpand.setDuration(mDuration);
        mAnimatorExpand.start();
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
        mAnimatorExpand.setDuration(0);
        mAnimatorExpand.start();
    }

    /**
     * Collapses the layout if it is not yet collapsed.
     */
    public void collapse() {
        if (getVisibility() == View.GONE) {
            return;
        }
        mAnimatorCollapse.setDuration(mDuration);
        mAnimatorCollapse.start();
    }

    /**
     * Collapses the layout if it is not yet collapsed. It collapses the
     * layout without any animation.
     */
    public void collapseNoAnim() {
        if (getVisibility() == View.GONE) {
            return;
        }
        mAnimatorCollapse.setDuration(0);
        mAnimatorCollapse.start();
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
        animator.setInterpolator(new DecelerateInterpolator());
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

    /**
     * Interface for a listener which is called whenever the expansion
     * state has changed.
     */
    public interface ExpansionStateListener {
        void OnExpansionStateChanged(boolean isExpanded);
    }
}
