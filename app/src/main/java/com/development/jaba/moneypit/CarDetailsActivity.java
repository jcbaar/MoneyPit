package com.development.jaba.moneypit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.fragments.BaseFragment;
import com.development.jaba.fragments.CarDetailsCostFragment;
import com.development.jaba.fragments.CarDetailsEconomyFragment;
import com.development.jaba.fragments.CarDetailsFillupsFragment;
import com.development.jaba.model.Car;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.utilities.SettingsHelper;
import com.development.jaba.view.SlidingTabLayout;
import com.development.jaba.view.ViewPagerEx;

import java.util.Date;
import java.util.Locale;

public class CarDetailsActivity extends BaseActivity implements CarDetailsFillupsFragment.OnDataChangedListener {

    /**
     * The {@link Car} entity to show the details of.
     */
    private Car mCarToShow;

    /**
     * The currently selected year.
     */
    private int mCurrentYear;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPagerEx mViewPager;

    MoneyPitDbContext mDbContext;
    SettingsHelper mSettings;

    /**
     * The {@link SlidingTabLayout} that will control the {@link ViewPager},
     */
    SlidingTabLayout mSlidingTabLayout;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("CurrentYear", mCurrentYear);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Makes sure the {@link com.development.jaba.moneypit.BaseActivity} knows which layout to inflate.
     * @return The resource ID of the layout to inflate.
     */
    protected int getLayoutResource() {
        return R.layout.activity_car_details;
    }

    /**
     * Reads the last selected year for this car from the settings. If none was
     * saved yet return the current year.
     *
     * @return The selected year or the current year.
     */
    protected int getCarYearFromPrefs() {
        String key = mCarToShow.toString() + "_year";
        return mSettings.getIntegerValue(key, DateHelper.getYearFromDate(new Date()));
    }

    /**
     * Saves the currently selected year for the car to the
     * settings.
     */
    protected void saveCarYearToPrefs() {
        String key = mCarToShow.toString() + "_year";
        mSettings.setIntegerValue(key, mCurrentYear);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create settings helper instance.
        mSettings = new SettingsHelper(this);

        // By default we show the current year.
        mCurrentYear = DateHelper.getYearFromDate(new Date());
        if(savedInstanceState != null) {
            mCurrentYear = savedInstanceState.getInt("CurrentYear");
        } else {
            mCurrentYear = getCarYearFromPrefs();
        }


        // Extract the Car instance
        Bundle b = getIntent().getExtras();
        if( b != null) {
            mCarToShow = (Car)b.getSerializable("Car");
            if(mCarToShow != null) {
                setTitle(mCarToShow.toString() + " - " + String.valueOf(mCurrentYear));
            }
        }

        // Check if there is any data available.
        mDbContext = new MoneyPitDbContext(this);
        boolean hasData = mDbContext.hasData(mCarToShow.getId(), mCurrentYear);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPagerEx) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setVisibility(hasData ? View.VISIBLE : View.GONE);

        checkSlidingAvailability();

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Get the fragment at the given position and tell it it's been
                // selected in the ViewPager.
                BaseFragment fragment = mSectionsPagerAdapter.getFragmentAt(position);
                if(fragment != null) {
                    fragment.onFragmentSelectedInViewPager();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_car_details, menu);
        return true;
    }

    /**
     * Checks to see whether or not paging and tabs should be present.
     */
    private void checkSlidingAvailability() {
        boolean hasData = mDbContext.hasData(mCarToShow.getId(), mCurrentYear);

        // No data? Go to the first page...
        if(!hasData) {
            mViewPager.setCurrentItem(0);
        }

        mViewPager.setSwipeEnabled(hasData);
        mSlidingTabLayout.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.selectYear) {
            // Let the user select the year of data they want to see.
            DialogHelper.showYearSelectionDialog(mCarToShow, mCurrentYear, new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);

                    // Get the currently selected item from the view. This
                    // will contain the selected year.
                    View view = dialog.getCustomView();
                    Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
                    int year = Integer.parseInt(spinner.getSelectedItem().toString());

                    // Broadcast the year selection to all fragments. They need to know what year
                    // was selected so they they can update their contents.
                    for ( int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                        BaseFragment fragment = mSectionsPagerAdapter.getFragmentAt(i);
                        if (fragment != null) {
                            fragment.onYearSelected(year);
                        }
                    }

                    // Save the selected year.
                    mCurrentYear = year;
                    setTitle(mCarToShow.toString() + " - " + String.valueOf(mCurrentYear));
                    checkSlidingAvailability();
                    saveCarYearToPrefs();
                }
            }, this);
        }
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called whenever the fill-up list fragment reported a data change. From here
     * we also need to pass this event on to the other fragments.
     *
     * @param year The year in which the data was changed.
     */
    @Override
    public void onDataChanged(int year) {
        // Did the year change? If so save it to the settings.
        if (mCurrentYear != year) {
            saveCarYearToPrefs();
        }

        // Skip the first fragment since this is the fill-up list fragment which sent
        // the event in the first place.
        for (int i = 1; i <= mSectionsPagerAdapter.getCount(); i++) {
            BaseFragment b = mSectionsPagerAdapter.getFragmentAt(i);
            if(b != null) {
                // If the year has changed we set the new year to the
                // other fragments which will trigger a data refresh
                // on those fragments. If the year did not change we need
                // to tell the other fragments the data of the current
                // year changed.
                if(year != mCurrentYear) {
                    b.onYearSelected(year);
                }
                else {
                    b.onDataChanged();
                }
            }
        }

        if(year != mCurrentYear) {
            mCurrentYear = year;
            setTitle(mCarToShow.toString() + " - " + String.valueOf(mCurrentYear));
        }
        checkSlidingAvailability();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final int NUM_PAGES = 4;
        private final BaseFragment[] mPages = new BaseFragment[NUM_PAGES];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public BaseFragment getFragmentAt(int position) {
            if(position >= 0 && position < NUM_PAGES) {
                return mPages[position];
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if(position == 0) {
                CarDetailsFillupsFragment fragment = (CarDetailsFillupsFragment)CarDetailsFillupsFragment.newInstance(position, mCarToShow);
                fragment.onYearSelected(mCurrentYear);
                mPages[0] = fragment;
                return fragment;
            }
            else if(position == 1) {
                CarDetailsCostFragment fragment = (CarDetailsCostFragment)CarDetailsCostFragment.newInstance(position, mCarToShow);
                fragment.onYearSelected(mCurrentYear);
                mPages[1] = fragment;
                return fragment;
            }
            else if(position == 2) {
                CarDetailsEconomyFragment fragment = (CarDetailsEconomyFragment)CarDetailsEconomyFragment.newInstance(position, mCarToShow);
                fragment.onYearSelected(mCurrentYear);
                mPages[2] = fragment;
                return fragment;
            }
            PlaceholderFragment fragment = PlaceholderFragment.newInstance(position + 1);
            mPages[position] = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.tab_fillups).toUpperCase(l);
                case 1:
                    return getString(R.string.tab_cost).toUpperCase(l);
                case 2:
                    return getString(R.string.tab_mileage).toUpperCase(l);
                case 3:
                    return getString(R.string.tab_summary).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends BaseFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return  inflater.inflate(R.layout.fragment_car_details, container, false);
        }
    }

}
