package com.development.jaba.moneypit;

import java.util.Date;
import java.util.Locale;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.database.Utils;
import com.development.jaba.fragments.BaseFragment;
import com.development.jaba.fragments.CarDetailsFillupsFragment;
import com.development.jaba.model.Car;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.view.SlidingTabLayout;

public class CarDetailsActivity extends ActionBarActivity {

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
    ViewPager mViewPager;

    /**
     * The {@link SlidingTabLayout} that will control the {@link ViewPager},
     */
    SlidingTabLayout mSlidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // By default we show the current year.
        mCurrentYear = Utils.getYearFromDate(new Date());

        // Extract the Car instance
        Bundle b = getIntent().getExtras();
        if( b != null) {
            mCarToShow = (Car)b.getSerializable("Car");
            if(mCarToShow != null) {
                setTitle(mCarToShow.toString() + " - " + String.valueOf(mCurrentYear));
            }
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

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
                        BaseFragment fragment = mSectionsPagerAdapter.getFragmentAt(mViewPager.getCurrentItem());
                        if (fragment != null) {
                            fragment.onYearSelected(year);
                        }
                    }

                    // Save the selected year.
                    mCurrentYear = year;
                    setTitle(mCarToShow.toString() + " - " + String.valueOf(mCurrentYear));
                }
            }, this);
        }
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final int NUM_PAGES = 4;
        private BaseFragment[] mPages = new BaseFragment[NUM_PAGES];

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
                mPages[0] = fragment;
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
