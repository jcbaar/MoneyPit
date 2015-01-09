package com.development.jaba.utilities;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.moneypit.R;

import java.util.Date;

/**
 * Helper functions related to dialogs.
 */
public class DialogHelper {

    /**
     * Generic message dialog with a single close button.
     * @param title The dialog title.
     * @param message The message to display in the dialog.
     * @param context The context which is opening the dialog.
     */
    public static void showMessageDialog(CharSequence title, CharSequence message, Context context) {

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);

        dialogBuilder.title(title);
        dialogBuilder.content(message);
        dialogBuilder.cancelable(false);
        dialogBuilder.positiveText(context.getString(R.string.dialog_close));
        dialogBuilder.show();
    }

    /**
     * Generic question dialog with a yes and a no button.
     * @param title The dialog title.
     * @param message The message to display in the dialog.
     * @param callback The callback to listen to dialog button clicks.
     * @param context The context which is opening the dialog.
     */
    public static void showYesNoDialog(CharSequence title, CharSequence message, MaterialDialog.ButtonCallback callback, Context context) {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);

        dialogBuilder.content(message);
        dialogBuilder.title(title);
        dialogBuilder.positiveText(context.getString(R.string.dialog_yes));
        dialogBuilder.negativeText(context.getString(R.string.dialog_no));
        dialogBuilder.callback(callback);
        dialogBuilder.show();
    }

    /**
     * Opens up the year selector for the given car. It will enable the user to select a year
     * in the range of the oldest year the database contains data of the car and the current year.
     *
     * @param car The {@link Car} entiry to show the year range for.
     * @param current The currently selected year. This is pre-selected in the spinner.
     * @param callback The {@link com.afollestad.materialdialogs.MaterialDialog.ButtonCallback} callback to handle dialog button clicks.
     * @param context The {@link android.content.Context}.
     */
    public static void showYearSelectionDialog(Car car, int current, MaterialDialog.ButtonCallback callback, Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.dialog_select_year_title)
                .customView(R.layout.dialog_select_year, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(callback)
                .build();

        // Get the spinner of the dialog view set it up.
        View view = dialog.getCustomView();
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);

        // We get the oldest year of data the car has from the database and
        // load the spinner with this year upto the current year.
        MoneyPitDbContext db = new MoneyPitDbContext(context);
        Integer start = db.getOldestDataYear(car.getId()),
                end = DateHelper.getYearFromDate(new Date());
        Integer[] years = new Integer[end - start + 1];

        int toSelect = 0;
        for( int  i = 0; i < years.length; i++) {
            years[i] = start + i;

            // Mark the current item so we know which one
            // to pre-select.
            if (years[i] == current) {
                toSelect = i;
            }
        }

        // Setup the data for the spinner.
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(context, R.layout.spinner_row_template, years);
        spinner.setAdapter(yearAdapter);
        spinner.setSelection(toSelect);

        dialog.show();
    }
}
