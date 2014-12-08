package com.development.jaba.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.development.jaba.model.Car;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.moneypit.AddOrEditCarActivity;
import com.development.jaba.adapters.CarRowAdapter;
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
    private ListAdapter mCarAdapter;                 // Adapter for holding the Car list.
    private ListView mCarList;                       // The car ListView.
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

        mCarAdapter = new CarRowAdapter(getActivity(), mCars);
        mCarList = (ListView) view.findViewById(R.id.carList);
        mCarList.setEmptyView(view.findViewById(R.id.listEmpty));

        mCarList.setAdapter(mCarAdapter);
        mCarList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editCar((Car) mCarAdapter.getItem(position));
            }
        });

        registerForContextMenu(mCarList);
        return view;
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
                    ((ArrayAdapter)mCarAdapter).notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.carList) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            Car selectedCar = (Car) mCarAdapter.getItem(info.position);
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
                Car selectedCar = (Car) mCarAdapter.getItem(info.position);
                editCar(selectedCar);
                return true;
            }

            case 1:
                Car selectedCar = (Car) mCarAdapter.getItem(info.position);
                mContext.deleteCar(selectedCar);
                mCars.remove(selectedCar);
                ((ArrayAdapter)mCarAdapter).notifyDataSetChanged();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_carlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_car) {
            editCar(null);
            return true;
        }
        return false;
    }
}
