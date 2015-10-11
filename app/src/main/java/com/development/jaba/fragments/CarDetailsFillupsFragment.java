package com.development.jaba.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.adapters.FillupRowAdapter;
import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.AddOrEditFillupActivity;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.utilities.FormattingHelper;
import com.development.jaba.view.RecyclerViewEx;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment for displaying the details of a {@link Car}.
 */
public class CarDetailsFillupsFragment extends BaseDetailsFragment {

    private final int REQUEST_ADD_FILLUP = 0,
            REQUEST_EDIT_FILLUP = 1;

    private MoneyPitDbContext mContext;         // The MoneyPit database mContext.
    private FillupRowAdapter mFillupAdapter;    // Adapter for holding the Fill-up list.
    private OnDataChangedListener mCallback;    // Listener for data changes.

    @Bind(R.id.addFab) FloatingActionButton mFab;          // The FloatingActionButton for quick add access.
    @Bind(R.id.fillupList) RecyclerViewEx mFillupList;
    @Bind(R.id.fillupListEmpty) TextView mEmptyText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (mCar == null) {
                mCar = (Car) savedInstanceState.getSerializable(Keys.EK_CAR);
            }
            mCurrentYear = savedInstanceState.getInt(Keys.EK_CURRENTYEAR);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // When we became visible we need to make sure the
        // FAB is also visible.
        if (isVisibleToUser) {
            if (mFab != null && !mFab.isShown()) {
                mFab.show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details_fillups, container, false);
        ButterKnife.bind(this, view);

        mContext = new MoneyPitDbContext(getActivity());
        if (mCar != null) {
            mFillupAdapter = new FillupRowAdapter(getActivity(), mCar, null);
            mFillupAdapter.setEmptyView(mEmptyText);
            mFillupAdapter.setOnRecyclerItemClicked(new OnRecyclerItemClicked() {

                @Override
                public void onExpansionStateChanged(int position, boolean isExpanded) {
                    if (isExpanded) {
                        mFillupList.smoothScrollToPosition(position);
                    } else {
                        mFab.show();
                    }
                }

                @Override
                public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
                    return false;
                }

                @Override
                public boolean onRecyclerItemMenuSelected(final int position, MenuItem item) {
                    int menuItemIndex = item.getItemId();
                    switch (menuItemIndex) {
                        case 0: {
                            Fillup selectedFillup = mFillupAdapter.getItem(position);
                            editFillup(mCar, selectedFillup, position);
                            return true;
                        }

                        case 1:
                            DialogHelper.showYesNoDialog(String.format(getString(R.string.dialog_delete_car_title), FormattingHelper.toShortDate(mFillupAdapter.getLastClickedItem().getDate())),
                                    getText(R.string.dialog_delete_fillup_content),
                                    new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            Fillup selectedFillup = mFillupAdapter.getItem(position);
                                            mContext.deleteFillup(selectedFillup);

                                            new LoadDataTask().execute();

                                            // Notify the activity the data has changed.
                                            if (mCallback != null) {
                                                mCallback.onDataChanged(mCurrentYear);
                                            }
                                        }
                                    },
                                    getActivity());
                            return true;

                        case FillupRowAdapter.MENU_NAV: {
                            Fillup selectedFillup = mFillupAdapter.getItem(position);
                            Intent navigation = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?daddr=" +
                                            String.valueOf(selectedFillup.getLatitude()) + "," +
                                            String.valueOf(selectedFillup.getLongitude())));
                            startActivity(navigation);
                            break;
                        }

                        default:
                            break;
                    }
                    return false;
                }
            });

            mFillupList.setAdapter(mFillupAdapter);
            mFillupList.setLayoutManager(new LinearLayoutManager(getActivity()));
            mFillupList.setHasFixedSize(false);

            // Loadup the data from the database.
            new LoadDataTask().execute();
        }
        return view;
    }

    /**
     * Adds a new {@link Fillup}.
     *
     * @param v The clicked {@link View}.
     */
    @OnClick(R.id.addFab)
    public void onClick(View v) {
        editFillup(mCar, null, -1);
    }

    /**
     * The user selected another year. Update the data to show the data
     * of the new year.
     *
     * @param year The year the user has selected.
     */
    @Override
    public void onYearSelected(int year) {
        super.onYearSelected(year);
        if (mContext != null && mCar != null) {
            new LoadDataTask().execute();
        }
    }

    private void editFillup(Car car, Fillup fillup, int position) {
        Intent editFillup = new Intent(getActivity(), AddOrEditFillupActivity.class);
        editFillup.putExtra(Keys.EK_CAR, car);
        if (fillup != null) {
            editFillup.putExtra(Keys.EK_FILLUP, fillup);
            editFillup.putExtra(Keys.EK_VIEWPOSITION, position);
        }
        startActivityForResult(editFillup, fillup == null ? REQUEST_ADD_FILLUP : REQUEST_EDIT_FILLUP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getExtras().containsKey(Keys.EK_FILLUP)) {
                int newYear = mCurrentYear;
                if (requestCode == REQUEST_ADD_FILLUP) {
                    Fillup fu = (Fillup) data.getExtras().getSerializable(Keys.EK_FILLUP);
                    if (fu != null) {
                        newYear = DateHelper.getYearFromDate(fu.getDate());
                    }
                }

                // Notify the activity the data has changed.
                if (mCallback != null) {
                    mCallback.onDataChanged(newYear);
                }

                // Setup the new year if it changed.
                if (newYear != mCurrentYear) {
                    onYearSelected(newYear);
                } else {
                    // At this point the easiest thing to do is to re-load the
                    // information from the database so that data reflects the changes.
                    new LoadDataTask().execute();
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback = (OnDataChangedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnDataChangedListener");
            }
        }
    }

    /**
     * A parent activity should implement this if it want's to know about
     * data changes.
     */
    public interface OnDataChangedListener {
        void onDataChanged(int year);
    }

    /**
     * {@link android.os.AsyncTask} derived class to get the data to show in the {@link com.development.jaba.view.RecyclerViewEx}.
     */
    private class LoadDataTask extends AsyncTask<Void, Void, List<Fillup>> {

        /**
         * Loads the data from the database.
         *
         * @param params Parameters (not used).
         * @return The data loaded from the database.
         */
        @Override
        protected List<Fillup> doInBackground(Void... params) {
            // TODO: This uses the same data set as the CarDetailsSummary fragment. They should really share it...
            return mContext.getFillupsOfCar(mCar.getId(), mCurrentYear);
        }

        /**
         * Back in the UI thread. Update the visuals with the data.
         *
         * @param result Nothing.
         */
        @Override
        protected void onPostExecute(List<Fillup> result) {
            mFillupAdapter.setData(result);
        }
    }
}
