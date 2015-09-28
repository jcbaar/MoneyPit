package com.development.jaba.moneypit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.DistanceUnit;
import com.development.jaba.model.VolumeUnit;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.utilities.GetCarImageHelper;
import com.development.jaba.view.EditTextEx;
import com.development.jaba.view.RecyclingImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.development.jaba.view.EditTextEx.BaseValidator;

/**
 * Activity for creating a new Car entity or editing an existing
 * entity.
 */
public class AddOrEditCarActivity extends BaseActivity {

    private final static int REQUEST_GET_PICTURE = 1;
    private final static int REQUEST_TAKE_PHOTO = 2;

    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";

    private Spinner mDistanceUnits,
            mVolumeUnits;

    private RecyclingImageView mImage;

    private EditTextEx mMake,
            mModel,
            mBuildYear,
            mLicense,
            mCurrency;

    private MoneyPitDbContext context;
    private Car mCarToEdit;
    private int mViewPosition = -1;

    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;

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
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_add_or_edit_car;
    }

    /**
     * Called when the Activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Extract the Car instance if this Activity is called to edit
        // an existing Car entity. Otherwise we instantiate a new Car
        // entity.
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCarToEdit = (Car) b.getSerializable(Keys.EK_CAR);
            mViewPosition = b.getInt(Keys.EK_VIEWPOSITION);
        } else {
            mCarToEdit = new Car();
        }

        // Instantiate a database context..
        context = new MoneyPitDbContext(this);

        // Get the TextView objects..
        mMake = (EditTextEx) findViewById(R.id.carBrand);
        mModel = (EditTextEx) findViewById(R.id.carModel);
        mBuildYear = (EditTextEx) findViewById(R.id.carBuildYear);
        mLicense = (EditTextEx) findViewById(R.id.carLicense);
        mCurrency = (EditTextEx) findViewById(R.id.carCurrency);
        mImage = (RecyclingImageView) findViewById(R.id.imageView);

        // Color the image buttons.
        ImageButton pictureRoll = (ImageButton) findViewById(R.id.pictureRoll);
        ImageButton pictureCamera = (ImageButton) findViewById(R.id.pictureCamera);
        ImageButton pictureDelete = (ImageButton) findViewById(R.id.pictureDelete);

        setButtonImageColor(pictureRoll, R.drawable.ic_camera_roll_grey600_24dp);
        setButtonImageColor(pictureCamera, R.drawable.ic_photo_camera_grey600_24dp);
        setButtonImageColor(pictureDelete, R.drawable.ic_delete_grey600_24dp);

        // And the Spinners.
        mDistanceUnits = (Spinner) findViewById(R.id.carDistanceUnit);
        mVolumeUnits = (Spinner) findViewById(R.id.carVolumeUnit);

        // Load up the contents of the Spinners.
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this, R.array.distance_units, R.layout.spinner_row_template);
        ArrayAdapter<CharSequence> volumeAdapter = ArrayAdapter.createFromResource(this, R.array.volume_units, R.layout.spinner_row_template);

        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDistanceUnits.setAdapter(distanceAdapter);
        mVolumeUnits.setAdapter(volumeAdapter);

        // Setup the validation listeners.
        mMake.setValidator(new EditTextEx.RequiredValidator(this));
        mModel.setValidator(new EditTextEx.RequiredValidator(this));
        mLicense.setValidator(new LicensePlateValidator(this));
        mCurrency.setValidator(new EditTextEx.RequiredValidator(this));
        mBuildYear.setValidator(new BuildYearValidator(this));

        // Restore any saved state.
        restoreState(savedInstanceState);

        // Setup the activity title depending on whether we are editing and
        // existing Car entity or a new one.
        if (mCarToEdit.getId() != 0) {
            // When there is no saved state we setup the values from the
            // passed in instance to the UI.
            if(savedInstanceState == null) {
                toUi();
            }
            setTitle(getString(R.string.title_edit_car));
        } else {
            setTitle(getString(R.string.title_create_car));
        }
    }

    /**
     * Setup a drawable in the given ImageButton and color it appropriately.
     * @param button The {@link android.widget.ImageButton} to set the  image onto.
     * @param resId The resource ID of the drawable to set on the {@link android.widget.ImageButton}
     */
    private void setButtonImageColor(ImageButton button, int resId) {
        Drawable d = ContextCompat.getDrawable(this, resId);
        if(d != null) {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;

            // TODO: Using R.color.abc_primary_text_material_dark will not work properly when styling is applied.
            d.mutate().setColorFilter(ContextCompat.getColor(this, R.color.abc_primary_text_material_dark), mode);
            button.setImageDrawable(d);
        }
    }

    /**
     * Restores the values from a saved instance state back into the UI.
     *
     * @param savedInstanceState The {@link android.os.Bundle} containing the saved values.
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMake.setText(savedInstanceState.getString(Car.KEY_MAKE));
            mModel.setText(savedInstanceState.getString(Car.KEY_MODEL));
            mBuildYear.setText(savedInstanceState.getString(Car.KEY_BUILDYEAR));
            mLicense.setText(savedInstanceState.getString(Car.KEY_LICENSEPLATE));
            mCurrency.setText(savedInstanceState.getString(Car.KEY_CURRENCY));
            mDistanceUnits.setSelection(savedInstanceState.getInt(Car.KEY_DISTANCEUNIT));
            mVolumeUnits.setSelection(savedInstanceState.getInt(Car.KEY_VOLUMEUNIT));
            mCarToEdit.setImageBytes(savedInstanceState.getByteArray(Car.KEY_PICTURE));

            new GetCarImageHelper(this, mImage, mCarToEdit, false).execute();
            if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
                mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
            }
            if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
                mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
            }
        }
    }

    /**
     * Saves the values from the UI into a {@link android.os.Bundle}.
     *
     * @param savedInstanceState The {@link android.os.Bundle} in which to save the values.
     */
    private void saveState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putString(Car.KEY_MAKE, mMake.getText().toString());
            savedInstanceState.putString(Car.KEY_MODEL, mModel.getText().toString());
            savedInstanceState.putString(Car.KEY_BUILDYEAR, mBuildYear.getText().toString());
            savedInstanceState.putString(Car.KEY_LICENSEPLATE, mLicense.getText().toString());
            savedInstanceState.putString(Car.KEY_CURRENCY, mCurrency.getText().toString());
            savedInstanceState.putInt(Car.KEY_DISTANCEUNIT, mDistanceUnits.getSelectedItemPosition());
            savedInstanceState.putInt(Car.KEY_VOLUMEUNIT, mVolumeUnits.getSelectedItemPosition());
            savedInstanceState.putByteArray(Car.KEY_PICTURE, mCarToEdit.getImageBytes());

            if (mCurrentPhotoPath != null) {
                savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
            }
            if (mCapturedImageURI != null) {
                savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
            }
        }
    }

    /**
     * Copies the values from the Car entity we are editing to the
     * UI fields.
     */
    private void toUi() {
        mMake.setText(mCarToEdit.getMake());
        mModel.setText(mCarToEdit.getModel());
        mBuildYear.setText(String.valueOf(mCarToEdit.getBuildYear()));
        mLicense.setText(mCarToEdit.getLicensePlate());
        mCurrency.setText(mCarToEdit.getCurrency());
        mDistanceUnits.setSelection(mCarToEdit.getDistanceUnit().getValue() - 1);
        mVolumeUnits.setSelection(mCarToEdit.getVolumeUnit().getValue() - 1);

        new GetCarImageHelper(this, mImage, mCarToEdit, false).execute();
    }

    /**
     * Copy the values from the UI to the Car entity we are editing.
     */
    private void fromUi() {
        mCarToEdit.setMake(mMake.getText().toString());
        mCarToEdit.setModel(mModel.getText().toString());
        mCarToEdit.setBuildYear(Integer.parseInt(mBuildYear.getText().toString()));
        mCarToEdit.setLicensePlate(mLicense.getText().toString());
        mCarToEdit.setCurrency(mCurrency.getText().toString());
        mCarToEdit.setDistanceUnit(DistanceUnit.fromValue(mDistanceUnits.getSelectedItemPosition() + 1));
        mCarToEdit.setVolumeUnit(VolumeUnit.fromValue(mVolumeUnits.getSelectedItemPosition() + 1));
    }

    /**
     * Validate all fields in the UI that require validation.
     *
     * @return true for a successful validation. false if one or more
     * of the fields failed validation.
     */
    private boolean validateFields() {
        return mMake.validate() &&
                mModel.validate() &&
                mBuildYear.validate() &&
                mLicense.validate() &&
                mCurrency.validate();
    }

    /**
     * Called to create the Activity menu.
     *
     * @param menu The menu.
     * @return true to show the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ok_cancel, menu);
        return true;
    }

    /**
     * Adds a new {@link Car} entity to the database or updates an existing one.
     *
     * @return true for success, false for failure.
     */
    private boolean addOrUpdateCar() {
        // Validate the fields before we continue.
        boolean isOk = validateFields();
        if (isOk) {
            // Copy the UI contents into the Car entity.
            fromUi();

            // When we have an ID of 0 here it means that this is a new
            // Car entity.
            if (mCarToEdit.getId() == 0) {
                // Add the Car to the database and store it's ID.
                int carId = (int) context.addCar(mCarToEdit);
                if (carId != 0) {
                    mCarToEdit.setId(carId);
                } else {
                    // That failed for some reason...
                    isOk = false;
                }
            } else {
                // Update the car entity in the database.
                isOk = context.updateCar(mCarToEdit);
            }

            if (isOk) {
                // We have a successful database transaction. Return the Car entity
                // to the calling Activity.
                Intent result = new Intent();
                result.putExtra(Keys.EK_CAR, mCarToEdit);
                result.putExtra(Keys.EK_VIEWPOSITION, mViewPosition);
                if (getParent() == null) {
                    setResult(RESULT_OK, result);
                } else {
                    getParent().setResult(RESULT_OK, result);
                }
                finish();
            } else {
                DialogHelper.showMessageDialog(getString(R.string.dialog_error_title), getString(R.string.error_saving_car), this);
            }
        }
        return isOk;
    }

    /**
     * Callback the is called when the user has selected an item from the options
     * menu.
     *
     * @param item The item the user selected.
     * @return true when the item selection was handled, false when not.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            addOrUpdateCar();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case REQUEST_GET_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    mCarToEdit.setImage(this, selectedImage);
                    new GetCarImageHelper(this, mImage, mCarToEdit, false).execute();
                }
                break;

            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    mCarToEdit.setImage(this, mCapturedImageURI);
                    new GetCarImageHelper(this, mImage, mCarToEdit, false).execute();
                }
                break;
        }
    }

    /**
     * Removes the picture from the {@link com.development.jaba.model.Car} entity.
     *
     * @param view The {@link android.view.View} that initiated this event.
     */
    public void clearPicture(View view) {
        mCarToEdit.setImage(this, null);
        mImage.setImageBitmap(null);
    }

    /**
     * Load a picture from the pictures present on the device storage and set it
     * on the {@link com.development.jaba.model.Car} entity.
     *
     * @param view The {@link android.view.View} that initiated this event.
     */
    public void getPicture(View view) {
        getPictureFromCameraRoll();
    }

    /**
     * Take a picture with the device camera and set it on the {@link com.development.jaba.model.Car} entity.
     *
     * @param view The {@link android.view.View} that initiated this event.
     */
    public void takePicture(View view) {
        getPictureFromCamera();
    }

    /**
     * Start the activity for picking an image.
     */
    private void getPictureFromCameraRoll() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(photoPickerIntent, REQUEST_GET_PICTURE);
    }

    /**
     * Start the activity for taking a picture with the device camera.
     */
    private void getPictureFromCamera() {
        // Check if there is a camera. Show a toast if there isn't.
        PackageManager packageManager = getPackageManager();
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Snackbar.make(mImage, R.string.error_no_camera, Snackbar.LENGTH_LONG).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go.
            // If you don't do this, you may get a crash in some devices.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Snackbar.make(mImage, R.string.error_save_picture, Snackbar.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCapturedImageURI = Uri.fromFile(photoFile);
                mCurrentPhotoPath = mCapturedImageURI.getPath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Creates a {@link java.io.File} object to be used by the device camera
     * for storing the picture.
     *
     * @return The {@link java.io.File} object.
     * @throws IOException
     */
    protected File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    /**
     * Build year validator. Checks if the value is between 1672 and the current year.
     */
    public class BuildYearValidator extends BaseValidator {
        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         */
        public BuildYearValidator(Context context) {
            super(context);
        }

        /**
         * Checks if the value is between 1672 and the current year.
         *
         * @param value The value to validate.
         * @return true for success, false for failure.
         */
        @Override
        public boolean isValid(String value) {
            try {
                int year = Integer.parseInt(value);
                if (year < 1672) {
                    setErrorMessage(R.string.buildyear_to_low);
                    return false;
                } else if (year > DateHelper.getYearFromDate(new Date())) {
                    setErrorMessage(R.string.buildyear_to_high);
                    return false;
                }
            } catch (NumberFormatException ex) {
                setErrorMessage(R.string.buildyear_error);
                return false;
            }
            setErrorMessage(null);
            return true;
        }
    }

    /**
     * Validator class to validate the entered license plate value.
     */
    private class LicensePlateValidator extends EditTextEx.BaseValidator {

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param context The context.
         */
        public LicensePlateValidator(Context context) {
            super(context);
        }

        /**
         * Checks if a value has been entered and if the value is not the same as another
         * license plate in the database.
         *
         * @param value The value to validate.
         * @return true if the value is valid, false if it was not.
         */
        @Override
        public boolean isValid(String value) {
            // We need a value...
            if (TextUtils.isEmpty(value)) {
                setErrorMessage(R.string.no_text_error);
                return false;
            }

            if (mCarToEdit.getId() == 0 && context.getCarByLicensePlate(value) != null) {
                setErrorMessage(getString(R.string.license_error));
                return false;
            }
            return true;
        }
    }
}