package com.development.jaba.moneypit;

import android.os.Bundle;

import com.development.jaba.fragments.BaseDetailsFragment;
import com.development.jaba.fragments.CarDetailsSummaryFragment;
import com.development.jaba.model.Car;

/**
 * Simple settings activity. Loads up the {@link com.development.jaba.fragments.SettingsFragment}.
 */
public class TotalSummaryActivity extends BaseActivity {

    private Car mCar;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(Keys.EK_CAR, mCar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
        {
            mCar = (Car)savedInstanceState.getSerializable(Keys.EK_CAR);
        }
        else {
            // Extract the Car instance if this Activity is called to edit
            // an existing Car entity. Otherwise we instantiate a new Car
            // entity.
            Bundle b = getIntent().getExtras();
            if (b != null) {
                mCar = (Car) b.getSerializable(Keys.EK_CAR);
            }
        }
        BaseDetailsFragment fragment = CarDetailsSummaryFragment.newInstance(mCar, CarDetailsSummaryFragment.class);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_totalsummary;
    }
}
