package com.development.jaba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.model.Car;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.CarAverage;
import com.development.jaba.moneypit.AddOrEditCarActivity;
import com.development.jaba.adapters.CarRowAdapter;
import com.development.jaba.moneypit.CarDetailsActivity;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DialogHelper;
import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;

import java.util.List;

/**
 * Lists the Car entities from the database enabling editing of that list and the
 * Car entities within.
 */
public class CarListFragment extends BaseFragment {

    private static final int REQUEST_EDIT_CAR = 1,  // Request code for editing a car.
                             REQUEST_ADD_CAR = 2;   // Request code for adding a new car.

    private MoneyPitDbContext mContext;              // The MoneyPit database mContext.
    private CarRowAdapter mCarAdapter;               // Adapter for holding the Car list.
    private List<Car> mCars;                         // The list of Car entities from the database.
    private ShowHideOnScroll mFabScroller;           // Shows or hides the FloatingActionButton.

    /**
     * Static factory method. Creates a new instance of this fragment.
     * @param sectionNumber The section number in the Navigation Drawer.
     * @return The created fragment.
     */
    public static CarListFragment newInstance(int sectionNumber) {
        CarListFragment fragment = new CarListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the {@link android.app.Activity} is created.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Called when the {@link View} is created.
     * @param inflater The {@link android.view.LayoutInflater}.
     * @param container The {@link android.view.ViewGroup}.
     * @param savedInstanceState The saved instance state/
     * @return The created {@link View}.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_drawer, container, false);

        mContext = new MoneyPitDbContext(getActivity());
        mCars = mContext.getAllCars();

        List<CarAverage> avgs = mContext.getCarAverages();
        for(CarAverage avg : avgs) {
            for(Car car : mCars) {
                if(car.getId() == avg.getCarId()) {
                    car.setAverages(avg);
                }
            }
        }

        mCarAdapter = new CarRowAdapter(getActivity(), mCars);
        mCarAdapter.setEmptyView(view.findViewById(R.id.listEmpty));
        mCarAdapter.setOnRecyclerItemClicked(new OnRecyclerItemClicked() {
            @Override
            public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
                if (!isLongClick) {
                    showCarDetails(mCarAdapter.getItem(position));
                }
                return false;
            }

            @Override
            public boolean onRecyclerItemMenuSelected(int position, MenuItem item) {
                int menuItemIndex = item.getItemId();
                switch(menuItemIndex) {
                    case 0:
                    {
                        Car selectedCar = mCarAdapter.getItem(position);
                        editCar(selectedCar);
                        return true;
                    }

                    case 1:
                        DialogHelper.showYesNoDialog(String.format(getString(R.string.dialog_delete_car_title), mCarAdapter.getLastClickedItem().toString()),
                                getText(R.string.dialog_delete_car_content),
                                new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        Car selectedCar = mCarAdapter.getLastClickedItem();
                                        mContext.deleteCar(selectedCar);
                                        mCars.remove(selectedCar);
                                        mCarAdapter.notifyDataSetChanged();
                                    }
                                },
                                getActivity());
                        return true;

                    default:
                        break;
                }
                return false;
            }
        });

        RecyclerView carList = (RecyclerView) view.findViewById(R.id.carList);
        carList.setAdapter(mCarAdapter);
        carList.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addFab);
        mFabScroller = new ShowHideOnScroll(fab);
        carList.setOnTouchListener(mFabScroller);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCar(null);
            }
        });
        return view;
    }

    /**
     * Open up the {@link Car} details activity for the given car.
     * @param car The {@link Car} which to show in the details page.
     */
    private void showCarDetails(Car car) {
        Intent carDetails = new Intent(getActivity(), CarDetailsActivity.class);
        if(car != null) {
            carDetails.putExtra("Car", car);
        }
        startActivity(carDetails);
    }

    /**
     * Open up the {@link Car} edit activity for the given car.
     * @param car The {@link Car} which to edit or null to create a new car.
     */
    private void editCar(Car car) {
        Intent editCar = new Intent(getActivity(), AddOrEditCarActivity.class);
        if(car != null) {
            editCar.putExtra("Car", car);
        }
        startActivityForResult(editCar, car == null ? REQUEST_ADD_CAR : REQUEST_EDIT_CAR);
    }

    /**
     * Evaluates the result for the {@link android.app.Activity}.
     * @param requestCode The request code that is giving the result.
     * @param resultCode The result code.
     * @param data The data of the result.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getExtras().containsKey("Car")) {
                Car car = (Car) data.getExtras().get("Car");
                if (car != null) {
                    // Was this an edit result?
                    if (requestCode == REQUEST_EDIT_CAR) {
                        // Find the Car entity from the data and update it.
                        for (int i = 0; i < mCars.size(); i++) {
                            if (mCars.get(i).getId() == car.getId()) {
                                mCars.set(i, car);
                                break;
                            }
                        }
                    } else {
                        // Simply add the Car to the data.
                        mCars.add(car);
                    }

                    // Update the visuals.
                    mCarAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    //region Options menu

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
        int id = item.getItemId();

        // All other items are not our's...
        return false;
    }
    //endregion
}
