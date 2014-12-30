package com.development.jaba.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
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
import com.development.jaba.utilities.UtilsHelper;

import java.util.Date;

/**
 * View class which contains an @{link EditText} adorned with a "number of characters" indication,
 * error indication, floating hints and validation possibilities.
 */
public class EditTextEx extends LinearLayout {

    private Animation mAnimateVisible,  // Animation for showing the hint indication.
            mAnimateGone,               // Animation for hiding the hint indication.
            mAnimateVisibleError,       // Animation for showing the error indication.
            mAnimateGoneError;          // Animation for hiding the error indication.
    private int mMaxLength,             // The maximum number of characters the EditText can contain.
            mAnimDuration;              // The animation duration.
    private int mErrorColor;            // The error color.
    private String mHintString;         // The hint string.
    private TextView mHint,             // The hint TextView.
            mError,                     // The error TextView.
            mCharCount;
    private EditText mEditor;           // The EditText.
    private LinearLayout mBottomInfo;   // The LinearLayout containing the error and character counter views.
    private BaseValidator mValidator;   // Validator for this instance.

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     */
    public EditTextEx(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     * @param attrs The attributes.
     */
    public EditTextEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     * @param attrs The attributes.
     * @param defStyleAttr The default style.
     */
    public EditTextEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Infalate the layout of the custom view.
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_edittext_ex, this);

        // Get the views.
        mHint = (TextView) findViewById(R.id.editEx_hint);
        mError = (TextView) findViewById(R.id.editEx_error);
        mCharCount = (TextView) findViewById(R.id.editEx_charCount);
        mEditor = (EditText) findViewById(R.id.editEx_editor);
        mBottomInfo = (LinearLayout) findViewById(R.id.editEx_bottomInfo);

        // Re-generate an id for the EditText because when there are more than one
        // instances of this view in the same activity androids restore on orientation
        // changes will screw things up.
        mEditor.setId(UtilsHelper.generateViewId());

        // Get the attributes.
        if (attrs != null) {
            int[] attrIntArray = new int[]{android.R.attr.maxLength};
            int[] attrStrArray = new int[]{android.R.attr.hint};
            TypedArray a = context.obtainStyledAttributes(attrs, attrIntArray);
            TypedArray b = context.obtainStyledAttributes(attrs, attrStrArray);
            TypedArray c = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextEx, 0, 0);

