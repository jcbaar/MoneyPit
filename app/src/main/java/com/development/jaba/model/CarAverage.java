package com.development.jaba.model;

import android.database.Cursor;
import android.util.Log;

import java.io.Serializable;

/**
 * Class for holding the averages of a single car. Averages are computed
 * on full tank fill-ups only.
 */
public class CarAverage implements Serializable {

    //region Private fields.
    private int mCarId;                         // The ID of the car this average belongs to.
    private double mAveragePricePerVolumeUnit;  // The average fuel price per volume unit.
    private double mAverageVolumePerFillup;     // The average volume per fill-up.
    //endregion

    //region Construction
    /**
     * Constructor. Initializes an instance of the object with the values
     * read from the given cursor. It is assumed that the cursor contains
     * records from the 'getCarAverages()' result. The instance is created from the row
     * at which the cursor points.
     */
    public CarAverage(Cursor cursor) {
        try {
            this.mCarId = cursor.getInt(0);
            this.mAveragePricePerVolumeUnit = cursor.getDouble(1);
            this.mAverageVolumePerFillup = cursor.getDouble(2);
        } catch (Exception e) {
            Log.e("CarAverage(cursor)", Log.getStackTraceString(e));
        }
    }
    //endregion

    //region Getters and setters.
    public int getCarId() {
        return mCarId;
    }

    public void setCarId(int carId) {
        this.mCarId = carId;
    }

    public double getAveragePricePerVolumeUnit() {
        return mAveragePricePerVolumeUnit;
    }

    public void setAveragePricePerVolumeUnit(double averagePricePerVolumeUnit) {
        this.mAveragePricePerVolumeUnit = averagePricePerVolumeUnit;
    }

    public double getAverageVolumePerFillup() {
        return mAverageVolumePerFillup;
    }

    public void setAverageVolumePerFillup(double AverageVolumePerFillup) {
        this.mAverageVolumePerFillup = AverageVolumePerFillup;
    }
    //endregion
}
