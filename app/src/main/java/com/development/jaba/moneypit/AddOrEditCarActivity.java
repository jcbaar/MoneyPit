package com.development.jaba.moneypit;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.MenuItem;
import android.widget.Toast;

import com.development.jaba.model.Car;
import com.development.jaba.model.DistanceUnit;
import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.VolumeUnit;
import com.development.jaba.view.EditTextEx;

import static com.development.jaba.view.EditTextEx.*;

/**
 * Activity for creating a new Car entity or editing an existing
 * entity.
 */
public class AddOrEditCarActivity extends ActionBarActivity {

    private Spinner distanceUnits,
                    volumeUnits;

    private EditTextEx make,
                     model,
                     buildYear,
                     license,
                     currency;

    private MoneyPitDbContext context;
    private Car carToEdit;

    /**
     * Called when the Activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_car);

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
            carToEdit = (Car)b.getSerializable("Car");
        }
        else {
            carToEdit = new Car();
        }

        // Instantiate a database context..
        context = new MoneyPitDbContext(this);

        // Get the TextView objects..
        make = (EditTextEx) findViewById(R.id.carBrand);
        model = (EditTextEx) findViewById(R.id.carModel);
        buildYear = (EditTextEx) findViewById(R.id.carBuildYear);
        license = (EditTextEx) findViewById(R.id.carLicense);
        currency = (EditTextEx) findViewById(R.id.carCurrency);

        // And the Spinners.
        distanceUnits = (Spinner) findViewById(R.id.carDistanceUnit);
        volumeUnits = (Spinner) findViewById(R.id.carVolumeUnit);

        // Load up the contents of the Spinners.
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this, R.array.distance_units, R.layout.spinner_row_template);
        ArrayAdapter<CharSequence> volumeAdapter = ArrayAdapter.createFromResource(this, R.array.volume_units, R.layout.spinner_row_template);

        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        distanceUnits.setAdapter(distanceAdapter);
        volumeUnits.setAdapter(volumeAdapter);

        // Setup the validation listeners.
        make.setValidator(new EditTextEx.RequiredValidator(this));
        model.setValidator(new EditTextEx.RequiredValidator(this));
        license.setValidator(new EditTextEx.RequiredValidator(this));
        currency.setValidator(new EditTextEx.RequiredValidator(this));

        buildYear.setValidator(new BuildYearValidator(this));

        // Setup the activity title depending on whether we are editing and
        // existing Car entity or a new one.
        if(carToEdit.getId() != 0)
        {
            toUi();
            setTitle(getString(R.string.title_edit_car));
        }
        else {
            setTitle(getString(R.string.title_create_car));
        }
    }

    /**
     * Copies the values from the Car entity we are editing to the
     * UI fields.
     */
    private void toUi() {
        make.setText(carToEdit.getMake());
        model.setText(carToEdit.getModel());
        buildYear.setText(String.valueOf(carToEdit.getBuildYear()));
        license.setText(carToEdit.getLicensePlate());
        currency.setText(carToEdit.getCurrency());
        distanceUnits.setSelection(carToEdit.getDistanceUnit().getValue() - 1);
        volumeUnits.setSelection(carToEdit.getVolumeUnit().getValue() - 1);
    }

    /**
     * Copy the values from the UI to the Car entity we are editing.
     */
    private void fromUi() {
        carToEdit.setMake(make.getText().toString());
        carToEdit.setModel(model.getText().toString());
        carToEdit.setBuildYear(Integer.parseInt(buildYear.getText().toString()));
        carToEdit.setLicensePlate(license.getText().toString());
        carToEdit.setCurrency(currency.getText().toString());
        carToEdit.setDistanceUnit(DistanceUnit.fromValue(distanceUnits.getSelectedItemPosition()+1));
        carToEdit.setVolumeUnit(VolumeUnit.fromValue(volumeUnits.getSelectedItemPosition()+1));
    }

    /**
     * Validate all fields in the UI that require validation.
     * @return true for a successful validation. false if one or more
     * of the fields failed validation.
     */
    private boolean validateFields() {
        return make.validate() &&
                model.validate() &&
                buildYear.validate() &&
                license.validate() &&
                currency.validate();
    }

    /**
     * Called to create the Activity menu.
     * @param menu The menu.
     * @return true to show the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok_cancel, menu);
        return true;
    }

    /**
     * Callback the is called when the user has selected an item from the options
     * menu.
     * @param item The item the user selected.
     * @return true when the item selection was handled, false when not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            // Validate the fields before we continue.
            boolean isOk = validateFields();
            if(isOk) {
                // Copy the UI contents into the Car entity.
                fromUi();

                // When we have an ID of 0 here it means that this is a new
                // Car entity.
                if (carToEdit.getId() == 0) {
                    // Add the Car to the database and store it's ID.
                    int carId = (int) context.addCar(carToEdit);
                    if (carId != 0) {
                        carToEdit.setId(carId);
                    } else {
                        // That failed for some reason...
                        isOk = false;
                    }
                } else {
                    // Update the car entity in the database.
                    isOk = context.updateCar(carToEdit);
                }

                if (isOk) {
                    // We have a successful database transaction. Return the Car entity
                    // to the calling Activity.
                    Intent result = new Intent();
                    result.putExtra("Car", carToEdit);
                    if(getParent() == null) {
                        setResult(RESULT_OK, result);
                    }
                    else {
                        getParent().setResult(RESULT_OK, result);
                    }
                    finish();
                } else {
                    // TODO: Report this with a dialog instead of a toast.
                    Toast.makeText(this, getString(R.string.error_saving_car), Toast.LENGTH_SHORT).show();
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
            NavUtils.navigateUpTo(this, intent);        }
        return super.onOptionsItemSelected(item);
    }
}