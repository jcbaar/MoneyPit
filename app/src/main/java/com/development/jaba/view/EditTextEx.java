package com.development.jaba.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.StyleableRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.UtilsHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * View class which contains an @{link EditText} adorned with a "number of characters" indication,
 * error indication, floating hints and validation possibilities.
 */
public class EditTextEx extends LinearLayout {

    private int mMaxLength;             // The maximum number of characters the EditText can contain.
    private int mErrorColor;            // The error color.
    private String mHintString;         // The hint string.
    @Bind(R.id.editEx_editor) EditText mEditor;           // The EditText.
    @Bind(R.id.editEx_wrapper) TextInputLayout mWrapper;
    private BaseValidator mValidator;   // Validator for this instance.

    private boolean mCounterEnabled = false; // Is the couter view enabled?

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

        // Inflate the layout of the custom view.
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_edittext_ex, this);

        // Get the views.
        ButterKnife.bind(this);

        // Re-generate an id for the EditText because when there are more than one
        // instances of this view in the same activity androids restore on orientation
        // changes will screw things up.
        mEditor.setId(UtilsHelper.generateViewId());

        @StyleableRes int inputType = 1;

        // Get the attributes.
        if (attrs != null) {
            int[] attrIntArray = new int[]{android.R.attr.maxLength, android.R.attr.inputType};
            int[] attrStrArray = new int[]{android.R.attr.hint};
            TypedArray a = context.obtainStyledAttributes(attrs, attrIntArray);
            TypedArray b = context.obtainStyledAttributes(attrs, attrStrArray);
            TypedArray c = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditTextEx, 0, 0);

            try {
                mMaxLength = a.getInteger(0, -1);
                mHintString = b.getString(0);
                mErrorColor = c.getColor(R.styleable.EditTextEx_eteErrorColor, Color.argb(200, 255, 0, 0));

                // Make sure we pass on the correct input type to the EditText.
                mEditor.setInputType(a.getInteger(inputType, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS ));
            } catch (Exception e) {
                Log.e("EditTextEx", "Unable to load attributes");
            } finally {
                a.recycle();
                b.recycle();
                c.recycle();
            }
        }

        // If we have a maximum number of characters the user can enter
        // we will setup a LengthFilter to enforce this.
        setMaxLength(mMaxLength);

        // Setup the views.
        mWrapper.setHint(mHintString);

        // Focus changes on the EditText are forwarded if the view has a
        // focus change listener. Als the current focus change listener of the EditText
        // is stored so that we can call it when focus changes happen. A focus change of
        // the EditText is also used to execute the validation (if present) and to see
        // if the error/character count line must be visible.
        final OnFocusChangeListener editorListener = mEditor.getOnFocusChangeListener();
        final View thisView = this;
        mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                OnFocusChangeListener listener = getOnFocusChangeListener();
                if (listener != null) {
                    listener.onFocusChange(thisView, hasFocus);
                }

                // Make sure we also call into the EditText focus change listener.
                if (editorListener != null) {
                    editorListener.onFocusChange(mEditor, hasFocus);
                }

                if (!hasFocus) {
                    validate();
                }
            }
        });
    }

    public void setMaxLength(int maxLength) {
        mMaxLength = maxLength;
        if(mMaxLength <= 0) {
            mMaxLength = 0;
            if(!mCounterEnabled) {
                mWrapper.setCounterEnabled(false);
                mCounterEnabled = true;
            }
        }
        else {
            mMaxLength = maxLength;
            if(mCounterEnabled) {
                mWrapper.setCounterEnabled(false);
                mCounterEnabled = false;
            }
        }
        mWrapper.setCounterMaxLength(mMaxLength);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(mMaxLength);
        mEditor.setFilters(filters);
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
        if(error != null) {
            mWrapper.setError(error);
            mWrapper.setErrorEnabled(true);
        }
        else
        {
            mWrapper.setErrorEnabled(false);
        }
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
        public final Context mContext;    // The context.

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
}
