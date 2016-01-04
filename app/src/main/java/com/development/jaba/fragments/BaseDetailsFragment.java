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

    protected Car mCar;
    protected int mCurrentYear = 0;

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
    @SuppressWarnings("TryWithIdenticalCatches")
    public static BaseDetailsFragment newInstance(Car carToShow, Class<?> c) {

        BaseDetailsFragment fragment = null;
        try {
            fragment = (BaseDetailsFragment) c.newInstance();
        }
        catch (java.lang.InstantiationException e) {
            Log.e("BaseDetailsFragment", e.getMessage());
        }
        catch (IllegalAccessException e) {
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
     * This should be overridden in sub classes to be informed when the user selected
     * another year of data to display.
     *
     * @param year The year the user has selected.
     */
    public void onYearSelected(int year) {
        mCurrentYear = year;
    }

    /**
     * This should be overridden in sub classes to be informed the containing data
     * has changed.
     */
    public void onDataChanged() {
    }

    /**
     * This should be overridden in sub classes to be informed the {@link android.support.design.widget.FloatingActionButton}
     * button, which is located in the parent activity) was clicked.
     */
    public void onFabClicked() {
    }

    /**
     * This should be overridden in sub classes to be informed when the {@link Car} entity is changed.
     * @param car The new {@link Car} entity (can be null).
     */
    public void setCar(Car car) {
        mCar = car;
    }
}
