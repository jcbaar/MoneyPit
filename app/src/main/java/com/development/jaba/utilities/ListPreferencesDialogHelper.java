package com.development.jaba.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * A {@link ListPreference} derived class to show a correctly styled
 * single choice dialog.
 *
 * @@see <a href="http://stackoverflow.com/questions/14398483/how-can-i-change-the-appearance-of-listpreference-dialog">StackOverflow answer</a>
 */
public class ListPreferencesDialogHelper extends ListPreference {

    private MaterialDialog.Builder mBuilder;
    private Context context;

    public ListPreferencesDialogHelper(Context context) {
        super(context);
        this.context = context;
    }

    public ListPreferencesDialogHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    /**
     * Shows the dialog using the material
     *
     * @param state The state bundle.
     */
    @Override
    protected void showDialog(Bundle state) {
        mBuilder = new MaterialDialog.Builder(context);
        mBuilder.title(getTitle());
        mBuilder.icon(getDialogIcon());
        mBuilder.negativeText(getNegativeButtonText());
        mBuilder.items(getEntries());
        mBuilder.itemsCallbackSingleChoice(findIndexOfValue(getValue()), new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                onClick(null, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
                CharSequence[] values = getEntryValues();
                if (which >= 0 && values != null) {
                    String value = values[which].toString();
                    if (callChangeListener(value)) {
                        setValue(value);
                        return true;
                    }
                }
                return false;
            }
        });

        final View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            mBuilder.customView(contentView, false);
        } else
            mBuilder.content(getDialogMessage());

        mBuilder.show();
    }
}
