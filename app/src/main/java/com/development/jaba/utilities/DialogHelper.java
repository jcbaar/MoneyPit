package com.development.jaba.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.moneypit.BaseActivity;
import com.development.jaba.moneypit.R;

/**
 * Helper functions related to dialogs.
 */
public class DialogHelper {

    /**
     * Generic message dialog with a single close button.
     *
     * @param title   The dialog title.
     * @param message The message to display in the dialog.
     * @param context The context which is opening the dialog.
     */
    public static void showMessageDialog(CharSequence title, CharSequence message, Context context) {

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);
        dialogBuilder
                .title(title)
                .content(message)
                .cancelable(false)
                .positiveText(context.getString(R.string.dialog_close))
                .show();
    }

    /**
     * Generic message dialog with a single close button.
     *
     * @param title   The dialog title.
     * @param message The message to display in the dialog.
     * @param callback The callback to listen to dialog button clicks.
     * @param context The context which is opening the dialog.
     */
    public static void showCallbackMessageDialog(CharSequence title, CharSequence message, MaterialDialog.SingleButtonCallback callback, Context context) {

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);
        dialogBuilder
                .title(title)
                .content(message)
                .cancelable(false)
                .positiveText(context.getString(R.string.dialog_close))
                .onAny(callback)
                .show();
    }

    /**
     * Generic question dialog with a yes and a no button.
     *
     * @param title    The dialog title.
     * @param message  The message to display in the dialog.
     * @param callback The callback to listen to dialog button clicks.
     * @param context  The context which is opening the dialog.
     */
    public static void showYesNoDialog(CharSequence title, CharSequence message, MaterialDialog.SingleButtonCallback callback, Context context) {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);
        dialogBuilder
                .content(message)
                .title(title)
                .positiveText(context.getString(R.string.dialog_yes))
                .negativeText(context.getString(R.string.dialog_no))
                .onAny(callback)
                .show();
    }

    /**
     * Special dialog for selecting the theme preference of the app.
     * @param context The {@link BaseActivity} containing the preferences.
     */
    public static void showThemePreferencesDialog(final BaseActivity context)
    {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context);
        dialogBuilder
                .items(R.array.theme)
                .title(R.string.settings_theme)
                .itemsCallbackSingleChoice(context.getSettings().getIntegerValue(SettingsHelper.PREF_THEME, SettingsHelper.THEME_LIGHT),
                        new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                materialDialog.hide();
                                context.getSettings().setIntegerValue(SettingsHelper.PREF_THEME, i);
                                return true;
                            }
                })
                .alwaysCallSingleChoiceCallback()
                .negativeText(context.getString(R.string.cancel))
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.hide();
                    }
                })
                .show();
    }
}
