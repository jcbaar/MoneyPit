package com.development.jaba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.development.jaba.adapters.FillupRowAdapter;
import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.database.Utils;
import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.model.SurroundingFillups;
import com.development.jaba.moneypit.AddOrEditFillupActivity;
import com.development.jaba.view.RecyclerViewEx;
import com.development.jaba.moneypit.R;
import com.melnykov.fab.FloatingActionButton;

import java.util.Date;
import java.util.List;

/**
 * Fragment for displaying the details of a {@link Car}.
 */
public class CarDetailsFillupsFragment extends BaseFragment {

    private final int REQUEST_ADD_FILLUP = 0,
            REQUEST_EDIT_FILLUP = 1;

    private MoneyPitDbContext mContext;              // The MoneyPit database mContext.
    private FillupRowAdapter mFillupAdapter;         // Adapter for holding the Fill-up list.
    private List<Fillup> mFillups;                   // The list of Fillup entities from the database.
    private Car mCar;
    private FloatingActionButton mFab;

    /**
     * Static factory method. Creates a new instance of this fragment.
     * @param sectionNumber The section number in the Navigation Drawer.
     * @return The created fragment.
     */
    public static Fragment newInstance(int sectionNumber, Car carToShow) {
        CarDetailsFillupsFragment fragment = new CarDetailsFillupsFragment();
        fragment.mCar = carToShow;

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(mCar == null && savedInstanceState != null) {
            mCar = (Car)savedInstanceState.getSerializable("Car");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("Car", mCar);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFragmentSelectedInViewPager() {
        mFab.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details_fillups, container, false);

        mContext = new MoneyPitDbContext(getActivity());
        if(mCar != null) {
            mFillups = mContext.getFillupsOfCar(mCar.getId(), Utils.getYearFromDate(new Date()));

            mFillupAdapter = new FillupRowAdapter(getActivity(), mCar, mFillups);
            mFillupAdapter.setEmptyView(view.findViewById(R.id.fillupListEmpty));
            mFillupAdapter.setOnRecyclerItemClicked(new OnRecyclerItemClicked() {
                @Override
                public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
                    if(!isLongClick) {
                        //open up the item editor.
                    }
                    return false;
                }

                @Override
                public boolean onRecyclerItemMenuSelected(int position, MenuItem item) {
                    // evaluate menu item clicks.
                    return false;
                }
            });

            RecyclerViewEx fillupList = (RecyclerViewEx) view.findViewById(R.id.fillupList);
            fillupList.setAdapter(mFillupAdapter);
            fillupList.setLayoutManager(new LinearLayoutManager(getActivity()));

            mFab = (FloatingActionButton) view.findViewById(R.id.addFab);
            mFab.attachToRecyclerView(fillupList);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editFillup(mCar, null);
                }
            });
        }
        return view;
    }

    private void editFillup(Car car, Fillup fillup) {
        Intent editFillup = new Intent(getActivity(), AddOrEditFillupActivity.class);
        editFillup.putExtra("Car", car);
        if(fillup != null) {
            editFillup.putExtra("Fillup", fillup);
        }
        startActivityForResult(editFillup, fillup == null ? REQUEST_ADD_FILLUP : REQUEST_EDIT_FILLUP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getExtras().containsKey("Fillup")) {
                Fillup fillup = (Fillup) data.getExtras().get("Fillup");
                if (fillup != null) {
                    if (requestCode == REQUEST_EDIT_FILLUP) {
                        for (int i = 0; i < mFillups.size(); i++) {
                            if (mFillups.get(i).getId() == fillup.getId()) {
                                mFillups.set(i, fillup);
                                break;
                            }
                        }
                    } else {
                        mFillups.add(fillup);
                    }

                    Fillup oldest = mFillupAdapter.getItem(mFillupAdapter.getItemCount() - 1);
                    SurroundingFillups result = mContext.getSurroundingFillups(oldest.getDate(), oldest.getCarId(), oldest.getId());

                    Utils.recomputeFillupTotals(mFillups, result != null ? result.getBefore() : null);
                    mFillupAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * Creates the fragment menu items.
     * @param menu The menu to create the fragment menu items in.
     * @param inflater The @{link MenuItemInflater}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_carlist, menu);
    }

    /**
     * Handler for selection of a option menu item.
     * @param item The item that was selected.
     * @return true if the item was handled, false if it was not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_add_car) {
//            editCar(null);
//            return true;
//        }

        // All other items are not our's...
        return false;
    }
    //endregion
}