            try {
                mMaxLength = a.getInteger(0, -1);
                mHintString = b.getString(0);
                mAnimDuration = c.getInteger(R.styleable.EditTextEx_eteDuration, 400);
                mErrorColor = c.getColor(R.styleable.EditTextEx_eteErrorColor, Color.argb(200, 255, 0, 0));
            } catch (Exception e) {
                Log.e("EditTextEx", "Unable to load attributes");
            } finally {
                a.recycle();
                b.recycle();
                c.recycle();
            }
        }

        // Setup the animation listeners. These take care of making the views (in)visible
        // at the appropriate moments.
        MyAnimationListner hideHintListener = new MyAnimationListner(mHint, true, true);
        MyAnimationListner showHintListener = new MyAnimationListner(mHint, false, true);
        MyAnimationListner hideErrListener = new MyAnimationListner(mError, true, false);
        MyAnimationListner showErrListener = new MyAnimationListner(mError, false, false);

        // And the animations.
        mAnimateGone = new AlphaAnimation(1, 0);
        mAnimateGone.setDuration(mAnimDuration);
        mAnimateGone.setFillAfter(true);
        mAnimateGone.setAnimationListener(hideHintListener);

        mAnimateVisible = new AlphaAnimation(0, 1);
        mAnimateVisible.setDuration(mAnimDuration);
        mAnimateVisible.setFillAfter(true);
        mAnimateVisible.setAnimationListener(showHintListener);

        mAnimateGoneError = new AlphaAnimation(1, 0);
        mAnimateGoneError.setDuration(mAnimDuration);
        mAnimateGoneError.setFillAfter(true);
        mAnimateGoneError.setAnimationListener(hideErrListener);

        mAnimateVisibleError = new AlphaAnimation(0, 1);
        mAnimateVisibleError.setDuration(mAnimDuration);
        mAnimateVisibleError.setFillAfter(true);
        mAnimateGoneError.setAnimationListener(showErrListener);

        // Hide the hint by default.
        mHint.setVisibility(View.GONE);

        // If we have a maximum number of characters the user can enter
        // we will setup a LengthFilter to enforce this.
        if (mMaxLength > 0) {
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(mMaxLength);
            mEditor.setFilters(filters);
        }

        // Setup the views.
        mEditor.setHint(mHintString);
        mHint.setText(mHintString);
        mError.setText(null);
        mError.setTextColor(mErrorColor);

        mCharCount.setTextColor(getResources().getColor(R.color.accentColor));

        setCharCount();
        checkBottomLine();

        // Focus changes on the EditText are forwarded to if the view has a
        // focus change listener. A focus change of the EditText is also used to
        // execute the validation (if present) and to see if the error/character
        // count line must be visible.
        final View thisView = this;
        mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                OnFocusChangeListener listner = getOnFocusChangeListener();
                if (listner != null) {
                    listner.onFocusChange(thisView, hasFocus);
                }

                if (!hasFocus) {
                    checkBottomLine();
                    validate();
                }
                setCharCount();
            }
        });

        // We need to listen to text changes in the EditText. Changes in the text are used to show or
        // hide the floating hint TextView. It is also used to update the character counter text view.
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
                if (s.length() != 0 && !TextUtils.isEmpty(mHintString)) {
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

    /**
     * Checks to see whether or not the layout with the error and
     * character counter views must be visible.
     */
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

    /**
     * Updates the character counter view.
     */
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
            mCharCount.setTextColor(mEditor.hasFocus() ? getResources().getColor(R.color.accentColor) : mEditor.getTextColors().getDefaultColor());
        }
    }

    /**
     * Validates the text in the EditText.
     *
     * @return true if validation was successful or false if it was not.
     */
    public boolean validate() {
        if (mValidator != null) {
            if (!mValidator.isValid(mEditor.getText().toString())) {
                setError(mValidator.getErrorMessage());
                return false;
            } else {
                setError(null);
            }
        }
        return true;
    }

    /**
     * Setup the {@link com.development.jaba.view.EditTextEx.BaseValidator} derived validator used
     * to validate the text input.
     * @param validator The {@link com.development.jaba.view.EditTextEx.BaseValidator} derived validator.
     */
    public void setValidator(BaseValidator validator) {
        mValidator = validator;
    }

    /**
     * Sets the text in the {@link EditText}.
     *
     * @param text The text to set in the {@link EditText}.
     */
    public void setText(CharSequence text) {
        mEditor.setText(text);
    }

    /**
     * Sets the text in the {@link EditText}.
     *
     * @param resId The resource id of the text to set in the {@link EditText}.
     */
    public void setText(int resId) {
        mEditor.setText(resId);
    }

    /**
     * Gets the text from the {@link EditText}.
     *
     * @return The text from the {@link EditText}.
     */
    public Editable getText() {
        return mEditor.getText();
    }

    /**
     * Sets or clears the error.
     *
     * @param error The error message to show. null will clear the error message.
     */
    public void setError(CharSequence error) {
        mError.setText(error);
        mError.startAnimation(TextUtils.isEmpty(error) ? mAnimateGoneError : mAnimateVisibleError);
    }

    /**
     * Gets the {@link EditText} view.
     *
     * @return The {@link EditText} view.
     */
    public EditText getEditor() {
        return mEditor;
    }

    /**
     * Abstract class which serves as a base class for validators.
     */
    public static abstract class BaseValidator {

        public String mErrorText;   // The validation error text.
        public Context mContext;    // The context.

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         */
        public BaseValidator(Context context) {
            mContext = context;
        }

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         * @param msgResId The resource ID of the error message.
         */
        public BaseValidator(Context context, int msgResId) {
            mContext = context;
            mErrorText = context.getResources().getString(msgResId);
        }

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         * @param msg The error message.
         */
        public BaseValidator(Context context, String msg) {
            mContext = context;
            mErrorText = msg;
        }

        /**
         * This method needs to be implemented in the derived validator class.
         * It should return true if the validation was successful. It should setup the
         * proper error message (if necessary) and return false if the validation failed.
         *
         * @param value The value to validate.
         * @return True for successful validation. false for failed validation.
         */
        public abstract boolean isValid(String value);

        /**
         * Gets the validation error message.
         *
         * @return The validation error message.
         */
        public String getErrorMessage() {
            return mErrorText;
        }

        /**
         * Sets the validation error message.
         *
         * @param msg The validation error message.
         */
        public void setErrorMessage(String msg) {
            mErrorText = msg;
        }

        /**
         * Sets the validation error message.
         *
         * @param msgResId The validation message resource id.
         */
        public void setErrorMessage(int msgResId) {
            mErrorText = mContext.getResources().getString(msgResId);
        }
    }

    /**
     * Required field validator. Checks if there is any text.
     */
    public static class RequiredValidator extends BaseValidator {

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         */
        public RequiredValidator(Context context) {
            super(context, context.getResources().getString(R.string.no_text_error));
        }

        /**
         * Checks if the value contains any text.
         *
         * @param value The value to validate.
         * @return true for success, false for failure.
         */
        @Override
        public boolean isValid(String value) {
            return !TextUtils.isEmpty(value.trim());
        }
    }

    /**
     * Build year validator. Checks if the value is between 1672 and the current year.
     */
    public static class BuildYearValidator extends BaseValidator {
        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         */
        public BuildYearValidator (Context context) {
            super(context);
        }

        /**
         * Checks if the value is between 1672 and the current year.
         *
         * @param value The value to validate.
         * @return true for success, false for failure.
         */
        @Override
        public boolean isValid(String value) {
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

    /**
     * AnimationListener derived class which knows the view it is linked to.
     */
    private static class MyAnimationListner implements Animation.AnimationListener {

        private View mView;     // The view the animation is linked to.
        private boolean mHide,  // Hide animation?
                mShowHideView;  // Change view visibility?

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param holder The {@link View} the animation is controlling.
         * @param hide true for a hiding animation, false for a show animation.
         * @param showHide true to change the view visibility before/after the animation.
         */
        public MyAnimationListner(View holder, boolean hide, boolean showHide) {
            mView = holder;
            mHide = hide;
            mShowHideView = showHide;
        }

        /**
         * Shows the view if this is a show animation and we must change the
         * {@link View} visibility.
         *
         * @param animation The {@link Animation}.
         */
        @Override
        public void onAnimationStart(Animation animation) {
            if(!mHide && mShowHideView) {
                mView.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Hides the view if this is a hide animation and we must change the
         * {@link View} visibility.
         *
         * @param animation The {@link Animation}.
         */
        @Override
        public void onAnimationEnd(Animation animation) {
            if(mHide && mShowHideView) {
                mView.setVisibility(View.GONE);
            }
        }

        /**
         * Not used.
         *
         * @param animation The {@link Animation}.
         */
        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
