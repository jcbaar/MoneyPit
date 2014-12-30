package com.development.jaba.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.development.jaba.database.Utils;
import com.development.jaba.moneypit.R;

import java.util.Date;

public class EditTextEx extends LinearLayout {

    private Animation mAnimateVisible, mAnimateGone, mAnimateVisibleError, mAnimateGoneError;

    private int mMaxLength;
    private String mHintString;

    private TextView mHint, mError, mCharCount;
    private EditText mEditor;
    private LinearLayout mBottomInfo;

    private BaseValidator mValidator;

    public EditTextEx(Context context) {
        this(context, null, 0);
    }

    public EditTextEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_edittext_ex, this);

        Animation.AnimationListener hideHintListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHint.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        Animation.AnimationListener showHintListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mHint.setTextColor(mEditor.getHintTextColors());
                mHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        mAnimateGone = new AlphaAnimation(1, 0);
        mAnimateGone.setDuration(400);
        mAnimateGone.setFillAfter(true);
        mAnimateGone.setAnimationListener(hideHintListener);

        mAnimateVisible = new AlphaAnimation(0, 1);
        mAnimateVisible.setDuration(400);
        mAnimateVisible.setFillAfter(true);
        mAnimateVisible.setAnimationListener(showHintListener);

        mAnimateGoneError = new AlphaAnimation(1, 0);
        mAnimateGoneError.setDuration(400);
        mAnimateGoneError.setFillAfter(true);

        mAnimateVisibleError = new AlphaAnimation(0, 1);
        mAnimateVisibleError.setDuration(400);
        mAnimateVisibleError.setFillAfter(true);

        if (attrs != null) {
            int[] attrIntArray = new int[]{android.R.attr.maxLength};
            int[] attrStrArray = new int[]{android.R.attr.hint};
            TypedArray a = context.obtainStyledAttributes(attrs, attrIntArray);
            TypedArray b = context.obtainStyledAttributes(attrs, attrStrArray);
            try {
                mMaxLength = a.getInteger(0, -1);
                mHintString = b.getString(0);
            } catch (Exception e) {
                Log.e("EditTextEx", "Unable to load attributes");
            } finally {
                a.recycle();
                b.recycle();
            }
        }

        mHint = (TextView) findViewById(R.id.editEx_hint);
        mError = (TextView) findViewById(R.id.editEx_error);
        mCharCount = (TextView) findViewById(R.id.editEx_charCount);
        mEditor = (EditText) findViewById(R.id.editEx_editor);
        mBottomInfo = (LinearLayout) findViewById(R.id.editEx_bottomInfo);

        mHint.setVisibility(View.GONE);

        if (mMaxLength > 0) {
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(mMaxLength);
            mEditor.setFilters(filters);
        }
        mEditor.setHint(mHintString);
        mHint.setText(mHintString);
        mError.setText(null);
        mError.setTextColor(getResources().getColor(R.color.errorColor));
        mCharCount.setTextColor(getResources().getColor(R.color.accentColor));

        setCharCount();
        checkBottomLine();

        final View thisView = this;
        mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                OnFocusChangeListener listner = getOnFocusChangeListener();
                if (listner != null) {
                    listner.onFocusChange(thisView, hasFocus);
                }

                if (!hasFocus) {
                    validate();
                    checkBottomLine();
                }
            }
        });

        mEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int visibility = View.GONE;
                if (s.length() != 0 && TextUtils.isEmpty(mHintString) == false) {
                    visibility = View.VISIBLE;
                }

                if (mHint.getVisibility() != visibility) {
                    if (visibility == View.GONE) {
                        mHint.startAnimation(mAnimateGone);
                    } else {
                        mHint.startAnimation(mAnimateVisible);
                    }
                }
                setCharCount();
            }
        });
    }

    private void checkBottomLine() {
        if(TextUtils.isEmpty(mError.getText()) && mMaxLength <= 0) {
            if (mBottomInfo.getVisibility() != View.GONE) {
                mBottomInfo.setVisibility(View.GONE);
            }
        }
        else {
            if (mBottomInfo.getVisibility() != View.VISIBLE) {
                mBottomInfo.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setCharCount() {
        if(mMaxLength <= 0 ) {
            if(mCharCount.getVisibility() != View.GONE) {
                mCharCount.setVisibility(View.GONE);
            }
        }
        else {
            if(mCharCount.getVisibility() != View.VISIBLE) {
                mCharCount.setVisibility(View.VISIBLE);
            }
            mCharCount.setText(String.format("%d/%d", mEditor.getText().length(), mMaxLength));
        }
    }

    public boolean validate() {
        if (mValidator != null) {
            if (mValidator.isValid(mEditor.getText().toString()) == false) {
                setError(mValidator.getErrorMessage());
                return false;
            } else {
                setError(null);
            }
        }
        return true;
    }

    public void setValidator(BaseValidator validator) {
        mValidator = validator;
    }

    public void setText(char[] text, int start, int len) {
        mEditor.setText(text, start, len);
    }

    public void setText(CharSequence text, TextView.BufferType type) {
        mEditor.setText(text, type);
    }

    public void setText(CharSequence text) {
        mEditor.setText(text);
    }

    public void setText(int resId) {
        mEditor.setText(resId);
    }

    public void setText(int resId, TextView.BufferType type) {
        mEditor.setText(resId, type);
    }

    public Editable getText() {
        return mEditor.getText();
    }

    public void setError(CharSequence error) {
        mError.setText(error);
        mError.startAnimation(TextUtils.isEmpty(error) ? mAnimateGoneError : mAnimateVisibleError);
    }

    public void setError(CharSequence error, Drawable icon) {
        mError.setText(error);
        mError.startAnimation(TextUtils.isEmpty(error) ? mAnimateGoneError : mAnimateVisibleError);
    }

    public EditText getEditor() {
        return mEditor;
    }

    public static abstract class BaseValidator {
        public String mErrorText;
        public Context mContext;

        public BaseValidator(Context context) {
            mContext = context;
        }

        public BaseValidator(Context context, int msgResId) {
            mContext = context;
            mErrorText = context.getResources().getString(msgResId);
        }

        public BaseValidator(Context context, String msg) {
            mContext = context;
            mErrorText = msg;
        }

        public abstract boolean isValid(String value);

        public String getErrorMessage() {
            return mErrorText;
        }

        public void setErrorMessage(String msg) {
            mErrorText = msg;
        }

        public void setErrorMessage(int msgResId) {
            mErrorText = mContext.getResources().getString(msgResId);
        }
    }

    public static class RequiredValidator extends BaseValidator {

        public RequiredValidator(Context context) {
            super(context, context.getResources().getString(R.string.no_text_error));
        }

        @Override
        public boolean isValid(String value) {
            return !TextUtils.isEmpty(value);
        }
    }

    public static class BuildYearValidator extends BaseValidator {
        public BuildYearValidator (Context context) {
            super(context);
        }

        @Override
        public boolean isValid(String value) {
            Resources res = mContext.getResources();
            try {
                int year = Integer.parseInt(value);
                if (year < 1672) {
                    setErrorMessage(R.string.buildyear_to_low);
                    return false;
                } else if (year > Utils.getYearFromDate(new Date())) {
                    setErrorMessage(R.string.buildyear_to_high);
                    return false;
                }
            }
            catch(NumberFormatException ex) {
                setErrorMessage(R.string.buildyear_error);
                return false;
            }
            setErrorMessage(null);
            return true;
        }
    }
}
