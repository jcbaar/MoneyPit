package com.development.jaba.moneypit;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.DatePicker;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.model.SurroundingFillups;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.utilities.LocationHelper;
import com.development.jaba.utilities.SettingsHelper;
import com.development.jaba.view.EditTextEx;

import java.util.Calendar;
import java.util.Date;

public class AddOrEditFillupActivity extends BaseActivity {

    private MoneyPitDbContext mContext;
    private Fillup mFillupToEdit;
    private Car mCar;
    private DatePicker mDate;
    private EditTextEx mOdometer,
            mVolume,
            mPrice,
            mRemarks;
    private CheckedTextView mFullTank;
    private SurroundingFillups mSurroundingFillups;
    private int mViewPosition = -1;
    private LocationTracker mLocHelp;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveState(savedInstanceState);
    }

    /**
     * Makes sure the {@link com.development.jaba.moneypit.BaseActivity} knows which layout to inflate.
     *
     * @return The resource ID of the layout to inflate.
     */
    protected int getLayoutResource() {
        return R.layout.activity_add_or_edit_fillup;
    }

    /**
     * The activity is paused. We need to turn off
     * location tracking here since there is no point in
     * keeping it active.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mLocHelp != null) {
            mLocHelp.stopLocationTracking();
            mLocHelp = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate a database context..
        mContext = new MoneyPitDbContext(this);

        // Extract the Car instance if this Activity is called to edit
        // an existing Car entity. Otherwise we instantiate a new Car
        // entity.
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCar = (Car) b.getSerializable(Keys.EK_CAR);
            mFillupToEdit = (Fillup) b.getSerializable(Keys.EK_FILLUP);
            mViewPosition = b.getInt(Keys.EK_VIEWPOSITION);
        }

        if (mFillupToEdit == null) {
            mFillupToEdit = new Fillup();

            // Link the new fill-up to the car.
            mFillupToEdit.setCarId(mCar.getId());
            mFillupToEdit.setDate(new Date());
        }

        mSurroundingFillups = mContext.getSurroundingFillups(mFillupToEdit.getDate(), mFillupToEdit.getCarId(), mFillupToEdit.getId());

        mDate = (DatePicker) findViewById(R.id.fillupDate);
        mOdometer = (EditTextEx) findViewById(R.id.fillupOdo);
        mVolume = (EditTextEx) findViewById(R.id.fillupVolume);
        mPrice = (EditTextEx) findViewById(R.id.fillupPrice);
        mRemarks = (EditTextEx) findViewById(R.id.fillupRemark);
        mFullTank = (CheckedTextView) findViewById(R.id.fillupFullTank);

        mFullTank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullTank.toggle();
            }
        });

        mOdometer.setValidator(new OdoValidator(this));
        mVolume.setValidator(new EditTextEx.RequiredValidator(this));
        mPrice.setValidator(new EditTextEx.RequiredValidator(this));

        if (mFillupToEdit.getId() != 0) {
            toUi();
            setTitle(getString(R.string.title_edit_fillup));
        } else {
            setupDate(new Date());
            mFullTank.setChecked(true);
            setTitle(getString(R.string.title_create_fillup));

            // If settings tell us to, estimate the odometer setting.
            SettingsHelper settings = new SettingsHelper(this);
            if (settings.getEstimateOdometer()) {
                Fillup before = mSurroundingFillups.getBefore();
                if (before != null) {
                    mFillupToEdit.setOdometer(mContext.getEstimatedOdometer(mCar.getId()));
                    mOdometer.setText(String.valueOf(Math.round(before.getOdometer() + mFillupToEdit.getOdometer())));
                }
            }

            // When allowed to do so we try to also record the
            // location of the fill-up.
            if (settings.getAllowLocation()) {
                mLocHelp = new LocationTracker(this);
                mLocHelp.startLocationTracking();
            }
        }
        restoreState(savedInstanceState);
    }

    /**
     * Setup the given date in the {@link android.widget.DatePicker}.
     *
     * @param d The date to set in the {@link android.widget.DatePicker}.
     */
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
     * Converts the selected date into a {@link java.util.Date}. When the fill-up currently
     * being edited is one that is already in the database we use it's time. When it is a new
     * fill-up being edited we use the current system time.
     *
     * @return The {@link java.util.Date}.
     */
    private Date getDate() {
        Calendar calDate = Calendar.getInstance();

        if (mFillupToEdit.getId() == 0) {
            calDate.setTime(new Date());
            calDate.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(),
                    calDate.get(Calendar.HOUR_OF_DAY),
                    calDate.get(Calendar.MINUTE),
                    calDate.get(Calendar.SECOND));
        } else {
            calDate.setTime(mFillupToEdit.getDate());
            calDate.set(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(),
                    calDate.get(Calendar.HOUR_OF_DAY),
                    calDate.get(Calendar.MINUTE),
                    calDate.get(Calendar.SECOND));
        }
        return calDate.getTime();
    }

    /**
     * Restores the values from a saved instance state back into the UI.
     *
     * @param savedInstanceState The {@link android.os.Bundle} containing the saved values.
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            setupDate(DateHelper.fromDateTimeString(savedInstanceState.getString(Fillup.KEY_DATE)));
            mOdometer.setText(savedInstanceState.getString(Fillup.KEY_ODOMETER));
            mVolume.setText(savedInstanceState.getString(Fillup.KEY_VOLUME));
            mPrice.setText(savedInstanceState.getString(Fillup.KEY_PRICE));
            mRemarks.setText(savedInstanceState.getString(Fillup.KEY_NOTE));
            mFullTank.setChecked(savedInstanceState.getBoolean(Fillup.KEY_FULLTANK));
            mFillupToEdit.setLatitude(savedInstanceState.getDouble(Fillup.KEY_LATITUDE));
            mFillupToEdit.setLongitude(savedInstanceState.getDouble(Fillup.KEY_LONGITUDE));
        }
    }

    /**
     * Saves the values from the UI into a {@link android.os.Bundle}.
     *
     * @param savedInstanceState The {@link android.os.Bundle} in which to save the values.
     */
    private void saveState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putString(Fillup.KEY_DATE, DateHelper.toDateTimeString(getDate()));
            savedInstanceState.putString(Fillup.KEY_ODOMETER, mOdometer.getText().toString());
            savedInstanceState.putString(Fillup.KEY_VOLUME, mVolume.getText().toString());
            savedInstanceState.putString(Fillup.KEY_PRICE, mPrice.getText().toString());
            savedInstanceState.putString(Fillup.KEY_NOTE, mRemarks.getText().toString());
            savedInstanceState.putBoolean(Fillup.KEY_FULLTANK, mFullTank.isChecked());
            savedInstanceState.putDouble(Fillup.KEY_LATITUDE, mFillupToEdit.getLatitude());
            savedInstanceState.putDouble(Fillup.KEY_LONGITUDE, mFillupToEdit.getLongitude());
        }
    }

    /**
     * Copies the values from the {@link Fillup} entity we are editing to the
     * UI fields.
     */
    private void toUi() {
        setupDate(mFillupToEdit.getDate());
        mOdometer.setText(String.valueOf(Math.round(mFillupToEdit.getOdometer())));
        mVolume.setText(String.valueOf(mFillupToEdit.getVolume()));
        mPrice.setText(String.valueOf(mFillupToEdit.getPrice()));
        mRemarks.setText(mFillupToEdit.getNote());
        mFullTank.setChecked(mFillupToEdit.getFullTank());
    }

    /**
     * Copy the values from the UI to the Car entity we are editing.
     */
    private void fromUi() {
        mFillupToEdit.setDate(getDate());
        mFillupToEdit.setFullTank(mFullTank.isChecked());
        mFillupToEdit.setOdometer(Double.parseDouble(mOdometer.getText().toString()));
        mFillupToEdit.setVolume(Double.parseDouble(mVolume.getText().toString()));
        mFillupToEdit.setPrice(Double.parseDouble(mPrice.getText().toString()));
        mFillupToEdit.setNote(mRemarks.getText().toString());
    }

    /**
     * Validate all fields in the UI that require validation.
     *
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

    private boolean addOrUpdateFillup() {
        // Validate the fields before we continue.
        boolean isOk = validateFields();
        if (isOk) {
            // Copy the UI contents into the Fill-up entity.
            fromUi();

            // When we have an ID of 0 here it means that this is a new
            // Fill-up entity.
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
                // Update the Fill-up entity in the database.
                isOk = mContext.updateFillup(mFillupToEdit);
            }

            if (isOk) {
                // We have a successful database transaction. Return the Fill-up entity
                // to the calling Activity.
                Intent result = new Intent();
                result.putExtra(Keys.EK_FILLUP, mFillupToEdit);
                result.putExtra(Keys.EK_VIEWPOSITION, mViewPosition);
                if (getParent() == null) {
                    setResult(RESULT_OK, result);
                } else {
                    getParent().setResult(RESULT_OK, result);
                }
                finish();
            } else {
                DialogHelper.showMessageDialog(getString(R.string.dialog_error_title), getString(R.string.error_saving_fillup), this);
            }
        }
        return isOk;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_ok) {
            addOrUpdateFillup();
            return true;
        } else if (id == R.id.action_cancel) {
            if (getParent() == null) {
                setResult(RESULT_CANCELED);
            } else {
                getParent().setResult(RESULT_CANCELED);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Validator class to validate the entered odometer value.
     */
    private class OdoValidator extends EditTextEx.BaseValidator {

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         */
        public OdoValidator(Context context) {
            super(context);
        }

        /**
         * Checks if the value is between the odometer settings of the previous fill-up
         * and the next fill-up.
         *
         * @param value The value to validate.
         * @return true if the value is valid, false if it was not.
         */
        @Override
        public boolean isValid(String value) {
            // We need something to parse...
            if (TextUtils.isEmpty(value)) {
                setErrorMessage(R.string.no_text_error);
                return false;
            }

            double odo;
            try {
                odo = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // Not a valid double string.
                setErrorMessage(R.string.nan);
                return false;
            }

            // Make sure the odometer value is between the previous and next fill up
            // odometer settings.
            if ((mSurroundingFillups.getBefore() != null && odo <= mSurroundingFillups.getBefore().getOdometer()) ||
                    (mSurroundingFillups.getAfter() != null && odo >= mSurroundingFillups.getAfter().getOdometer())) {
                setErrorMessage(R.string.odo_error);
                return false;
            }
            return true;
        }
    }

    /**
     * A {@link com.development.jaba.utilities.LocationHelper} derived class to record the fill-up
     * location.
     */
    private class LocationTracker extends LocationHelper {

        public LocationTracker(Context context) {
            super(context);
        }

        @Override
        public void onLocationChanged(Location location) {
            super.onLocationChanged(location);
            mFillupToEdit.setLatitude(location.getLatitude());
            mFillupToEdit.setLongitude(location.getLongitude());
        }
    }
}
