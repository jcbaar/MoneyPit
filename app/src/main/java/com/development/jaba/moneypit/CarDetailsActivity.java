package com.development.jaba.moneypit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.fragments.BaseDetailsFragment;
import com.development.jaba.fragments.CarDetailsCostFragment;
import com.development.jaba.fragments.CarDetailsEconomyFragment;
import com.development.jaba.fragments.CarDetailsFillupsFragment;
import com.development.jaba.fragments.CarDetailsSummaryFragment;
import com.development.jaba.model.Car;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.SettingsHelper;
import com.development.jaba.view.SlidingTabLayout;
import com.development.jaba.view.ViewPagerEx;

import java.util.Date;
import java.util.Locale;

public class CarDetailsActivity extends BaseActivity implements CarDetailsFillupsFragment.OnDataChangedListener {

    private Car mCarToShow;                     // The car we are currently showing the details for.
    private int mCurrentYear;                   // The year we are currently showing the details for.
    SectionsPagerAdapter mSectionsPagerAdapter; // ViewPager adapter for serving up the fragments.
    ViewPagerEx mViewPager;                     // ViewPager that serves as a host for the fragments.
    MoneyPitDbContext mDbContext;               // Database context.
    SettingsHelper mSettings;                   // Settings context.
    SlidingTabLayout mSlidingTabLayout;         // Sliding tab that controls the ViewPager.
    Spinner mYearSpinner;                       // Year selection spinner.

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(Keys.EK_CURRENTYEAR, mCurrentYear);
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

