package com.development.jaba.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class for reading and writing preferences.
 */
public class SettingsHelper {
    /**
     * Preferences keys.
     */
    public final static String PREF_ESTIMATE_ODOMETER = "estimate_odometer",
            PREF_IMPORT_EXPORT_PICTURES = "export_import_pictures",
            PREF_ALLOW_LOCATION = "allow_location",
            PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * {@link SharedPreferences} reference.
     */
    private final SharedPreferences mPreferences;

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context under which the object is instantiated.
     */
    public SettingsHelper(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Gets the PREF_ESTIMATE_ODOMETER setting.
     *
     * @return The value of the PREF_ESTIMATE_ODOMETER setting.
     */
    public boolean getEstimateOdometer() {
        return mPreferences.getBoolean(PREF_ESTIMATE_ODOMETER, true);
    }

    /**
     * Gets the PREF_IMPORT_EXPORT_PICTURES setting.
     *
     * @return The value of the PREF_IMPORT_EXPORT_PICTURES setting.
     */
    public boolean getExportImportPictures() {
        return mPreferences.getBoolean(PREF_IMPORT_EXPORT_PICTURES, true);
    }

    /**
     * Gets the PREF_USER_LEARNED_DRAWER settings.
     *
     * @return The value of the PREF_USER_LEARNED_DRAWER setting.
     */
    public boolean getUserLearnedDrawer() {
        return mPreferences.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    /**
     * Sets the value of the PREF_USER_LEARNED_DRAWER setting.
     *
     * @param value The value the PREF_USER_LEARNED_DRAWER setting must be set to.
     */
    public void setUserLearnedDrawer(boolean value) {
        mPreferences.edit().putBoolean(PREF_USER_LEARNED_DRAWER, value).apply();
    }

    /**
     * Gets the PREF_ALLOW_LOCATION settings.
     *
     * @return The value of the PREF_ALLOW_LOCATION setting.
     */
    public boolean getAllowLocation() {
        return mPreferences.getBoolean(PREF_ALLOW_LOCATION, false);
    }

    /**
     * Gets the given integer value from the preferences.
     *
     * @param key          The key under which the value is stored.
     * @param defaultValue The default value if it is not present in the preferences.
     * @return The integer value or it's default if not set yet..
     */
    public int getIntegerValue(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    /**
     * Saves the given integer value to the preferences.
     *
     * @param key   The key under which the value is saved.
     * @param value The integer value to save to the preferences.
     */
    public void setIntegerValue(String key, int value) {
        mPreferences.edit().putInt(key, value).apply();
    }
}
