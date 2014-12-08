package com.development.jaba.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

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
    public static void showMessageDialog(String title, String message, Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message)
                          .setCancelable(false)
                          .setPositiveButton(context.getString(R.string.dialog_close),
                                             new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    dialog.cancel();
                                                }
                          });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
