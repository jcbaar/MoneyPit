package com.development.jaba.moneypit;

import android.os.Bundle;

import com.development.jaba.fragments.SettingsFragment;

/**
 * Simple settings activity. Loads up the {@link com.development.jaba.fragments.SettingsFragment}.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_settings;
    }
}
