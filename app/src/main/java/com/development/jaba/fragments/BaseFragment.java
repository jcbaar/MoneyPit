package com.development.jaba.fragments;

import android.net.Uri;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    public static final String ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER";

    /**
     * Default empty constructor
     */
    public BaseFragment() {
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
    }

    /**
     * This is overridden in sub classes to inform the fragment the containing data
     * has changed.
     */
    public void onDataChanged() {
    }

    /**
     * This interface must be implemented by activities that contain this
     * mFragment to allow an interaction in this mFragment to be communicated
     * to the mActivity and potentially other fragments contained in that
     * mActivity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
        public void onFragmentInteraction(String id);
        public void onFragmentInteraction(int actionId);
    }
}
