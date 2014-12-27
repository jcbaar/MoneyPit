package com.development.jaba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.model.Car;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.CarAverage;
import com.development.jaba.moneypit.AddOrEditCarActivity;
import com.development.jaba.adapters.CarRowAdapter;
import com.development.jaba.moneypit.CarDetailsActivity;
import com.development.jaba.moneypit.R;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
        });

        RecyclerView carList = (RecyclerView) view.findViewById(R.id.carList);
        carList.setAdapter(mCarAdapter);
        carList.setLayoutManager(new LinearLayoutManager(getActivity()));

        registerForContextMenu(carList);
        return view;
    }

    private void showCarDetails(Car car) {
        Intent carDetails = new Intent(getActivity(), CarDetailsActivity.class);
        if(car != null) {
            carDetails.putExtra("Car", car);
        }
        startActivity(carDetails);
    }

    private void editCar(Car car) {
        Intent editCar = new Intent(getActivity(), AddOrEditCarActivity.class);
        if(car != null) {
            editCar.putExtra("Car", car);
        }
        startActivityForResult(editCar, car == null ? REQUEST_ADD_CAR : REQUEST_EDIT_CAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getExtras().containsKey("Car")) {
                Car car = (Car) data.getExtras().get("Car");
                if (car != null) {
                    if (requestCode == REQUEST_EDIT_CAR) {
                        for (int i = 0; i < mCars.size(); i++) {
                            if (mCars.get(i).getId() == car.getId()) {
                                mCars.set(i, car);
                                break;
                            }
                        }
                    } else {
                        mCars.add(car);
                    }
                    mCarAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.carList) {
            // The RecyclerView does not give you the item for which the context menu must be built.
            // Therefore we need to get the last item clicked from the adapter so we can get to the
            // data we need.
            Car selectedCar = mCarAdapter.getItem(mCarAdapter.getLastClickedPosition());
            menu.setHeaderTitle(selectedCar.toString());
            String[] menuItems = getResources().getStringArray(R.array.edit_delete);
            for(int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch(menuItemIndex) {
            case 0:
            {
                Car selectedCar = mCarAdapter.getItem(info.position);
                editCar(selectedCar);
                return true;
            }

            case 1:
                Car selectedCar = mCarAdapter.getItem(info.position);
                mContext.deleteCar(selectedCar);
                mCars.remove(selectedCar);
                mCarAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_car) {
            editCar(null);
            return true;
        }

        // All other items are not our's...
        return false;
    }
    //endregion
}
