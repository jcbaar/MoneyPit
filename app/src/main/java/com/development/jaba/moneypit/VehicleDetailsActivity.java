package com.development.jaba.moneypit;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
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
import android.widget.ImageView;
import android.widget.Spinner;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.fragments.BaseDetailsFragment;
import com.development.jaba.fragments.CarDetailsCostFragment;
import com.development.jaba.fragments.CarDetailsEconomyFragment;
import com.development.jaba.fragments.CarDetailsFillupsFragment;
import com.development.jaba.fragments.CarDetailsSummaryFragment;
import com.development.jaba.model.Car;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.UtilsHelper;
import com.development.jaba.view.PageTransformerEx;
import com.development.jaba.view.ViewPagerEx;

import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity showing the detail of a specific vehicle.
 */
public class VehicleDetailsActivity extends BaseActivity implements CarDetailsFillupsFragment.OnDataChangedListener {

    // Views bound using ButterKnife.
    @SuppressWarnings("unused")
    @Bind(R.id.image) ImageView mCarImage;                 // Shows the image of the car or, when there is no image, a simple tinted material design image.
    @SuppressWarnings("unused")
    @Bind(R.id.pager) ViewPagerEx mViewPager;              // ViewPager that serves as a host for the fragments.
    @SuppressWarnings("unused")
    @Bind(R.id.sliding_tabs) TabLayout mSlidingTabLayout;  // Sliding tab that controls the ViewPager.
    @SuppressWarnings("unused")
    @Bind(R.id.addFab) FloatingActionButton mFab;           // The FloatingActionButton for quick add access.

    private Car mCarToShow;                                 // The vehicle we are showing the details for.
    private int mCurrentYear;                               // Currently selected year.
    private MoneyPitDbContext mDbContext;                   // Database context.
    private Spinner mYearSpinner;                           // Instance of the year selection spinner.
    private SectionsPagerAdapter mSectionsPagerAdapter;     // ViewPager adapter for serving up the detail fragments.
    private ViewPager.OnPageChangeListener mListener;       // Listener for page changing events.

    /**
     * Saves the state information of the activity.
     * @param savedInstanceState The {@link Bundle} in which the activity state is saved.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(Keys.EK_CURRENTYEAR, mCurrentYear);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Creates the activity.
     * @param savedInstanceState The {@link Bundle} with state information or null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Extract the Car instance
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCarToShow = (Car) b.getSerializable("Car");
        }

        // By default we show the current year. If a year was selected previously
        // for this vehicle we use that.
        if (savedInstanceState != null) {
            mCurrentYear = savedInstanceState.getInt(Keys.EK_CURRENTYEAR);
        } else {
            mCurrentYear = getCarYearFromPrefs();
        }

        // Show the vehicle image. If no vehicle image is present we use a tinted
        // material design image instead.
        Bitmap carImage = null;
        if(mCarToShow != null) {
            setTitle(mCarToShow.toString());
            carImage = mCarToShow.getImage();
            if(carImage != null) {
                mCarImage.setImageBitmap(carImage);
            }
        }

        // Ideally I would collapse the ImageView and prevent the nested scrolling from
        // resizing the AppBarLayout without affecting other layout behaviours when there
        // is no vehicle image. Since I have yet to find a way to do that I will simply
        // load a tinted header background image instead...
        if(carImage == null) {
            Drawable drawable = UtilsHelper.getTintedDrawable(this, R.drawable.background_header, getColorPrimary());
            UtilsHelper.setBackgroundDrawable(mCarImage, drawable);
        }

        // Instantiate a database context.
        mDbContext = new MoneyPitDbContext(this);

        // Setup the year selection spinner.
        LayoutInflater inflater = LayoutInflater.from(getToolbar().getContext());
        View layout = inflater.inflate(R.layout.year_spinner, mSlidingTabLayout, false);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.END;
        getToolbar().addView(layout, 0, layoutParams);
        mYearSpinner = (Spinner) findViewById(R.id.yearSpinner);
        setupYears();

        ButterKnife.bind(this);

        mYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int year = Integer.parseInt(mYearSpinner.getSelectedItem().toString());

                // Broadcast the year selection to all available fragments. They need to know what year
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
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new PageTransformerEx(PageTransformerEx.TransformType.ROLL));
        mSlidingTabLayout.setupWithViewPager(mViewPager);

        // Setup the page change listener for the ViewPager.
        mListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            /**
             * We only want the {@link FloatingActionButton} available when we are on the first
             * {@link Fragment} and there is data present for the selected year. The other fragments do
             * not show the {@link FloatingActionButton}. Ideally the {@link FloatingActionButton} would
             * be part of that {@link Fragment} but that would make it unreachable for the
             * {@link android.support.design.widget.CoordinatorLayout}.
             * @param position The position of the currently selected {@link Fragment}.
             */
            @Override
            public void onPageSelected(int position) {
                if(mViewPager.getSwipeEnabled() && position == 0) {
                    mFab.show();
                }
                else {
                    mFab.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        // Setup the page sliding functionality. The page sliding will be unavailable
        // when there is no data for the selected year.
        checkSlidingAvailability();
    }


    /**
     * Gets the layout to inflate for this {@link android.app.Activity}
     * @return The resource ID of the layout to inflate.
     */
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_vehicle_details;
    }

    /**
     * Passes the {@link FloatingActionButton} click to the first fragment (the fill up list).
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.addFab)
    public void onClick() {
        BaseDetailsFragment b = mSectionsPagerAdapter.getFragmentAt(0);
        if(b != null) {
            b.onFabClicked();
        }
    }

    /**
     * Reads the last selected year for this car from the settings. If none was
     * saved yet return the current year.
     * @return The selected year or the current year.
     */
    private int getCarYearFromPrefs() {
        if(mCarToShow != null) {
            String key = mCarToShow.toString() + "_year";
            return getSettings().getIntegerValue(key, DateHelper.getYearFromDate(new Date()));
        }
        return 0;
    }

    /**
     * Saves the currently selected year for the car to the
     * settings.
     */
    private void saveCarYearToPrefs() {
        String key = mCarToShow.toString() + "_year";
        getSettings().setIntegerValue(key, mCurrentYear);
    }

    /**
     * Setup the items in the year spinner. The range will be from the "oldest" entry
     * in the car it's fill-up list until the current year.
     * TODO: Perhaps years that have no data should be filtered from the selectable years (with the exception of the current year of course)?
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
        boolean hasData = mDbContext.hasData(mCarToShow.getId(), mCurrentYear) > 0;

        // No data? Go to the first page...
        if (!hasData) {
            mViewPager.setCurrentItem(0);
        }

        mViewPager.setSwipeEnabled(hasData);
        if(!hasData) {
            mViewPager.removeOnPageChangeListener(mListener);
        }
        else {
            mViewPager.addOnPageChangeListener(mListener);
        }
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
         * the position is out of bounds.
         */
        public BaseDetailsFragment getFragmentAt(int position) {
            if (position >= 0 && position < NUM_PAGES) {
                return mPages[position];
            }
            return null;
        }

        /**
         * Called each time the {@link android.support.v4.view.ViewPager} want's to get to a {@link android.support.v4.app.Fragment}
         * instance. We use this to manage the instances for this {@link com.development.jaba.moneypit.VehicleDetailsActivity.SectionsPagerAdapter}.
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
         * @param position The position for which an instance has to be created.
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
         * Gets the number of pages in this {@link com.development.jaba.moneypit.VehicleDetailsActivity.SectionsPagerAdapter}.
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
