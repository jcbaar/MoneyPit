package com.development.jaba.moneypit;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * A simple {@link android.support.v7.app.ActionBarActivity} derived class that serves as a base
 * class for the activities. It handles some generic things all activities share like the
 * {@link android.support.v7.widget.Toolbar}.
 */
public abstract class BaseActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    /**
     * Sets up the {@link android.support.v7.widget.Toolbar} with the ID R.id.app_bar
     * as the AppBar.
     *
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Sub-classes must aoverride this to supply the correct layout ID.
     * @return The layout ID to inflate as content view.
     */
    protected abstract int getLayoutResource();

    /**
     * Gets a reference to the {@link android.support.v7.widget.Toolbar} used as AppBar.
     * @return The {@link android.support.v7.widget.Toolbar}.
     */
    protected Toolbar getToolbar() {
        return mToolbar;
    }
}
