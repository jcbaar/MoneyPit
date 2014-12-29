package com.development.jaba.utilities;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.moneypit.R;

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
}
