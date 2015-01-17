package com.development.jaba.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.development.jaba.moneypit.R;

/**
 * A simple fragment for editing the application settings.
 */
public class SettingsFragment extends PreferenceFragment {

    /**
     * Static factory method. Creates a new instance of this fragment.
     * @return The created SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    /**
     * Constructor. Initializes an instance of the object.
     */
    public SettingsFragment() {
    }

    /**
     * Called when the fragment is created. Here the preferences
     * screen from the resources is added to the fragment.
     * @param savedInstanceState Saved instance data.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