        // Extract the Car instance
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCarToShow = (Car) b.getSerializable("Car");
        }

        // By default we show the current year. If a year was selected previously
        // for this car we use that.
        if (savedInstanceState != null) {
            mCurrentYear = savedInstanceState.getInt(Keys.EK_CURRENTYEAR);
        } else {
            mCurrentYear = getCarYearFromPrefs();
        }

        setTitle(mCarToShow.toString());

        // Check if there is any data available.
        mDbContext = new MoneyPitDbContext(this);

        // Setup the spinner.
        LayoutInflater inflater = LayoutInflater.from(getToolbar().getContext());
        View layout = inflater.inflate(R.layout.year_spinner, null);
        mYearSpinner = (Spinner) layout.findViewById(R.id.yearSpinner);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.RIGHT;
        getToolbar().addView(layout, 0, layoutParams);
        setupYears();

        mYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int year = Integer.parseInt(mYearSpinner.getSelectedItem().toString());

                // Broadcast the year selection to all fragments. They need to know what year
                // was selected so they they can update their contents.
                for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                    BaseDetailsFragment fragment = mSectionsPagerAdapter.getFragmentAt(i);
                    if (fragment != null) {
                        fragment.onYearSelected(year);
                    }
                }

                // Save the selected year.
                mCurrentYear = year;
                checkSlidingAvailability();
                saveCarYearToPrefs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Create the adapter that will return a fragment for each of the four
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPagerEx) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        // Setup the page sliding functionality.
        checkSlidingAvailability();

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Get the fragment at the given position and tell it it's been
                // selected in the ViewPager.
                BaseDetailsFragment fragment = mSectionsPagerAdapter.getFragmentAt(position);
                if (fragment != null) {
                    fragment.onFragmentSelectedInViewPager();
                }
            }
        });
    }

    /**
     * Setup the items in the year spinner. The range will be from the "oldest" entry
     * in the car it's fill-up list until the current year.
     */
    private void setupYears() {
        Integer start = mDbContext.getOldestDataYear(mCarToShow.getId()),
                end = DateHelper.getYearFromDate(new Date());
        Integer[] years = new Integer[end - start + 1];

        int toSelect = 0;
        for (int i = 0; i < years.length; i++) {
            years[i] = start + i;

            // Mark the current item so we know which one
            // to pre-select.
            if (years[i] == mCurrentYear) {
                toSelect = i;
            }
        }

        ArrayAdapter<Integer> ad = new ArrayAdapter<>(this, R.layout.year_spinner_item, years);
        ad.setDropDownViewResource(R.layout.year_spinner_item_dropdown);
        mYearSpinner.setAdapter(ad);
        mYearSpinner.setSelection(toSelect);
    }

    /**
     * Checks to see whether or not paging and tabs should be present.
     */
    private void checkSlidingAvailability() {
        boolean hasData = mDbContext.hasData(mCarToShow.getId(), mCurrentYear);

        // No data? Go to the first page...
        if (!hasData) {
            mViewPager.setCurrentItem(0);
        }

        mViewPager.setSwipeEnabled(hasData);
        mSlidingTabLayout.setVisibility(hasData ? View.VISIBLE : View.GONE);
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
            setupYears();
        }

        // Skip the first fragment since this is the fill-up list fragment which sent
        // the event in the first place.
        for (int i = 1; i < mSectionsPagerAdapter.getCount(); i++) {
            BaseDetailsFragment b = mSectionsPagerAdapter.getFragmentAt(i);
            if (b != null) {
                // If the year has changed we set the new year to the
                // other fragments which will trigger a data refresh
                // on those fragments. If the year did not change we need
                // to tell the other fragments the data of the current
                // year changed.
                if (year != mCurrentYear) {
                    b.onYearSelected(year);
                } else {
                    b.onDataChanged();
                }
            }
        }

        if (year != mCurrentYear) {
            mCurrentYear = year;
        }
        checkSlidingAvailability();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final int NUM_PAGES = 4;
        private final BaseDetailsFragment[] mPages = new BaseDetailsFragment[NUM_PAGES];
        private final String[] mTitles = new String[NUM_PAGES];

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param fm The {@link android.support.v4.app.FragmentManager}.
         */
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            // Setup tab titles.
            Locale l = Locale.getDefault();
            mTitles[0] = getString(R.string.tab_fillups).toUpperCase(l);
            mTitles[1] = getString(R.string.tab_cost).toUpperCase(l);
            mTitles[2] = getString(R.string.tab_mileage).toUpperCase(l);
            mTitles[3] = getString(R.string.tab_summary).toUpperCase(l);
        }

        /**
         * Returns the instance of the {@link com.development.jaba.fragments.BaseDetailsFragment} at the given position.
         *
         * @param position The position for which to get the {@link com.development.jaba.fragments.BaseDetailsFragment}.
         * @return The {@link com.development.jaba.fragments.BaseDetailsFragment} instance or null of it is not valid or
         * the poisition is out of bounds.
         */
        public BaseDetailsFragment getFragmentAt(int position) {
            if (position >= 0 && position < NUM_PAGES) {
                return mPages[position];
            }
            return null;
        }

        /**
         * Called each time the {@link android.support.v4.view.ViewPager} want's to get to a {@link android.support.v4.app.Fragment}
         * instance. We use this to manage the instances for this {@link com.development.jaba.moneypit.CarDetailsActivity.SectionsPagerAdapter}.
         *
         * @param container The {@link android.view.ViewGroup}.
         * @param position  The position to get the instance for.
         * @return The {@link com.development.jaba.fragments.BaseDetailsFragment} instance for the given position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Let the superclass come up with the instance we need.
            BaseDetailsFragment fragment = (BaseDetailsFragment) super.instantiateItem(container, position);

            // Save it in our cache and update it's current year.
            mPages[position] = fragment;
            fragment.onYearSelected(mCurrentYear);
            return fragment;
        }

        /**
         * A {@link android.support.v4.app.Fragment} is being destroyed. Remove it from out cache.
         *
         * @param container The {@link android.view.ViewGroup}.
         * @param position  The position of the destroyed {@link android.support.v4.app.Fragment}.
         * @param object    The object to be destroyed.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mPages[position] = null;
        }

        /**
         * Called to actually instantiate a new {@link com.development.jaba.fragments.BaseDetailsFragment} for the
         * {@link android.support.v4.view.ViewPager}.
         *
         * @param position The position for which an instance hase to be created.
         * @return The instance of the {@link com.development.jaba.fragments.BaseDetailsFragment} derived class.
         */
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return BaseDetailsFragment.newInstance(mCarToShow, CarDetailsFillupsFragment.class);
            } else if (position == 1) {
                return BaseDetailsFragment.newInstance(mCarToShow, CarDetailsCostFragment.class);
            } else if (position == 2) {
                return BaseDetailsFragment.newInstance(mCarToShow, CarDetailsEconomyFragment.class);
            } else if (position == 3) {
                return BaseDetailsFragment.newInstance(mCarToShow, CarDetailsSummaryFragment.class);
            }
            return null;
        }

        /**
         * Gets the number of pages in this {@link com.development.jaba.moneypit.CarDetailsActivity.SectionsPagerAdapter}.
         *
         * @return The page count.
         */
        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        /**
         * Gets the title of the page for the given position.
         *
         * @param position The position for which to get the title.
         * @return The title of the page for the given position. null if out of bounds.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            if (position >= 0 && position < NUM_PAGES) {
                return mTitles[position];
            }
            return null;
        }
    }
}
