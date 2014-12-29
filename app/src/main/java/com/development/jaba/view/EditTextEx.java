package com.development.jaba.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.development.jaba.moneypit.R;

public class EditTextEx extends android.support.v7.internal.widget.TintEditText {
    private static enum Animation {NONE, SHRINK, GROW}

    private final Paint mFloatingHintPaint = new Paint();
    private final ColorStateList mHintColors;
    private final ColorStateList mTextColors;
    private final float mHintScale;
    private final int mAnimationSteps;
    private int mMaxLength = -1;

    private boolean mWasEmpty;
    private int mAnimationFrame;
    private Animation mAnimation = Animation.NONE;

    public EditTextEx(Context context) {
        this(context, null);
    }

    public EditTextEx(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.eteHintEditTextStyle);
    }

    public EditTextEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.edittextex_hint_scale, typedValue, true);
        mHintScale = typedValue.getFloat();
        mAnimationSteps = getResources().getInteger(R.dimen.edittextex_hint_animation_steps);

        if (attrs != null) {
            int[] maxLengthAttr = new int[]{android.R.attr.maxLength};
            int indexOfAttrMaxLength = 0;
            TypedArray a = context.obtainStyledAttributes(attrs, maxLengthAttr);
            try {
                mMaxLength = a.getInteger(indexOfAttrMaxLength, -1);
            } catch (Exception e) {
                Log.e("EditTextEx", "Unable to load attributes");
            } finally {
                a.recycle();
            }
        }

        mHintColors = getHintTextColors();
        mTextColors = getTextColors();

        mWasEmpty = TextUtils.isEmpty(getText());

//        initPadding();

//        setBgDrawable(null);

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void setBgDrawable(Drawable d) {

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(d);
        }
        else {
            setBackground(d);
        }
    }

    @Override
    public int getCompoundPaddingTop() {
        final FontMetricsInt metrics = getPaint().getFontMetricsInt();
        final int floatingHintHeight = (int) ((metrics.bottom - metrics.top) * mHintScale);
        return super.getCompoundPaddingTop() + floatingHintHeight;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore,
                                 int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        final boolean isEmpty = TextUtils.isEmpty(getText());

        // The empty state hasn't changed, so the hint stays the same.
        if (mWasEmpty == isEmpty) {
            return;
        }

        mWasEmpty = isEmpty;

        // Don't animate if we aren't visible.
        if (!isShown()) {
            return;
        }

        if (isEmpty) {
            mAnimation = Animation.GROW;
            setHintTextColor(Color.TRANSPARENT);
        } else {
            mAnimation = Animation.SHRINK;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void initPadding() {
        FontMetricsInt fontMetrics = getPaint().getFontMetricsInt();

        int paddingBottom = (int) ((fontMetrics.descent - fontMetrics.ascent) * mHintScale) / 2;
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + paddingBottom + dp2px(getContext(), 2.0f));
    }

    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public static int dp2px(Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float normalHintSize = getTextSize();
        final float floatingHintSize = normalHintSize * mHintScale;

/*        Paint p = new Paint();

        p.setStrokeWidth(dp2px(getContext(), 2.0f));
        p.setColor(fetchAccentColor());

        int y = getHeight() - (int)floatingHintSize;
        canvas.drawLine(0, y, getWidth() - getCompoundPaddingRight(), y, p);

        if (mMaxLength > 0) {
            String t = String.format("%d/%d", getText().length(), mMaxLength);
            p.setColor(fetchAccentColor());
            p.setTextSize(getTextSize() * mHintScale);
            Rect textSize = new Rect();
            p.getTextBounds(t, 0, t.length(), textSize);

            canvas.drawText(t, getWidth() - (textSize.width() + getCompoundPaddingRight()), getHeight(), p);
        }*/

        if (TextUtils.isEmpty(getHint())) {
            return;
        }

        final boolean isAnimating = mAnimation != Animation.NONE;

        // The large hint is drawn by Android, so do nothing.
        if (!isAnimating && TextUtils.isEmpty(getText())) {
            return;
        }

        mFloatingHintPaint.set(getPaint());
        mFloatingHintPaint.setColor(
                mHintColors.getColorForState(getDrawableState(), mHintColors.getDefaultColor()));

        final float hintPosX = getCompoundPaddingLeft() + getScrollX();
        final float normalHintPosY = getBaseline();
        final float floatingHintPosY = normalHintPosY + getPaint().getFontMetricsInt().top + getScrollY();

        // If we're not animating, we're showing the floating hint, so draw it and bail.
        if (!isAnimating) {
            mFloatingHintPaint.setTextSize(floatingHintSize);
            canvas.drawText(getHint().toString(), hintPosX, floatingHintPosY, mFloatingHintPaint);
            return;
        }

        if (mAnimation == Animation.SHRINK) {
            drawAnimationFrame(canvas, normalHintSize, floatingHintSize,
                    hintPosX, normalHintPosY, floatingHintPosY);
        } else {
            drawAnimationFrame(canvas, floatingHintSize, normalHintSize,
                    hintPosX, floatingHintPosY, normalHintPosY);
        }

        mAnimationFrame++;

        if (mAnimationFrame == mAnimationSteps) {
            if (mAnimation == Animation.GROW) {
                setHintTextColor(mHintColors);
            }
            mAnimation = Animation.NONE;
            mAnimationFrame = 0;
        }

        invalidate();
    }

    private void drawAnimationFrame(Canvas canvas, float fromSize, float toSize,
                                    float hintPosX, float fromY, float toY) {
        final float textSize = lerp(fromSize, toSize);
        final float hintPosY = lerp(fromY, toY);
        mFloatingHintPaint.setTextSize(textSize);
        canvas.drawText(getHint().toString(), hintPosX, hintPosY, mFloatingHintPaint);
    }

    private float lerp(float from, float to) {
        final float alpha = (float) mAnimationFrame / (mAnimationSteps - 1);
        return from * (1 - alpha) + to * alpha;
    }
}