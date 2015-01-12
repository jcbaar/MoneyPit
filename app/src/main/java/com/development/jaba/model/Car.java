package com.development.jaba.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.development.jaba.moneypit.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * Class representing the Car entity.
 */
public class Car implements Serializable {

    //region Table column name constants.
    public static final String KEY_ID = "_id";
    public static final String KEY_MAKE = "Make";
    public static final String KEY_MODEL = "Model";
    public static final String KEY_PICTURE = "Picture";
    public static final String KEY_LICENSEPLATE = "LicensePlate";
    public static final String KEY_BUILDYEAR = "BuildYear";
    public static final String KEY_CURRENCY = "Currency";
    public static final String KEY_VOLUMEUNIT = "VolumeUnit";
    public static final String KEY_DISTANCEUNIT = "DistanceUnit";
    //endregion

    //region Private fields.
    private int mId;
    private String mMake; // 50
    private String mModel; // 50
    private String mLicensePlate; // 15
    private int mBuildYear;
    private String mCurrency; // 3
    private DistanceUnit mDistanceUnit;
    private VolumeUnit mVolumeUnit;
    private byte[] mImage;
    private String mDateRange;

    private CarAverage mAverages;
    //endregion

    //region Construction
    /**
     * Constructor. Initializes an instance of the object.
     */
    public Car() {}

    /**
     * Constructor. Initializes an instance of the object with the values
     * read from the given cursor. It is assumed that the cursor contains
     * records from the 'Car' table. The instance is created from the row
     * at which the cursor points.
     */
    public Car(Cursor cursor) {
        try {
            this.mId = cursor.getInt(0);
            this.mMake = cursor.getString(1);
            this.mModel = cursor.getString(2);
            this.mImage = cursor.getBlob(3);
            this.mLicensePlate = cursor.getString(4);
            this.mBuildYear = cursor.getInt(5);
            this.mCurrency = cursor.getString(6);
            this.mVolumeUnit = VolumeUnit.fromValue(cursor.getInt(7));
            this.mDistanceUnit = DistanceUnit.fromValue(cursor.getInt(8));
        } catch (Exception e) {
            Log.e("Car(cursor)", Log.getStackTraceString(e));
        }
    }
    //endregion

    //region Field getters and setters.
    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getMake() {
        return mMake;
    }

    public void setMake(String make) {
        this.mMake = make;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        this.mModel = model;
    }

    public String getLicensePlate() {
        return mLicensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.mLicensePlate = licensePlate;
    }

    public int getBuildYear() {
        return mBuildYear;
    }

    public void setBuildYear(int buildYear) {
        this.mBuildYear = buildYear;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        this.mCurrency = currency;
    }

    public DistanceUnit getDistanceUnit() {
        return mDistanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        this.mDistanceUnit = distanceUnit;
    }

    public VolumeUnit getVolumeUnit() {
        return mVolumeUnit;
    }

    public void setVolumeUnit(VolumeUnit volumeUnit) {
        this.mVolumeUnit = volumeUnit;
    }

    public Bitmap getImage() {
        if(mImage != null) {
            return BitmapFactory.decodeByteArray(this.mImage, 0, this.mImage.length);
        }
        return null;
    }

    public void setImage(Context context, Uri bitmapUri) {
        if(bitmapUri != null) {
            try {
                mImage = decodeUri(context, bitmapUri);
            } catch (FileNotFoundException e) {
                Log.e("setImage", "Image file not found.");
                mImage = null;
            }
        }
        else {
            mImage = null;
        }
    }

    public String getDateRange() {
        return mDateRange;
    }

    public void setDateRange(String dateRange) {
        this.mDateRange = dateRange;
    }

    public CarAverage getAverages() {
        return mAverages;
    }

    public void setAverages(CarAverage averages) {
        mAverages = averages;
    }
    //endregion

    //region Conversion methods.
    /**
     * Converts the object to {@link android.content.ContentValues}.
     * @return The {@link android.content.ContentValues} object containing the object.
     * This will not include the ID field.
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_MAKE, this.mMake);
        values.put(KEY_MODEL, this.mModel);
        values.put(KEY_BUILDYEAR, this.mBuildYear);
        values.put(KEY_CURRENCY, this.mCurrency);
        values.put(KEY_DISTANCEUNIT, this.mDistanceUnit.getValue());
        values.put(KEY_LICENSEPLATE, this.mLicensePlate);
        values.put(KEY_PICTURE, this.mImage);
        values.put(KEY_VOLUMEUNIT, this.mVolumeUnit.getValue());
        return values;
    }

    /**
     * Converts the object to it's string representation.
     * @return The string representation of the object.
     */
    @Override
    public String toString() {
        return mMake + " " + mModel;
    }
    //endregion


    //region Helpers
    /**
     * Decodes the image at the given Uri and automatically scales it to the proper size.
     *
     * @param context The {@link android.content.Context}
     * @param selectedImage The {@link android.net.Uri} of the picture to decode.
     *
     * @return A array of bytes holding the complete image file.
     * @throws FileNotFoundException
     */
    private byte[] decodeUri(Context context, Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int maxSize = context.getResources().getInteger(R.dimen.image_scale_max_size);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < maxSize || height_tmp / 2 < maxSize) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bs);
        bm.recycle();
        return bs.toByteArray();
    }
    //endregion
}