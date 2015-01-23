package com.development.jaba.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DialogHelper;

import java.io.File;

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

        Preference pref = findPreference("emptyMapCache");
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DialogHelper.showYesNoDialog(getString(R.string.warning), getString(R.string.empty_cache), new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);

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
                            },
                            getActivity());
                    return true;
                }
            });
        }
    }
}
