package com.development.jaba.moneypit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.development.jaba.utilities.SettingsHelper;

/**
 * A simple {@link AppCompatActivity} derived class that serves as a base
 * class for the activities. It handles some generic things all activities share like the
 * {@link android.support.v7.widget.Toolbar}.
 */
public abstract class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Toolbar mToolbar;
    private SettingsHelper mSettings;
    private boolean mThemeChanged = false;
    private String mCurrentTheme = SettingsHelper.THEME_LIGHT;

    protected void onCreate(Bundle savedInstanceState) {
        mSettings = new SettingsHelper(this);
        mSettings.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        mCurrentTheme = mSettings.getStringValue(SettingsHelper.PREF_THEME, SettingsHelper.THEME_LIGHT);
        switch (mCurrentTheme) {
            case SettingsHelper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar ab = getSupportActionBar();
            if(ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setHomeButtonEnabled(true);
            }
        }

        // On Lollipop and and higher we want a "transparent" status bar for the
        // navigation drawer. On the other activities we set the primaryColorDark.
        // This is an ugly solution but for now it works...
        if (this.getClass() != MainDrawerActivity.class &&
                Build.VERSION.SDK_INT >= 21) {
            if (mCurrentTheme.equals(SettingsHelper.THEME_LIGHT)) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.primaryColorDark));
            } else {
                getWindow().setStatusBarColor(getResources().getColor(R.color.primaryColorDark_Dark));
            }
        }
    }

    /**
     * Sub-classes must aoverride this to supply the correct layout ID.
     *
     * @return The layout ID to inflate as content view.
     */
    protected abstract int getLayoutResource();

    /**
     * Gets a reference to the {@link android.support.v7.widget.Toolbar} used as AppBar.
     *
     * @return The {@link android.support.v7.widget.Toolbar}.
     */
    protected Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * Callback the is called when the user has selected an item from the options
     * menu.
     *
     * @param item The item the user selected.
     * @return true when the item selection was handled, false when not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(this);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
            } else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the {@link SettingsHelper} instance for the activity.
     *
     * @return The {@link SettingsHelper} instance.
     */
    public SettingsHelper getSettings() {
        return mSettings;
    }

    /**
     * When resumed we check to see whether or not we are marked for
     * reload (theme changed).
     */
    @Override
    protected void onStart() {
        if (mThemeChanged) {
            reloadActivity();
        }
        super.onStart();
    }

    /**
     * When resumed we check to see whether or not we are marked for
     * reload (theme changed).
     */
    @Override
    protected void onResume() {
        if (mThemeChanged) {
            reloadActivity();
        }
        super.onResume();
    }

    /**
     * Reloads the current activity (used for theme changes).
     */
    private void reloadActivity() {
        Class<?> cls = getClass();
        finish();
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Listens for changes on the theme settings. When the theme is changed we mark the activity
     * for reloading when started or resumed. If the current activity is the {@link SettingsActivity}
     * we reload it immediately.
     *
     * @param sharedPreferences The {@link SharedPreferences} instance.
     * @param key               The key of the changed setting.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Was it the theme that changed?
        if (key.equals(SettingsHelper.PREF_THEME)) {
            // Paranoia? Make sure the setting actually did change.
            String theme = mSettings.getStringValue(SettingsHelper.PREF_THEME, SettingsHelper.THEME_LIGHT);
            if (!theme.equals(mCurrentTheme)) {
                // If this is the SettingsActivity we reload now. Otherwise we
                // mark the activity for realod after resume or start.
                if (getClass() == SettingsActivity.class) {
                    reloadActivity();
                } else {
                    mThemeChanged = true;
                }
            }
        }
    }
}
