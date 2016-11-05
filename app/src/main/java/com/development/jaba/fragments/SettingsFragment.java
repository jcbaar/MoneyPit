package com.development.jaba.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.moneypit.BaseActivity;
import com.development.jaba.moneypit.MoneyPitApp;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DialogHelper;

import java.io.File;

/**
 * A simple fragment for editing the application settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    /**
     * Static factory method. Creates a new instance of this fragment.
     * @return The created SettingsFragment.
     */
    @SuppressWarnings("unused")
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    /**
     * Constructor. Initializes an instance of the object.
     */
    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        Preference pref = findPreference("emptyMapCache");

        if (pref != null) {
            if ( ContextCompat.checkSelfPermission(MoneyPitApp.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                pref.setEnabled(false);
            }
            else {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        DialogHelper.showYesNoDialog(getString(R.string.warning),
                                getString(R.string.empty_cache),
                                new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        if (dialogAction == DialogAction.POSITIVE) {
                                            File cache = new File(Environment.getExternalStorageDirectory() + "/MoneyPit/mapcache/");
                                            String[] files = cache.list();
                                            boolean ok = true;
                                            for (String f : files) {
                                                File file = new File(cache.getAbsolutePath() + "/" + f);
                                                ok &= file.delete();
                                            }

                                            if (ok) {
                                                Toast.makeText(getActivity(), getString(R.string.cache_purged), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.cache_purge_failed), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                },
                                getActivity());
                        return true;
                    }
                });
            }

            // This is a pity... The MaterialDialogs library does support a bunch of preference dialogs
            // but they are not derived from android.support.v7.preference.Preference. This will not
            // work properly with support preferences so we need to do this ourselves.
            //
            // The reason for not using the "normal" ListPreference is that it does not theme properly.
            // The buttons will not used the theme accent colors.
            pref = findPreference("selected_theme");
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        DialogHelper.showThemePreferencesDialog((BaseActivity)getActivity());
                        return false;
                    }
                });
            }
        }
    }
}
