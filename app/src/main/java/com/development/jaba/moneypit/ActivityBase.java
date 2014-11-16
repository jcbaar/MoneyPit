package com.development.jaba.moneypit;

import android.app.Activity;
import android.widget.TextView;

import com.development.jaba.database.Utils;

import java.util.Date;

public class ActivityBase extends Activity {

    protected boolean validateTextViewBuildYear(TextView textView) {
        try {
            int year = Integer.parseInt(textView.getText().toString());
            if (year < 1672) {
                textView.setError(getResources().getString(R.string.buildyear_to_low));
                return false;
            } else if (year > Utils.getYearFromDate(new Date())) {
                textView.setError(getResources().getString(R.string.buildyear_to_high));
                return false;
            }
        }
        catch(NumberFormatException ex){
            textView.setError(getResources().getString(R.string.buildyear_error));
            return false;
        }
        textView.setError(null);
        return true;
    }

    protected boolean validateTextViewManditory(TextView textView) {
        String text = textView.getText().toString();
        if(text.trim().length() == 0) {
            textView.setError(getResources().getString(R.string.no_text_error));
            return false;
        }
        textView.setError(null);
        return true;
    }
}
