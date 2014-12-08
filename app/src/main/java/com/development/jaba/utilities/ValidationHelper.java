package com.development.jaba.utilities;

import android.content.res.Resources;
import android.widget.TextView;

import com.development.jaba.database.Utils;
import com.development.jaba.moneypit.MoneyPitApp;
import com.development.jaba.moneypit.R;

import java.util.Date;

/**
 * Helper class containing input validation methods.
 */
public class ValidationHelper {
    /**
     * Validates the build year of a car. Rules are that the year can not be
     * before 1672 and not beyond the current year.
     * @param textView The TextView that contains the value to validate.
     * @return true for a successful validation, false for a failed validation.
     */
    public static boolean validateTextViewBuildYear(TextView textView) {
        Resources res = MoneyPitApp.getContext().getResources();
        try {
            int year = Integer.parseInt(textView.getText().toString());
            if (year < 1672) {
                textView.setError(res.getString(R.string.buildyear_to_low));
                return false;
            } else if (year > Utils.getYearFromDate(new Date())) {
                textView.setError(res.getString(R.string.buildyear_to_high));
                return false;
            }
        }
        catch(NumberFormatException ex){
            textView.setError(res.getString(R.string.buildyear_error));
            return false;
        }
        textView.setError(null);
        return true;
    }

    /**
     * Validates that there is actually text entered.
     * @param textView The TextView that contains the value to validate.
     * @return true for a successful validation, false for a failed validation.
     */
    public static boolean validateTextViewMandatory(TextView textView) {
        Resources res = MoneyPitApp.getContext().getResources();
        String text = textView.getText().toString();
        if(text.trim().length() == 0) {
            textView.setError(res.getString(R.string.no_text_error));
            return false;
        }
        textView.setError(null);
        return true;
    }
}
