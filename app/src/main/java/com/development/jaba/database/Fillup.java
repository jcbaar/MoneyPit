package com.development.jaba.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Jan on 8-11-2014.
 */
public class Fillup implements Serializable {
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

    private int id;
    private int carId;
    private Date date;
    private double odometer;
    private double volume;
    private double price;
    private boolean fullTank;
    private String note;
    private double longitude;
    private double latitude;

    private int daysSinceLastFillup;
    private double distance;
    private double totalPrice;
    private double fuelConsumption;

    public Fillup() {}

    public Fillup(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.carId = cursor.getInt(1);
        this.date = Utils.getDateFromDateTime(cursor.getString(2));
        this.odometer = cursor.getDouble(3);
        this.volume = cursor.getDouble(4);
        this.price = cursor.getDouble(5);
        this.fullTank = cursor.getInt(6) == 1;
        this.note = cursor.getString(7);
        this.longitude = cursor.getDouble(8);
        this.latitude = cursor.getDouble(9);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_CAR, this.carId);
        values.put(KEY_DATE, Utils.getDateTimeAsString(this.date));
        values.put(KEY_ODOMETER, this.odometer);
        values.put(KEY_VOLUME, this.volume);
        values.put(KEY_PRICE, this.price);
        values.put(KEY_FULLTANK, this.fullTank);
        values.put(KEY_NOTE, this.note);
        values.put(KEY_LONGITUDE, this.longitude);
        values.put(KEY_LATITUDE, this.latitude);
        return values;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getOdometer() {
        return odometer;
    }

    public void setOdometer(double odometer) {
        this.odometer = odometer;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isFullTank() {
        return fullTank;
    }

    public void setFullTank(boolean fullTank) {
        this.fullTank = fullTank;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getDaysSinceLastFillup() { return daysSinceLastFillup; }

    public void setDaysSinceLastFillup(int daysSinceLastFillup) { this.daysSinceLastFillup = daysSinceLastFillup; }

    public double getDistance() { return distance; }

    public void setDistance(double distance) { this.distance = distance; }

    public double getTotalPrice() { return totalPrice; }

    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getFuelConsumption() { return fuelConsumption; }

    public void setFuelConsumption(double fuelConsumption) { this.fuelConsumption = fuelConsumption; }
}
