package com.development.jaba.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.development.jaba.moneypit.R;

/**
 * A {@link BaseDetailsFragment} subclass containing the summary information
 * of a car for a given period.
 */
public class CarDetailsSummaryFragment extends BaseDetailsFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details_summary, container, false);
        return view;
    }

}
