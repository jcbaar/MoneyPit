package com.development.jaba.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class for reading and writing preferences.
 */
public class SettingsHelper {
    private static final String TAG = "SettingsHelper";

    /**
     * Preferences keys.
     */
    public final static String PREF_ESTIMATE_ODOMETER = "estimate_odometer",
                               PREF_IMPORT_EXPORT_PICTURES = "export_import_pictures",
                               PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * {@link SharedPreferences} reference.
     */
    private final SharedPreferences mPreferences;

    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context under which the object is instantiated.
     */
    public SettingsHelper(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Gets the PREF_ESTIMATE_ODOMETER setting.
     * @return The value of the PREF_ESTIMATE_ODOMETER setting.
     */
    public boolean getEstimateOdometer() {
        return mPreferences.getBoolean(PREF_ESTIMATE_ODOMETER, true);
    }

    /**
     * Gets the PREF_IMPORT_EXPORT_PICTURES setting.
     * @return The value of the PREF_IMPORT_EXPORT_PICTURES setting.
     */
    public boolean getExportImportPictures() {
        return mPreferences.getBoolean(PREF_IMPORT_EXPORT_PICTURES, true);
    }

    /**
     * Gets the PREF_USER_LEARNED_DRAWER settings.
     * @return The value of the PREF_USER_LEARNED_DRAWER setting.
     */
    public boolean getUserLearnedDrawer() {
        return mPreferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    /**
     * Sets the value of the PREF_USER_LEARNED_DRAWER setting.
     * @param value The value the PREF_USER_LEARNED_DRAWER setting must be set to.
     */
    public void setUserLearnedDrawer(boolean value) {
        mPreferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, value).apply();
    }
}
