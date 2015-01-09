package com.development.jaba.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.development.jaba.utilities.DateHelper;

import java.io.Serializable;
import java.util.Date;

/**
 * Class representing the Fillup entity.
 */
public class Fillup implements Serializable {

    //region Table column name constants.
    public static final String KEY_ID = "_id";
    public static final String KEY_CAR = "CarId";
    public static final String KEY_DATE = "Date";
    public static final String KEY_ODOMETER = "Odometer";
    public static final String KEY_VOLUME = "Volume";
    public static final String KEY_PRICE = "Price";
    public static final String KEY_FULLTANK = "FullTank";
    public static final String KEY_NOTE = "Note";
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_LATITUDE = "Latitude";
    //endregion

    //region Private fields.
    private int mId;
    private int mCarId;
    private Date mDate;
    private double mOdometer;
    private double mVolume;
    private double mPrice;
    private boolean mFullTank;
    private String mNote;
    private double mLongitude;
    private double mLatitude;

    // The following fields are not persisted to the database.
    private int mDaysSinceLastFillup;
    private double mDistance;
    private double mTotalPrice;
    private double mFuelConsumption;
    //endregion

    //region Construction.
    /**
     * Constructor. Initializes an instance of the object.
     */
    public Fillup() {}

    /**
     * Constructor. Initializes an instance of the object with the values
     * read from the given cursor. It is assumed that the cursor contains
     * records from the 'Fillup' table. The instance is created from the row
     * at which the cursor points.
     */
    public Fillup(Cursor cursor) {
        try {
            this.mId = cursor.getInt(0);
            this.mCarId = cursor.getInt(1);
            this.mDate = DateHelper.getDateFromDateTime(cursor.getString(2));
            this.mOdometer = cursor.getDouble(3);
            this.mVolume = cursor.getDouble(4);
            this.mPrice = cursor.getDouble(5);
            this.mFullTank = cursor.getInt(6) == 1;
            this.mNote = cursor.getString(7);
            this.mLongitude = cursor.getDouble(8);
            this.mLatitude = cursor.getDouble(9);

            // Computed in query, not stored in data model.
            this.mDistance = cursor.getDouble(10);
            this.mDaysSinceLastFillup = cursor.getInt(11);
            this.mFuelConsumption = cursor.getDouble(12);
            this.mTotalPrice = cursor.getDouble(13);
        } catch (Exception e) {
            Log.e("Fillup(cursor)", Log.getStackTraceString(e));
        }
    }
    //endregion

    //region Conversion methods.
    /**
     * Converts the object to {@link android.content.ContentValues}.
     * @return The {@link android.content.ContentValues} object containing the object. This will not
     * include the ID field. Also the fields that are not persisted to the database will not be included.
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_CAR, this.mCarId);
        values.put(KEY_DATE, DateHelper.getDateTimeAsString(this.mDate));
        values.put(KEY_ODOMETER, this.mOdometer);
        values.put(KEY_VOLUME, this.mVolume);
        values.put(KEY_PRICE, this.mPrice);
        values.put(KEY_FULLTANK, this.mFullTank);
        values.put(KEY_NOTE, this.mNote);
        values.put(KEY_LONGITUDE, this.mLongitude);
        values.put(KEY_LATITUDE, this.mLatitude);
        return values;
    }
    //endregion

    //region Field getters and setters.
    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getCarId() {
        return mCarId;
    }

    public void setCarId(int carId) {
        this.mCarId = carId;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public double getOdometer() {
        return mOdometer;
    }

    public void setOdometer(double odometer) {
        this.mOdometer = odometer;
    }

    public double getVolume() {
        return mVolume;
    }

    public void setVolume(double volume) {
        this.mVolume = volume;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        this.mPrice = price;
    }

    public boolean getFullTank() {
        return mFullTank;
    }

    public void setFullTank(boolean fullTank) {
        this.mFullTank = fullTank;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        this.mNote = note;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public int getDaysSinceLastFillup() {
        return mDaysSinceLastFillup;
    }

    public void setDaysSinceLastFillup(int daysSinceLastFillup) {
        this.mDaysSinceLastFillup = daysSinceLastFillup;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double distance) {
        this.mDistance = distance;
    }

    public double getTotalPrice() {
        return mTotalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.mTotalPrice = totalPrice;
    }

    public double getFuelConsumption() {
        return mFuelConsumption;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.mFuelConsumption = fuelConsumption;
    }
    //endregion
}
