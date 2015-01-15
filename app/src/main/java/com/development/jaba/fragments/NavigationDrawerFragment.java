package com.development.jaba.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.development.jaba.adapters.NavigationDrawerAdapter;
import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.model.NavigationDrawerItem;
import com.development.jaba.moneypit.R;
import com.development.jaba.moneypit.SettingsActivity;
import com.development.jaba.utilities.SettingsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    private RecyclerView mDrawerList;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Helper instance for reading and writing app settings.
     */
    private SettingsHelper mSettings;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private NavigationDrawerAdapter mAdapter;

    private int mCurrentSelectedPosition = -1;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        mSettings = new SettingsHelper(getActivity());
        mUserLearnedDrawer = mSettings.getUserLearnedDrawer();

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    /**
     * Setup the {@link java.util.List} of {@link com.development.jaba.model.NavigationDrawerItem} items for the navigation drawer.
     *
     * @return The {@link java.util.List} of {@link com.development.jaba.model.NavigationDrawerItem} objects.
     */
    private List<NavigationDrawerItem> getData() {
        List<NavigationDrawerItem> data = new ArrayList<>();
        NavigationDrawerItem item = new NavigationDrawerItem();

        item.setTitle(getResources().getString(R.string.nav_cars));
        item.setIconId(R.drawable.ic_directions_car_grey600_24dp);
        data.add(item);
        return data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerList = (RecyclerView) layout.findViewById(R.id.navigationList);
        mAdapter = new NavigationDrawerAdapter(getActivity(), getData());

        mAdapter.setOnRecyclerItemClicked(new OnRecyclerItemClicked() {
            @Override
            public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
                if (!isLongClick) {
                    selectItem(position);
                }
                return false;
            }

            @Override
            public boolean onRecyclerItemMenuSelected(int position, MenuItem item) {
                return false;
            }
        });

        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectItem(mCurrentSelectedPosition < 0 ? 0 : mCurrentSelectedPosition);

        // Setup the click listeners for the settings and the about activities.
        View settings = layout.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent s = new Intent(getActivity(), SettingsActivity.class);
                startActivity(s);
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            }
        });

        View about = layout.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(mFragmentContainerView);
                }
            }
        });

        return layout;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolbar,
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    mSettings.setUserLearnedDrawer(true);
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * Selectes the specified item in the navigation drawer.
     *
     * @param position The position of the item to select.
     */
    private void selectItem(int position) {
        mAdapter.selectItem(position);

        if (position != mCurrentSelectedPosition) {
            mCurrentSelectedPosition = position;

            if (mCallbacks != null) {
                mCallbacks.onNavigationDrawerItemSelected(position);
            }
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            menu.clear();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
