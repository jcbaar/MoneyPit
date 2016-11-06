package com.development.jaba.moneypit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.model.SurroundingFillups;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.utilities.FormattingHelper;
import com.development.jaba.utilities.LocationHelper;
import com.development.jaba.utilities.PermissionHelper;
import com.development.jaba.utilities.SettingsHelper;
import com.development.jaba.view.EditTextEx;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddOrEditFillupActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    private final static int REQUEST_LOCATION_PERMISSION = 0;

    private MoneyPitDbContext mContext;
    private Fillup mFillupToEdit;
    private Car mCar;
    private SurroundingFillups mSurroundingFillups;
    private int mViewPosition = -1;
    private LocationTracker mLocHelp;
    private Calendar mCalendar;

    private final String PICKER = "DatePicker";

    // Bind the views.
    @Bind(R.id.fillupDate) Button mDate;
    @Bind(R.id.fillupOdo) EditTextEx mOdometer;
    @Bind(R.id.fillupVolume) EditTextEx mVolume;
    @Bind(R.id.fillupPrice) EditTextEx mPrice;
    @Bind(R.id.fillupRemark) EditTextEx mRemarks;
    @Bind(R.id.fillupFullTank) CheckedTextView mFullTank;

    /**
     * Saves the values from the UI into a {@link android.os.Bundle}.
     *
     * @param savedInstanceState The {@link android.os.Bundle} in which to save the values.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
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

    /**
     * The activity is resumed. Restore the {@link DatePickerDialog} it's callback.
     */
    @Override
    protected void onResume() {
        super.onResume();
        DatePickerDialog dpd = (DatePickerDialog) getFragmentManager().findFragmentByTag(PICKER);
        if (dpd != null) dpd.setOnDateSetListener(this);
    }

    /**
     * Handle the result of the permission request.
     *
     * @param requestCode The callback code.
     * @param permissions The requested permission(s).
     * @param grantResults The result for each requested permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (PermissionHelper.verifyPermissions(grantResults)) {
                mLocHelp = new LocationTracker(this);
                mLocHelp.startLocationTracking();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Requests for the permission to use the location services.
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Tell the user why we need the requested permissions.
            DialogHelper.showCallbackMessageDialog(getString(R.string.permission),
                    getString(R.string.location_permission),
                    new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ActivityCompat.requestPermissions(AddOrEditFillupActivity.this,
                                    new String[] {
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                    },
                                    REQUEST_LOCATION_PERMISSION);
                        }
                    }, this);
        }
        else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind views.
        ButterKnife.bind(this);

        int l1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int l2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        // Request the permissions.
        if (l1 != PackageManager.PERMISSION_GRANTED || l2 != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
        else {
            mLocHelp = new LocationTracker(this);
            mLocHelp.startLocationTracking();
        }

        // Instantiate a database context..
        mContext = new MoneyPitDbContext(this);

        // Instantiate Calendar instance.
        mCalendar = Calendar.getInstance();

        // Extract the Car instance if this Activity is called to edit
        // an existing Car entity. Otherwise we instantiate a new Car
        // entity.
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCar = (Car) b.getSerializable(Keys.EK_CAR);
            mFillupToEdit = (Fillup) b.getSerializable(Keys.EK_FILLUP);
            mViewPosition = b.getInt(Keys.EK_VIEWPOSITION);
        }

        // When we are called to create a new fill up we create an empty
        // instance here we use to edit.
        if (mFillupToEdit == null) {
            mFillupToEdit = new Fillup();

            // Link the new fill-up to the car.
            mFillupToEdit.setCarId(mCar.getId());
            mFillupToEdit.setDate(new Date());
        }

        // Load up the surrounding fill ups.
        mSurroundingFillups = mContext.getSurroundingFillups(mFillupToEdit.getDate(), mFillupToEdit.getCarId(), mFillupToEdit.getId());

        // Wire up the field validators.
        mOdometer.setValidator(new OdoValidator(this));
        mVolume.setValidator(new EditTextEx.RequiredValidator(this));
        mPrice.setValidator(new EditTextEx.RequiredValidator(this));

        // Setup the activity for either editing a new fill up
        // or editing an existing fill up.
        if (mFillupToEdit.getId() != 0) {
            toUi();
            setTitle(getString(R.string.title_edit_fillup));

            // When we are editing an existing fill up we do
            // not change the location!
        } else {
            mFullTank.setChecked(true);
            setTitle(getString(R.string.title_create_fillup));

            // If settings tell us to, estimate the odometer setting.
            if (getSettings().getBooleanValue(SettingsHelper.PREF_ESTIMATE_ODOMETER)) {
                Fillup before = mSurroundingFillups.getBefore();
                if (before != null) {
                    mFillupToEdit.setOdometer(mContext.getEstimatedOdometer(mCar.getId()));
                    mOdometer.setText(String.valueOf(Math.round(before.getOdometer() + mFillupToEdit.getOdometer())));
                }
            }
        }

        mDate.setText(FormattingHelper.toShortDate(mCalendar.getTime()));
        restoreState(savedInstanceState);
    }

    /**
     * Toggle's the {@link CheckedTextView} it's state.
     *
     * @param view The clicked {@link View}
     */
    @OnClick(R.id.fillupFullTank)
    public void check(View view) {
        mFullTank.toggle();
    }

    /**
     * Opens up the {@link DatePickerDialog} for the user to select a date.
     *
     * @param view The clicked {@link View}
     */
    @OnClick(R.id.fillupDate)
    public void selectDate(View view) {
        // Instantiate the dialog.
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                AddOrEditFillupActivity.this,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        );

        // We assume that "now" is the maximum date we can enter.
        Calendar max = Calendar.getInstance();

        // Should we clamp the possible date when editing a fill up?
        // For now we do not.
        //
        // When we are editing a fill up we clamp the possible date
        // selection between the previous fill up and the next fill up
        // relative to the edited fill up.
                /*
                if (mFillupToEdit.getId() != 0 && mSurroundingFillups != null) {
                    if (mSurroundingFillups.getBefore() != null) {
                        Calendar min = Calendar.getInstance();
                        min.setTime(mSurroundingFillups.getBefore().getDate());
                        dpd.setMinDate(min);
                    }
                    if (mSurroundingFillups.getAfter() != null) {
                        max.setTime(mSurroundingFillups.getAfter().getDate());
                    }
                }
                */
        dpd.setMaxDate(max);
        dpd.dismissOnPause(false);
        dpd.setThemeDark(!usingLightTheme());
        dpd.show(getFragmentManager(), PICKER);
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
            calDate.set(mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH),
                    calDate.get(Calendar.HOUR_OF_DAY),
                    calDate.get(Calendar.MINUTE),
                    calDate.get(Calendar.SECOND));
        } else {
            calDate.setTime(mFillupToEdit.getDate());
            calDate.set(mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH),
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
            mCalendar.setTime(DateHelper.fromDateTimeString(savedInstanceState.getString(Fillup.KEY_DATE)));
            mDate.setText(FormattingHelper.toShortDate(mCalendar.getTime()));
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
     * Copies the values from the {@link Fillup} entity we are editing to the
     * UI fields.
     */
    private void toUi() {
        mCalendar.setTime(mFillupToEdit.getDate());
        mDate.setText(FormattingHelper.toShortDate(mFillupToEdit.getDate()));
        mOdometer.setText(String.valueOf(Math.round(mFillupToEdit.getOdometer())));
        mVolume.setText(String.valueOf(mFillupToEdit.getVolume()));
        mPrice.setText(String.valueOf(mFillupToEdit.getPrice()));
        mRemarks.setText(mFillupToEdit.getNote());
        mFullTank.setChecked(mFillupToEdit.getFullTank());
    }

    /**
     * Copy the values from the UI fields to the {@link Fillup} entity we are editing.
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

    /**
     * Inflates the activity options menu.
     *
     * @param menu The {@link Menu} to inflate into.
     * @return Always true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ok_cancel, menu);
        return true;
    }

    /**
     * Handles the menu item selection.
     *
     * @param item The item the user selected.
     * @return true if handled, false if not handled.
     */
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
     * Store the edited {@link Fillup} into the database.
     *
     * @return true for success, false for failure.
     */
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

    /**
     * Callback of the {@link DatePickerDialog} which is called when the
     * user has selected a date.
     *
     * @param view        The source {@link DatePickerDialog}.
     * @param year        The selected year.
     * @param monthOfYear The selected month.
     * @param dayOfMonth  The selected day of the month.
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(year, monthOfYear, dayOfMonth,
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND));

        // Show in the date view and setup the surrounding fill up again.
        mDate.setText(FormattingHelper.toShortDate(mCalendar.getTime()));
        mSurroundingFillups = mContext.getSurroundingFillups(mCalendar.getTime(), mFillupToEdit.getCarId(), mFillupToEdit.getId());
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
