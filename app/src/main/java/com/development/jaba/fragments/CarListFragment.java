package com.development.jaba.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.adapters.CarRowAdapter;
import com.development.jaba.adapters.OnRecyclerItemClicked;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.CarAverage;
import com.development.jaba.moneypit.AddOrEditCarActivity;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.development.jaba.moneypit.TotalSummaryActivity;
import com.development.jaba.moneypit.VehicleDetailsActivity;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.view.RecyclerViewEx;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @SuppressWarnings("unused")
    @Bind(R.id.listEmpty) TextView mEmptyView;        // The text to show when the list is empty.
    @SuppressWarnings("unused")
    @Bind(R.id.carList) RecyclerViewEx mCarList;     // The list of vehicles.
    @SuppressWarnings("unused")
    @Bind(R.id.addFab) FloatingActionButton mFab;    // The floating action button.

    /**
     * Static factory method. Creates a new instance of this fragment.
     *
     * @return The created fragment.
     */
    public static CarListFragment newInstance() {
        return new CarListFragment();
    }

    /**
     * Called when the {@link android.app.Activity} is created.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Called when the {@link View} is created.
     *
     * @param inflater           The {@link android.view.LayoutInflater}.
     * @param container          The {@link android.view.ViewGroup}.
     * @param savedInstanceState The saved instance state/
     * @return The created {@link View}.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_drawer, container, false);
        ButterKnife.bind(this, view);

        mContext = new MoneyPitDbContext(getActivity());
        mCars = mContext.getAllCars();

        List<CarAverage> averages = mContext.getCarAverages();
        for (CarAverage avg : averages) {
            for (Car car : mCars) {
                if (car.getId() == avg.getCarId()) {
                    car.setAverages(avg);
                }
            }
        }

        mCarAdapter = new CarRowAdapter(getActivity(), mCars);
        mCarAdapter.setEmptyView(mEmptyView);
        mCarAdapter.setOnRecyclerItemClicked(new OnRecyclerItemClicked() {
            @Override
            public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
                if (!isLongClick) {
                    showCarDetails(mCarAdapter.getItem(position), position);
                }
                return false;
            }

            @Override
            public boolean onRecyclerItemMenuSelected(final int position, MenuItem item) {
                int menuItemIndex = item.getItemId();
                switch (menuItemIndex) {
                    case 0: {
                        Car selectedCar = mCarAdapter.getItem(position);
                        editCar(selectedCar, position);
                        return true;
                    }

                    case 1:
                        DialogHelper.showYesNoDialog(String.format(getString(R.string.dialog_delete_car_title), mCarAdapter.getLastClickedItem().toString()),
                                getText(R.string.dialog_delete_car_content),
                                new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        if (dialogAction == DialogAction.POSITIVE) {
                                            Car selectedCar = mCarAdapter.getItem(position);
                                            mContext.deleteCar(selectedCar);
                                            mCars.remove(selectedCar);
                                            mCarAdapter.notifyItemRemoved(position);
                                        }
                                    }
                                },
                                getActivity());
                        return true;

                    case 2:
                        Intent summary = new Intent(getActivity(), TotalSummaryActivity.class);
                        Car selectedCar = mCarAdapter.getItem(position);
                        summary.putExtra(Keys.EK_CAR, selectedCar);
                        ActivityCompat.startActivity(getActivity(), summary, getSceneBundle(position));
                        return true;

                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onExpansionStateChanged(int position, boolean isExpanded) {

            }
        });

        mCarList.setAdapter(mCarAdapter);
        mCarList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCarList.setHasFixedSize(true);
        return view;
    }

    /**
     * Adds a new {@link Car}
     *
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.addFab)
    public void onClick() {
        editCar(null, -1);
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

    /**
     * Returns a scene transition bundle which will animate the car picture.
     * @param position The position of the item in the {@link RecyclerViewEx} to animate.
     * @return The scene animation {@link Bundle} or null when running lower than Lollipop.
     */
    private Bundle getSceneBundle(int position) {
        Bundle trans = null;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CarRowAdapter.CarRowViewHolder vh = (CarRowAdapter.CarRowViewHolder) mCarList.findViewHolderForLayoutPosition(position);
            if(vh != null) {

                trans = ActivityOptions.makeSceneTransitionAnimation(getActivity(), vh.getImage(), "carImage").toBundle();
            }
        }
        return trans;
    }

    /**
     * Open up the {@link Car} details activity for the given car.
     *
     * @param car The {@link Car} which to show in the details page.
     * @param position The position of the {@link RecyclerViewEx} to show.
     */
    private void showCarDetails(Car car, int position) {
        Intent carDetails = new Intent(getActivity(), VehicleDetailsActivity.class);
        if (car != null) {
            carDetails.putExtra(Keys.EK_CAR, car);
        }
        ActivityCompat.startActivity(getActivity(), carDetails, getSceneBundle(position));
    }

    /**
     * Open up the {@link Car} edit activity for the given car.
     *
     * @param car      The {@link Car} which to edit or null to create a new car.
     * @param position The position in the {@link android.support.v7.widget.RecyclerView} the item has or -1 in case of a new item.
     */
    private void editCar(Car car, int position) {
        Intent editCar = new Intent(getActivity(), AddOrEditCarActivity.class);
        if (car != null) {
            editCar.putExtra(Keys.EK_CAR, car);
            editCar.putExtra(Keys.EK_VIEWPOSITION, position);
        }
        ActivityCompat.startActivityForResult(getActivity(), editCar, car == null ? REQUEST_ADD_CAR : REQUEST_EDIT_CAR, getSceneBundle(position));
    }

    /**
     * Evaluates the result for the {@link android.app.Activity}.
     *
     * @param requestCode The request code that is giving the result.
     * @param resultCode  The result code.
     * @param data        The data of the result.
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
                        int position = (int) data.getExtras().get(Keys.EK_VIEWPOSITION);
                        if (position >= 0) {
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
}
