package com.development.jaba.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class Car implements Serializable {

    public static final String KEY_ID = "_id";
    public static final String KEY_MAKE = "Make";
    public static final String KEY_MODEL = "Model";
    public static final String KEY_PICTURE = "Picture";
    public static final String KEY_LICENSEPLATE = "LicensePlate";
    public static final String KEY_BUILDYEAR = "BuildYear";
    public static final String KEY_CURRENCY = "Currency";
    public static final String KEY_VOLUMEUNIT = "VolumeUnit";
    public static final String KEY_DISTANCEUNIT = "DistanceUnit";

    private int id;
    private String make; // 50
    private String model; // 50
    private byte[] picture;
    private String licensePlate; // 15
    private int buildYear;
    private String currency; // 3
    private DistanceUnit distanceUnit;
    private VolumeUnit volumeUnit;
    private Bitmap image;
    private String dateRange;

    public Car() {}

    public Car(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.make = cursor.getString(1);
        this.model = cursor.getString(2);
        this.picture = cursor.getBlob(3);
        this.licensePlate = cursor.getString(4);
        this.buildYear = cursor.getInt(5);
        this.currency = cursor.getString(6);
        this.distanceUnit = DistanceUnit.Unknown.fromValue(cursor.getInt(7));
        this.volumeUnit = VolumeUnit.Unknown.fromValue(cursor.getInt(8));
        if (this.picture != null) {
            this.image = BitmapFactory.decodeByteArray(this.picture, 0, this.picture.length);
        }
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_MAKE, this.make);
        values.put(KEY_MODEL, this.model);
        values.put(KEY_BUILDYEAR, this.buildYear);
        values.put(KEY_CURRENCY, this.currency);
        values.put(KEY_DISTANCEUNIT, this.distanceUnit.getValue());
        values.put(KEY_LICENSEPLATE, this.licensePlate);
        values.put(KEY_PICTURE, this.picture);
        values.put(KEY_VOLUMEUNIT, this.volumeUnit.getValue());
        return values;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getBuildYear() {
        return buildYear;
    }

    public void setBuildYear(int buildYear) {
        this.buildYear = buildYear;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public VolumeUnit getVolumeUnit() {
        return volumeUnit;
    }

    public void setVolumeUnit(VolumeUnit volumeUnit) {
        this.volumeUnit = volumeUnit;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    @Override
    public String toString() {
        return make + " " + model;
    }
}