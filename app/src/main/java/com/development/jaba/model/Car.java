package com.development.jaba.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
    private byte[] mPicture;
    private String mLicensePlate; // 15
    private int mBuildYear;
    private String mCurrency; // 3
    private DistanceUnit mDistanceUnit;
    private VolumeUnit mVolumeUnit;
    private Bitmap mImage;
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
            this.mPicture = cursor.getBlob(3);
            this.mLicensePlate = cursor.getString(4);
            this.mBuildYear = cursor.getInt(5);
            this.mCurrency = cursor.getString(6);
            this.mDistanceUnit = DistanceUnit.fromValue(cursor.getInt(7));
            this.mVolumeUnit = VolumeUnit.fromValue(cursor.getInt(8));
            if (this.mPicture != null) {
                this.mImage = BitmapFactory.decodeByteArray(this.mPicture, 0, this.mPicture.length);
            }
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

    public byte[] getPicture() {
        return mPicture;
    }

    public void setPicture(byte[] picture) {
        this.mPicture = picture;
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
        return mImage;
    }

    public void setImage(Bitmap image) {
        this.mImage = image;
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
        values.put(KEY_PICTURE, this.mPicture);
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

    public String toPrice(double price) {
        return String.format("%s %.02f", getCurrency(), price);
    }
    //endregion
}