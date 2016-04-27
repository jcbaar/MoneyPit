package com.development.jaba.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.development.jaba.model.Car;
import com.development.jaba.model.CarAverage;
import com.development.jaba.model.Fillup;
import com.development.jaba.model.SurroundingFillups;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.DialogHelper;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Database context class containing the MoneyPit database functionality.
 */
public class MoneyPitDbContext extends SQLiteOpenHelper {

    //region Private fields
    private static final int DATABASE_VERSION = 4;              // Database version.
    public static final String DATABASE_NAME = "MoneyPit.db3"; // Database filename.
    private static final String TABLE_CAR = "Car";              // Car entity table name.
    private static final String TABLE_FILLUP = "Fillup";        // Fillup entity table name.
    private final MoneyPitDbManager mDbManager;                 // Database manager instance handling singleton SQLiteDatabase.
    private final Context mContext;                             // The context.
    //endregion

    //region Construction.

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     */
    public MoneyPitDbContext(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        MoneyPitDbManager.initializeInstance(this);
        mDbManager = MoneyPitDbManager.getInstance();
        mContext = context;
    }
    //endregion


    //region Overrides

    /**
     * Make sure foreign key constraints are active.
     *
     * @param db The {@link android.database.sqlite.SQLiteDatabase}.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
    //endregion

    //region Database create/update
    @Override
    public void onCreate(SQLiteDatabase db) {
        // First create the data model.
        try {
            db.beginTransaction();
            db.execSQL("CREATE TABLE IF NOT EXISTS [Car] ( " +
                    "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "[Make] NTEXT(50) NOT NULL, " +
                    "[Model] NTEXT(50) NOT NULL, " +
                    "[Picture] IMAGE, " +
                    "[LicensePlate] NTEXT(15) UNIQUE NOT NULL, " +
                    "[BuildYear] INT, " +
                    "[Currency] NTEXT(3) NOT NULL, " +
                    "[VolumeUnit] SMALLINT NOT NULL DEFAULT 1, " +
                    "[DistanceUnit] SMALLINT NOT NULL DEFAULT 1);");

            db.execSQL("CREATE TABLE IF NOT EXISTS [Fillup] ( " +
                    "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "[CarId] INTEGER NOT NULL CONSTRAINT [FillupCar] REFERENCES [Car]([_id]) ON DELETE CASCADE, " +
                    "[Date] DATETIME NOT NULL, " +
                    "[Odometer] DOUBLE NOT NULL, " +
                    "[Volume] DOUBLE NOT NULL, " +
                    "[Price] DOUBLE NOT NULL, " +
                    "[FullTank] BOOL NOT NULL, " +
                    "[Note] NTEXT, " +
                    "[Longitude] DOUBLE, " +
                    "[Latitude] DOUBLE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS [CrashLog] ( " +
                    "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "[Message] NTEXT NOT NULL, " +
                    "[Stacktrace] NTEXT NOT NULL, " +
                    "[Stamp] DATETIME NOT NULL);");
            db.setTransactionSuccessful();
        } catch (SQLiteException ex) {
            Log.e("onCreate", ex.getMessage());
            DialogHelper.showMessageDialog(mContext.getString(R.string.dialog_error_title), mContext.getString(R.string.model_create_failed), mContext);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
            From version 1:
            Add indexes.
         */
        if (oldVersion >= 1) {
            try {
                db.beginTransaction();
                if (oldVersion == 1) {
                    db.execSQL("CREATE INDEX carToFillup ON Fillup(CarId)");
                    oldVersion = 2;
                }
                if (oldVersion == 2) {
                    db.execSQL("CREATE INDEX dateOfFillup ON Fillup(Date)");
                }
                if (oldVersion == 3) {
                    db.execSQL("CREATE INDEX odoOfFillup ON Fillup(Odometer)");
                    db.execSQL("CREATE INDEX volOfFillup ON Fillup(Volume)");
                }
                db.setTransactionSuccessful();
            }
            catch (SQLiteException ex) {
                Log.e("onUpgrade", ex.getMessage());
                DialogHelper.showMessageDialog(mContext.getString(R.string.dialog_error_title), mContext.getString(R.string.model_create_failed), mContext);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            } finally {
                db.endTransaction();
            }
        }
    }
    //endregion

    //region Car related methods

    /**
     * Adds a car to the database. It will return 0 when the car has a
     * license plate which is already present in the database.
     *
     * @param car The car entity to store in the database.
     * @return The database ID of the added Car entity or 0 when either a car
     * with the same license plate already exists in the database or an error
     * occurred during the database add.
     */
    public long addCar(Car car) {
        SQLiteDatabase db;
        try {
            db = mDbManager.openDatabase();

            ContentValues values = car.toContentValues();
            return db.insertOrThrow(TABLE_CAR,
                    null,
                    values);
        } catch (SQLiteException ex) {
            Log.e("addCar", ex.getMessage());
            return 0;
        } finally {
            mDbManager.closeDatabase();
        }
    }

    /**
     * Updates the given car entity in the database. The id field
     * of the entity needs to contain the id of the car entity to
     * update. The rest of the fields are used as the updated information.
     *
     * @param car The car entity to update in the database.
     * @return true for success, false for failure.
     */
    public boolean updateCar(Car car) {
        SQLiteDatabase db;
        try {
            db = mDbManager.openDatabase();

            ContentValues values = car.toContentValues();

            return db.update(TABLE_CAR,
                    values,
                    Car.KEY_ID + " = ?",
                    new String[]{String.valueOf(car.getId())}) == 1;
        } catch (SQLiteException ex) {
            Log.e("updateCar", ex.getMessage());
            return false;
        } finally {
            mDbManager.closeDatabase();
        }
    }

    /**
     * Deletes the given car entity from the database. Only the
     * id field of the entity needs to be valid.
     *
     * @param car The car entity to delete from the database.
     * @return true for success, false for failure.
     */
    public boolean deleteCar(Car car) {
        SQLiteDatabase db;

        try {
            db = mDbManager.openDatabase();

            return db.delete(TABLE_CAR,
                    Car.KEY_ID + " = ?",
                    new String[]{String.valueOf(car.getId())}) == 1;
        } catch (SQLiteException ex) {
            Log.e("deleteCar", ex.getMessage());
            return false;
        } finally {
            mDbManager.closeDatabase();
        }
    }

    /**
     * Helper function to either get all car entities from the database or to
     * get a single car entity from the database.
     *
     * @param id The id of the car entity to get or 0 to get them all.
     * @return A List of car entities queried from the database.
     */
    private List<Car> getCars(int id) {
        List<Car> cars = new LinkedList<>();

        String query;
        String[] args = null;
        if (id == 0) {
            query = "SELECT  * FROM " + TABLE_CAR;
        } else {
            query = "SELECT * FROM " + TABLE_CAR + " WHERE _Id = ?";
            args = new String[]{String.valueOf(id)};
        }

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                do {
                    Car car = new Car(cursor);
                    cars.add(car);
                } while (cursor.moveToNext());
            }
            return cars;
        } catch (SQLiteException ex) {
            Log.e("getCars", ex.getMessage());
            cars.clear();
            return cars;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Get all car entities from the database.
     *
     * @return A List of Car entities or an empty list if there are no
     * cars in the database or an error occurred.
     */
    public List<Car> getAllCars() {
        return getCars(0);
    }


    /**
     * Gets a car with the given license plate.
     *
     * @param licensePlate The license plate to look for.
     * @return The car entity or null if the car was not found in the database.
     */
    public Car getCarByLicensePlate(String licensePlate) {
        Car result = null;
        String query = "SELECT * FROM " + TABLE_CAR + " WHERE LicensePlate = ? COLLATE NOCASE";
        String[] args = new String[]{String.valueOf(licensePlate)};

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.getCount() == 1) {
                if (cursor.moveToFirst()) {
                    result = new Car(cursor);
                }
            }
            return result;
        } catch (SQLiteException ex) {
            Log.e("getCars", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Gets the averages for all cars in the database. The averages are computed on
     * full tank fill-ups only. Partial fill-ups are left out of the computation.
     *
     * @return A {@link @List} of {@link CarAverage} objects. One {@link CarAverage} for each
     * car in the database.
     */
    public List<CarAverage> getCarAverages() {
        List<CarAverage> avgs = new LinkedList<>();

        String query;

        query = "SELECT Car._id, SUM(Price)/COUNT(1), SUM(Volume)/COUNT(1) FROM Fillup " +
                "LEFT OUTER JOIN Car ON Car._id = Fillup.CarId " +
                "WHERE Fillup.FullTank = 1 AND Fillup.Volume <> 0 " +
                "GROUP BY Car._id";

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    CarAverage avg = new CarAverage(cursor);
                    avgs.add(avg);
                } while (cursor.moveToNext());
            }
            return avgs;
        } catch (SQLiteException ex) {
            Log.e("getCarAverages", ex.getMessage());
            avgs.clear();
            return avgs;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Gets the average for a single {@link Car} from the database. The averages are computed on
     * full tank fill-ups only. Partial fill-ups are left out of the computation.
     *
     * @return A {@link CarAverage} object.
     */
    public CarAverage getCarAverage(int carId) {
        String query;
        query = "SELECT Car._id, SUM(Price)/COUNT(1), SUM(Volume)/COUNT(1) FROM Fillup " +
                "LEFT OUTER JOIN Car ON Car._id = Fillup.CarId " +
                "WHERE Fillup.FullTank = 1 AND Fillup.Volume <> 0 AND Car._id = ?";
        String[] args = new String[]{String.valueOf(carId)};

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            CarAverage avg = null;
            if (cursor.moveToFirst()) {
                avg = new CarAverage(cursor);
            }
            return avg;
        } catch (SQLiteException ex) {
            Log.e("getCarAverage", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }
    //endregion

    //region Fillup related methods

    /**
     * Add a new fillup entity to the database.
     *
     * @param fillup The fillup entity to add to the database.
     * @return The database id of the added entity or 0 in case of an error.
     */
    public long addFillup(Fillup fillup) {
        SQLiteDatabase db;
        try {
            db = mDbManager.openDatabase();

            ContentValues values = fillup.toContentValues();
            return db.insertOrThrow(TABLE_FILLUP,
                    null,
                    values);
        } catch (SQLiteException ex) {
            Log.e("addFillup", ex.getMessage());
            return 0;
        } finally {
            mDbManager.closeDatabase();
        }
    }

    /**
     * Update a fillup entity in the database. The id field of the entity
     * should contain the database id of the fillup to update. The other fields
     * are used as the information to update.
     *
     * @param fillup The fillup entity to update.
     * @return true for success, false for failure.
     */
    public boolean updateFillup(Fillup fillup) {
        SQLiteDatabase db;
        try {
            db = mDbManager.openDatabase();

            ContentValues values = fillup.toContentValues();

            return db.update(TABLE_FILLUP,
                    values,
                    Fillup.KEY_ID + " = ?",
                    new String[]{String.valueOf(fillup.getId())}) == 1;
        } catch (SQLiteException ex) {
            Log.e("updateFillup", ex.getMessage());
            return false;
        } finally {
            mDbManager.closeDatabase();
        }
    }

    /**
     * Delete the given fillup from the database. The id field on the entity
     * should contain the database ID of the fillup to delete. The other fields
     * in the entity are ignored.
     *
     * @param fillup The fillup entity to delete.
     * @return true for success, false for failure.
     */
    public boolean deleteFillup(Fillup fillup) {
        SQLiteDatabase db;

        try {
            db = mDbManager.openDatabase();

            return db.delete(TABLE_FILLUP,
                    Fillup.KEY_ID + " = ?",
                    new String[]{String.valueOf(fillup.getId())}) == 1;
        } catch (SQLiteException ex) {
            Log.e("deleteFillup", ex.getMessage());
            return false;
        } finally {
            mDbManager.closeDatabase();
        }
    }

    /**
     * Gets all the fillups belonging to the given car from the given year.
     *
     * @param carId The id of the car for which to get the fillups.
     * @param year  The year for which to get the fillups or 0 to get them all.
     * @return A List of Fillup entities.
     */
    public List<Fillup> getFillupsOfCar(int carId, int year) {
        List<Fillup> result = new LinkedList<>();
        String query =
                "SELECT *, " +
                        "Odometer - (IFNULL((SELECT Odometer FROM Filtered WHERE Date < T1.Date ORDER BY Date DESC LIMIT 1), Odometer)) AS Distance, " +
                        "CASE WHEN (SELECT CAST(strftime('%s', Date) AS INT) FROM Filtered WHERE Date < T1.Date ORDER BY Date DESC LIMIT 1) IS NULL THEN 0 ELSE " +
                        "(CAST(strftime('%s', Date) AS INT) - (SELECT CAST(strftime('%s', Date) AS INT) FROM Filtered WHERE Date < T1.Date ORDER BY Date DESC LIMIT 1)) / 86400 END AS Days, " +
                // Fuel economy is computed between full fill-ups. The partial fill-ups are only used to compute
                // the total fuel volume. This is then used to compute the total fuel economy between
                // the two full fill-ups.
                //
                // The computed fuel economy of partial fill-ups is incorrect and therefore hidden from
                // the UI and not used for averages.
                        "(Odometer - (IFNULL((SELECT Odometer FROM Filtered WHERE Date < T1.Date AND FullTank = 1 ORDER BY Date DESC LIMIT 1), Odometer))) / " +
                        "(SELECT TOTAL(Volume) FROM Filtered WHERE Date <= T1.Date AND Date > (SELECT Date FROM Filtered WHERE Date < T1.Date AND FullTank = 1 ORDER BY Date DESC LIMIT 1)) AS Economy, " +
                "Price * Volume AS TotalPrice " +
                        "FROM Filtered AS T1 " +
                        "WHERE (? = '0' OR ? = CAST(strftime('%Y', Date) AS INT)) " +
                "ORDER BY Date DESC";
        String[] args = new String[]{//String.valueOf(carId),
//                String.valueOf(carId),
//                String.valueOf(carId),
//                String.valueOf(carId),
//                String.valueOf(carId),
//                String.valueOf(carId),
//                String.valueOf(carId),
                String.valueOf(year),
                String.valueOf(year)};

        SQLiteDatabase db;
        Cursor cursor = null;

        try {
            db = mDbManager.openDatabase();

            // First we are going to limit the data set to the sorted list of fillups
            // of the given car.
            db.execSQL("DROP TABLE IF EXISTS Filtered;");
            db.execSQL("CREATE TEMPORARY TABLE Filtered AS " +
                    "WITH FT_CTE AS ( " +
                    "SELECT * FROM Fillup " +
                    "WHERE CarId = ? " +
                    "ORDER BY DATE DESC" +
                    ") SELECT * FROM FT_CTE; ", new String[]{String.valueOf(carId)});

            // Make sure the temporary table has a index on the Date column to maximise
            // performance.
            db.execSQL("CREATE INDEX dateIdx ON Filtered(Date);");

            // Execute the query on the filtered and sorted data set.
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                do {
                    Fillup fillup = new Fillup(cursor);
                    result.add(fillup);

                } while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;

            // Drop the temporary table.
            db.execSQL("DROP TABLE IF EXISTS Filtered;");
            return result;
        } catch (SQLiteException ex) {
            Log.e("getFillupByCar", ex.getMessage());
            result.clear();
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Get the first fill up entity before the given date and after
     * the given date. If before returns null there are no older fill ups.
     * If after returns null there are no later fill ups. When they both
     * return null there are no fill ups at all.
     *
     * @param date     The date to find the surrounding fill ups of.
     * @param carId    The id of the car the fill ups should belong to.
     * @param fillupId The id of the fill up which is being edited. 0 for a new fill up.
     */
    public SurroundingFillups getSurroundingFillups(Date date, int carId, int fillupId) {
        SurroundingFillups result = new SurroundingFillups();

        String query1 = "SELECT * FROM " + TABLE_FILLUP + " WHERE (Date < ?) AND (CarId = ?) AND (Volume <> 0) AND (_id <> ?) ORDER BY Date DESC LIMIT 1";
        String query2 = "SELECT * FROM " + TABLE_FILLUP + " WHERE (Date > ?) AND (CarId = ?) AND (Volume <> 0) AND (_id <> ?) ORDER BY Date ASC LIMIT 1";
        String[] args = new String[]{DateHelper.toDateTimeString(date),
                String.valueOf(carId),
                String.valueOf(fillupId)};

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query1, args);

            if (cursor.moveToFirst()) {
                Fillup fillup = new Fillup(cursor);
                result.setBefore(fillup);
            }

            cursor.close();
            cursor = null;

            cursor = db.rawQuery(query2, args);
            if (cursor.moveToFirst()) {
                Fillup fillup = new Fillup(cursor);
                result.setAfter(fillup);
            }

            cursor.close();
            cursor = null;
            return result;
        } catch (SQLiteException ex) {
            Log.e("GetSurroundingFillups", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Get the oldest year which the given car has fillup data.
     *
     * @param carId The id of the {@link Car} to get the oldest year of.
     * @return The oldest year of the {@link Car}
     */
    public int getOldestDataYear(int carId) {
        int year = DateHelper.getYearFromDate(new Date());

        String query = "SELECT Date FROM " + TABLE_FILLUP + " WHERE (CarId = ? AND Volume <> 0) ORDER BY Date ASC LIMIT 1";
        String[] args = new String[]{String.valueOf(carId)};

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                year = DateHelper.getYearFromDate(DateHelper.fromDateTimeString(cursor.getString(0)));
            }

            cursor.close();
            cursor = null;
            return year;
        } catch (SQLiteException ex) {
            Log.e("getOldestDataYear", ex.getMessage());
            return year;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Get the average fuel cost of the given car per month in the given year.
     *
     * @param carId The id of the car the information is read for.
     * @param year  The year the data should belong to.
     */
    public List<BarEntry> getFuelCostPerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), SUM(Price * Volume) FROM Fillup WHERE CarId = ? AND Volume <> 0 AND CAST(strftime('%Y', Date) AS INT) = ? GROUP BY CAST(strftime('%m', Date) AS INT)";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(year)};

        return getGraphDataPoints(query, args);
    }

    /**
     * Get the average fuel cost of the given car per month, per kilometer in the given year.
     *
     * @param carId The id of the car the information is read for.
     * @param year  The year the data should belong to.
     */
    public List<BarEntry> getFuelCostPerKilometerPerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), SUM(Price*Volume) / " +
                "SUM(Odometer - (SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? AND Volume <> 0 ORDER BY Date DESC LIMIT 1)) " +
                "FROM Fillup AS T1 WHERE CarId=? AND Volume <> 0 AND CAST(strftime('%Y', Date) AS INT) = ?" +
                "GROUP BY CAST(strftime('%m', Date) AS INT)";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(year)};

        return getGraphDataPoints(query, args);
    }

    /**
     * Get the average driven distance of the given car per month in the given year.
     *
     * @param carId The id of the car the information is read for.
     * @param year  The year the data should belong to.
     */
    public List<BarEntry> getDistancePerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), " +
                "SUM(Odometer - IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? AND Volume <> 0 ORDER BY Date DESC LIMIT 1), Odometer)) " +
                "FROM Fillup AS T1 WHERE CarId=? AND Volume <> 0 AND CAST(strftime('%Y', Date) AS INT) = ?" +
                "GROUP BY CAST(strftime('%m', Date) AS INT)";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(year)};

        return getGraphDataPoints(query, args);
    }

    /**
     * Get the average fuel economy of the given car per month in the given year.
     *
     * @param carId The id of the car the information is read for.
     * @param year  The year the data should belong to.
     */
    public List<BarEntry> getEconomyPerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), " +
                "SUM(Odometer - IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? AND Volume <> 0 AND FullTank = 1 ORDER BY Date DESC LIMIT 1), Odometer)) / SUM(Volume)" +
                "FROM Fillup AS T1 WHERE CarId=? AND Volume <> 0 AND CAST(strftime('%Y', Date) AS INT) = ?" +
                "GROUP BY CAST(strftime('%m', Date) AS INT)";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(year)};

        return getGraphDataPoints(query, args);
    }

    /**
     * Get the statistical information for the graphs.
     *
     * @param query The query to run.
     * @param args  The query arguments.
     * @return An array of 12 {@link BarEntry} objects containing the information.
     */
    public List<BarEntry> getGraphDataPoints(String query, String[] args) {
        List<BarEntry> result = new ArrayList<>();
        for (int m = 0; m < 12; m++) {
            BarEntry e = new BarEntry(0, m);
            e.setXIndex(m);
            result.add(e);
        }

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                do {
                    int m = cursor.getInt(0) - 1;
                    if (m >= 0) {
                        result.get(m).setVal(cursor.getFloat(1));
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
            cursor = null;
            return result;
        } catch (SQLiteException ex) {
            Log.e("getGraphDataPoints", ex.getMessage());
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Get the estimated odometer setting for the next fill-up.
     *
     * @param carId The ID of the {@link Car} for which to return the estimated odometer setting.
     * @return The estimated odometer for the given car.
     */
    public double getEstimatedOdometer(int carId) {
        SQLiteDatabase db;
        Cursor cursor = null;

        String query = "SELECT Odometer - IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? AND Volume <> 0 ORDER BY Date DESC LIMIT 1), Odometer), " +
                "FullTank " +
                "FROM Fillup AS T1 WHERE CarId= ?" +
                "ORDER BY DATE DESC";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(carId)};

        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            double fullDist = 0;
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    double dist = cursor.getDouble(0);
                    boolean isFull = cursor.getInt(1) == 1;
                    if (isFull && dist > 0) {
                        fullDist += dist;
                        count++;
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
            cursor = null;
            return fullDist / count;
        } catch (SQLiteException ex) {
            Log.e("getEstimatedOdometer", ex.getMessage());
            return 0.0f;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }

    /**
     * Check to see if the given car has any data for the given year.
     *
     * @param carId The ID of the {@link Car} for which check.
     * @param year  The year for which to check if there is data.
     * @return The number of record found for the given car.
     */
    public int hasData(int carId, int year) {
        SQLiteDatabase db;
        Cursor cursor = null;
        int records = 0;

        String query = "SELECT COUNT(1) FROM Fillup WHERE CarId = ? AND Volume <> 0 AND ( ? = '0' OR CAST(strftime('%Y', Date) AS INT) = ?)";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(year),
                String.valueOf(year)};

        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                records = cursor.getInt(0);
            }

            cursor.close();
            cursor = null;
            return records;
        } catch (SQLiteException ex) {
            Log.e("hasData", ex.getMessage());
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }
    //endregion
}
