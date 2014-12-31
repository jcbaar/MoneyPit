package com.development.jaba.moneypit;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Toast;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.view.EditTextEx;

import java.util.Calendar;
import java.util.Date;


public class AddOrEditFillupActivity extends ActionBarActivity {

    private MoneyPitDbContext mContext;
    private Fillup mFillupToEdit;
    private Car mCar;
    private DatePicker mDate;
    private EditTextEx mOdometer,
            mVolume,
            mPrice,
            mRemarks;
    private CheckBox mFullTank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_fillup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Extract the Car instance if this Activity is called to edit
        // an existing Car entity. Otherwise we instantiate a new Car
        // entity.
        Bundle b = getIntent().getExtras();
        if( b != null) {
            mCar = (Car)b.getSerializable("Car");
            mFillupToEdit = (Fillup)b.getSerializable("Fillup");
        }

        if(mFillupToEdit == null) {
            mFillupToEdit = new Fillup();

            // Link the new fill-up to the car.
            mFillupToEdit.setCarId(mCar.getId());
        }

        // Instantiate a database context..
        mContext = new MoneyPitDbContext(this);

        mDate = (DatePicker)findViewById(R.id.fillupDate);
        mOdometer = (EditTextEx) findViewById(R.id.fillupOdo);
        mVolume = (EditTextEx) findViewById(R.id.fillupVolume);
        mPrice = (EditTextEx) findViewById(R.id.fillupPrice);
        mRemarks = (EditTextEx) findViewById(R.id.fillupRemark);
        mFullTank = (CheckBox) findViewById(R.id.fillupFullTank);

        mOdometer.setValidator(new EditTextEx.RequiredValidator(this));
        mVolume.setValidator(new EditTextEx.RequiredValidator(this));
        mPrice.setValidator(new EditTextEx.RequiredValidator(this));

        if(mFillupToEdit.getId() != 0) {
            toUi();
            setTitle(getString(R.string.title_edit_fillup));
        }
        else {
            setupDate(new Date());
            mFullTank.setChecked(true);
            setTitle(getString(R.string.title_create_fillup));
        }
    }

    private void setupDate(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        int year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH),
                day = cal.get(Calendar.DAY_OF_MONTH);

        mDate.updateDate(year, month, day);
        mDate.setMaxDate(new Date().getTime());
    }

    /**
     * Copies the values from the Fillup entity we are editing to the
     * UI fields.
     */
    private void toUi() {
        setupDate(mFillupToEdit.getDate());
        mOdometer.setText(String.valueOf(mFillupToEdit.getOdometer()));
        mVolume.setText(String.valueOf(mFillupToEdit.getVolume()));
        mPrice.setText(String.valueOf(mFillupToEdit.getPrice()));
        mRemarks.setText(mFillupToEdit.getNote());
//        mFullTank.setChecked(mFillupToEdit.getFullTank()));
    }

    /**
     * Copy the values from the UI to the Car entity we are editing.
     */
    private void fromUi() {
        Calendar calDate = Calendar.getInstance();

        if(mFillupToEdit.getId() == 0) {
            calDate.setTime(new Date());
            calDate.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(),
                    calDate.get(Calendar.HOUR_OF_DAY),
                    calDate.get(Calendar.MINUTE),
                    calDate.get(Calendar.SECOND));
        }
        else {
            calDate.setTime(mFillupToEdit.getDate());
            calDate.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(),
                    calDate.get(Calendar.HOUR_OF_DAY),
                    calDate.get(Calendar.MINUTE),
                    calDate.get(Calendar.SECOND));
        }

        mFillupToEdit.setDate(calDate.getTime());
        mFillupToEdit.setFullTank(mFullTank.isChecked());
        mFillupToEdit.setOdometer(Double.parseDouble(mOdometer.getText().toString()));
        mFillupToEdit.setVolume(Double.parseDouble(mVolume.getText().toString()));
        mFillupToEdit.setPrice(Double.parseDouble(mPrice.getText().toString()));
        mFillupToEdit.setNote(mRemarks.getText().toString());
    }

    /**
     * Validate all fields in the UI that require validation.
     * @return true for a successful validation. false if one or more
     * of the fields failed validation.
     */
    private boolean validateFields() {
        return mOdometer.validate() &&
                mVolume.validate() &&
                mPrice.validate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ok_cancel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_ok) {
            // Validate the fields before we continue.
            boolean isOk = validateFields();
            if(isOk) {
                // Copy the UI contents into the Car entity.
                fromUi();

                // When we have an ID of 0 here it means that this is a new
                // Fillup entity.
                if (mFillupToEdit.getId() == 0) {
                    // Add the Fillup to the database and store it's ID.
                    int fillupId = (int) mContext.addFillup(mFillupToEdit);
                    if (fillupId != 0) {
                        mFillupToEdit.setId(fillupId);
                    } else {
                        // That failed for some reason...
                        isOk = false;
                    }
                } else {
                    // Update the car entity in the database.
                    isOk = mContext.updateFillup(mFillupToEdit);
                }

                if (isOk) {
                    // We have a successful database transaction. Return the Car entity
                    // to the calling Activity.
                    Intent result = new Intent();
                    result.putExtra("Fillup", mFillupToEdit);
                    if(getParent() == null) {
                        setResult(RESULT_OK, result);
                    }
                    else {
                        getParent().setResult(RESULT_OK, result);
                    }
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.error_saving_fillup), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (id == R.id.action_cancel) {
            if(getParent() == null) {
                setResult(RESULT_CANCELED);
            }
            else {
                getParent().setResult(RESULT_CANCELED);
            }
            finish();
        }
        else if(id == android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
