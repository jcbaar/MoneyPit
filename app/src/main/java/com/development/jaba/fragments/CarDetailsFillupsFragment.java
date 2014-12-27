package com.development.jaba.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.development.jaba.adapters.FillupRowAdapter;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.database.Utils;
import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.R;

import java.util.Date;
import java.util.List;

/**
 * Fragment for displaying the details of a {@link Car}.
 */
public class CarDetailsFillupsFragment extends BaseFragment {

    private MoneyPitDbContext mContext;              // The MoneyPit database mContext.
    private FillupRowAdapter mFillupAdapter;              // Adapter for holding the Fill-up list.
    private List<Fillup> mFillups;                   // The list of Fillup entities from the database.
    private Car mCar;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details_fillups, container, false);

        mContext = new MoneyPitDbContext(getActivity());
        if(mCar != null) {
            mFillups = mContext.getFillupsOfCar(mCar.getId(), Utils.getYearFromDate(new Date()));

            mFillupAdapter = new FillupRowAdapter(getActivity(), mFillups);
            mFillupAdapter.setCar(mCar);

            ListView fillupList = (ListView) view.findViewById(R.id.fillupList);
            fillupList.setEmptyView(view.findViewById(R.id.fillupListEmpty));

            fillupList.setAdapter(mFillupAdapter);
            fillupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //showCarDetails((Car) mFillupAdapter.getItem(position));
                }
            });
            registerForContextMenu(fillupList);
        }
        return view;
    }

/*    private void showCarDetails(Car car) {
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
                    ((ArrayAdapter) mFillupAdapter).notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.carList) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            Car selectedCar = (Car) mFillupAdapter.getItem(info.position);
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
                Car selectedCar = (Car) mFillupAdapter.getItem(info.position);
                editCar(selectedCar);
                return true;
            }

            case 1:
                Car selectedCar = (Car) mFillupAdapter.getItem(info.position);
                mContext.deleteCar(selectedCar);
                mCars.remove(selectedCar);
                ((ArrayAdapter) mFillupAdapter).notifyDataSetChanged();
                return true;
        }
        return false;
    }*/

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
