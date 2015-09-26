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
            PREF_ALLOW_LOCATION = "allow_location",
            PREF_THEME = "selected_theme";

    public final static String THEME_LIGHT = "light",
            THEME_DARK = "dark";

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

        // Setup defaults when they do not yet exist.
        if (!mPreferences.contains(PREF_ESTIMATE_ODOMETER)) {
            mPreferences.edit().putBoolean(PREF_ESTIMATE_ODOMETER, true).apply();
        }
        if (!mPreferences.contains(PREF_ALLOW_LOCATION)) {
            mPreferences.edit().putBoolean(PREF_ALLOW_LOCATION, false).apply();
        }
        if (!mPreferences.contains(PREF_THEME)) {
            mPreferences.edit().putString(PREF_THEME, THEME_LIGHT).apply();
        }
    }

    /**
     * Gets the {@link SharedPreferences} instance.
     * @return The {@link SharedPreferences} instance.
     */
    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }

    /**
     * Gets the given boolean value from the preferences.
     *
     * @param key          The key under which the value is stored.
     * @param defaultValue The default value if it is not present in the preferences.
     * @return The boolean value or it's default if not set yet..
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, false);
    }

    /**
     * Saves the given boolean value to the preferences.
     *
     * @param key   The key under which the value is saved.
     * @param value The boolean value to save to the preferences.
     */
    public void setBooleanValue(String key, boolean value) {
        mPreferences.edit().putBoolean(key, value).apply();
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

    /**
     * Gets the given string value from the preferences.
     *
     * @param key          The key under which the value is stored.
     * @param defaultValue The default value if it is not present in the preferences.
     * @return The string value or it's default if not set yet..
     */
    public String getStringValue(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    /**
     * Saves the given string value to the preferences.
     *
     * @param key   The key under which the value is saved.
     * @param value The String value to save to the preferences.
     */
    public void setStringValue(String key, String value) {
        mPreferences.edit().putString(key, value).apply();
    }
}
