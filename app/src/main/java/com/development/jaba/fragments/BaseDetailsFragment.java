package com.development.jaba.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.development.jaba.model.Car;
import com.development.jaba.moneypit.Keys;

/**
 * Base class for the {@link Car} detail fragments.
 */
public class BaseDetailsFragment extends Fragment {

    public Car mCar;
    public int mCurrentYear;

    /**
     * Default empty constructor
     */
    public BaseDetailsFragment() {
    }

    /**
     * Static factory method. Creates a new instance of a {@link com.development.jaba.fragments.BaseDetailsFragment} derived class.
     *
     * @param carToShow The {@link com.development.jaba.model.Car} entity linked to the instance.
     * @param c The class to instantiate.
     * @return The created fragment.
     */
    public static BaseDetailsFragment newInstance(Car carToShow, Class<?> c) {

        BaseDetailsFragment fragment = null;
        try {
            fragment = (BaseDetailsFragment) c.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            Log.e("BaseDetailsFragment", e.getMessage());
        }
        if (fragment != null) {
            fragment.mCar = carToShow;
        }
        return fragment;
    }

    /**
     * Saves the state information. This will both save the {@link Car} entity
     * and the currently selected year into the outState {@link android.os.Bundle}.
     *
     * @param outState The {@link Bundle} into which the state information is saved.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Keys.EK_CAR, mCar);
        outState.putInt(Keys.EK_CURRENTYEAR, mCurrentYear);
        super.onSaveInstanceState(outState);
    }

    /**
     * This is overridden in sub classes to detect when the fragment is selected
     * in a viewpager.
     */
    public void onFragmentSelectedInViewPager() {
    }

    /**
     * This is overridden in sub classes to inform the fragment the user selected
     * another year of data to display.
     *
     * @param year The year the user has selected.
     */
    public void onYearSelected(int year) {
        mCurrentYear = year;
    }

    /**
     * This is overridden in sub classes to inform the fragment the containing data
     * has changed.
     */
    public void onDataChanged() {
    }
}
