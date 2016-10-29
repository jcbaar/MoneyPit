package com.development.jaba.moneypit;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.development.jaba.fragments.CarListFragment;
import com.development.jaba.fragments.DriveBackupFragment;
import com.development.jaba.utilities.UtilsHelper;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainDrawerActivity extends BaseActivity {

    @Bind(R.id.drawer_layout) DrawerLayout mDrawer;
    @Bind(R.id.navigation_drawer) NavigationView mDrawerView;

    private ActionBarDrawerToggle mToggle;
    private int mCheckedId = -1;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // Storage keys for activity data.
    private final static String CHECKED_ITEM = "mCheckedId";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(CHECKED_ITEM, mCheckedId);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Makes sure the {@link com.development.jaba.moneypit.BaseActivity} knows which layout to inflate.
     *
     * @return The resource ID of the layout to inflate.
     */
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    /**
     * Called to create the the activity.
     *
     * @param savedInstanceState Previously saved values.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        // Well...Uhm. They changed the NavigationView header behaviour in the
        // 23.1.0 version of the support libraries. We cannot @Bind() the header
        // anymore...
        //
        // We simply do not declare the header in the layout but we inflate it here
        // in code. Oh wel...
        View header = mDrawerView.inflateHeaderView(R.layout.navigation_header);
        Drawable drawable = UtilsHelper.getTintedDrawable(this, R.drawable.background_header, getColorPrimary());
        UtilsHelper.setBackgroundDrawable(header, drawable);

        mTitle = getTitle();
        mToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(mToggle);
        setupDrawerContent(mDrawerView);

        // Do we have a previously checked navigation drawer item?
        // If we do we restore the fragment of that checked item. Otherwise
        // we setup the first fragment.
        if (savedInstanceState != null) {
            mCheckedId = savedInstanceState.getInt(CHECKED_ITEM);
        }
        MenuItem item = mDrawerView.getMenu().findItem(mCheckedId);
        if (item != null) {
            selectDrawerItem(item);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, CarListFragment.newInstance()).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, getToolbar(), R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment;

        switch (menuItem.getItemId()) {
            case R.id.vehicles:
                mCheckedId = R.id.vehicles;
                fragment = CarListFragment.newInstance();
                setTitle(getString(R.string.nav_cars));
                break;
            case R.id.backup:
                mCheckedId = R.id.backup;
                fragment = DriveBackupFragment.newInstance();
                setTitle(getString(R.string.backup_restore));
                break;
            case R.id.settings: {
                if (mDrawer != null) {
                    mDrawer.closeDrawers();
                }
                Intent s = new Intent(this, SettingsActivity.class);
                startActivity(s);
                return;
            }
            default: {
                if (mDrawer != null) {
                    mDrawer.closeDrawers();
                }
                Intent s = new Intent(this, AboutActivity.class);
                startActivity(s);
                return;
            }
        }

        // Insert the fragment by replacing any existing fragment

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        super.setTitle(title);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }
}
