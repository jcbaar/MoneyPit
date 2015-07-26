package com.development.jaba.moneypit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * A simple {@link AppCompatActivity} derived class that serves as a base
 * class for the activities. It handles some generic things all activities share like the
 * {@link android.support.v7.widget.Toolbar}.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    /**
     * Sets up the {@link android.support.v7.widget.Toolbar} with the ID R.id.app_bar
     * as the AppBar.
     * <p/>
     * It uses {@see com.development.jaba.moneypit.BaseActivity#getLayoutResource} to know which
     * layout to use as content view.
     *
     * @param savedInstanceState Previously saved values.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
}
