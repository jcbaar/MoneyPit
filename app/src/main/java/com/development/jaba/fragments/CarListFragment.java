package com.development.jaba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.adapters.CarRowAdapter;
import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.CarAverage;
import com.development.jaba.moneypit.AddOrEditCarActivity;
import com.development.jaba.moneypit.CarDetailsActivity;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DialogHelper;
import com.melnykov.fab.FloatingActionButton;

import java.util.List;

/**
 * Lists the Car entities from the database enabling editing of that list and the
 * Car entities within.
 */
public class CarListFragment extends Fragment {

    private static final int REQUEST_EDIT_CAR = 1,  // Request code for editing a car.
                             REQUEST_ADD_CAR = 2;   // Request code for adding a new car.

    private MoneyPitDbContext mContext;              // The MoneyPit database mContext.
    private CarRowAdapter mCarAdapter;               // Adapter for holding the Car list.
    private List<Car> mCars;                         // The list of Car entities from the database.

    /**
     * Static factory method. Creates a new instance of this fragment.
     * @return The created fragment.
     */
    public static CarListFragment newInstance() {
        CarListFragment fragment = new CarListFragment();
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
            public boolean onRecyclerItemMenuSelected(final int position, MenuItem item) {
                int menuItemIndex = item.getItemId();
                switch(menuItemIndex) {
                    case 0:
                    {
                        Car selectedCar = mCarAdapter.getItem(position);
                        editCar(selectedCar, position);
                        return true;
                    }

                    case 1:
                        DialogHelper.showYesNoDialog(String.format(getString(R.string.dialog_delete_car_title), mCarAdapter.getLastClickedItem().toString()),
                                getText(R.string.dialog_delete_car_content),
                                new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        Car selectedCar = mCarAdapter.getItem(position);
                                        mContext.deleteCar(selectedCar);
                                        mCars.remove(selectedCar);
                                        mCarAdapter.notifyItemRemoved(position);
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
        fab.attachToRecyclerView(carList);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCar(null, -1);
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
            carDetails.putExtra(Keys.EK_CAR, car);
        }
        startActivity(carDetails);
    }

    /**
     * Open up the {@link Car} edit activity for the given car.
     * @param car The {@link Car} which to edit or null to create a new car.
     * @param position The position in the {@link android.support.v7.widget.RecyclerView} the item has or -1 in case of a new item.
     */
    private void editCar(Car car, int position) {
        Intent editCar = new Intent(getActivity(), AddOrEditCarActivity.class);
        if(car != null) {
            editCar.putExtra(Keys.EK_CAR, car);
            editCar.putExtra(Keys.EK_VIEWPOSITION, position);
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
            if (data.getExtras().containsKey(Keys.EK_CAR)) {
                Car car = (Car) data.getExtras().get(Keys.EK_CAR);
                if (car != null) {
                    // Get it's averages.
                    car.setAverages(mContext.getCarAverage(car.getId()));

                    // Was this an edit result?
                    if (requestCode == REQUEST_EDIT_CAR) {
                        // Find the Car entity from the data and update it.
                        for (int i = 0; i < mCars.size(); i++) {
                            if (mCars.get(i).getId() == car.getId()) {
                                mCars.set(i, car);
                                break;
                            }
                        }

                        // If all was ok we did get a RecyclerView position with the
                        // Intent data.
                        int position = (int)data.getExtras().get(Keys.EK_VIEWPOSITION);
                        if(position >= 0) {
                            // Notify the item change.
                            mCarAdapter.notifyItemChanged(position);
                        }
                    } else {
                        // Simply add the Car to the data.
                        mCars.add(car);

                        // We do not know the position of the item so we do a full
                        // rebind.
                        mCarAdapter.notifyDataSetChanged();
                    }
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
//        int id = item.getItemId();

        // All other items are not our's...
        return false;
    }
    //endregion
}
