package com.development.jaba.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Database context class containing the MoneyPit database functionality.
 */
public class MoneyPitDbContext extends SQLiteOpenHelper {

    //region Private fields
    private static final int DATABASE_VERSION = 1;              // Database version.
    private static final String DATABASE_NAME = "MoneyPit.db3"; // Database filename.
    private static final String TABLE_CAR = "Car";              // Car entity table name.
    private static final String TABLE_FILLUP = "Fillup";        // Fillup entity table name.
    private MoneyPitDbManager _dbManager;                       // Database manager instance handling singleton SQLiteDatabase.
    //endregion

    //region Construction.
    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context.
     */
    public MoneyPitDbContext(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        MoneyPitDbManager.initializeInstance(this);
        _dbManager = MoneyPitDbManager.getInstance();
    }
    //endregion

    //region Database create/update
    @Override
    public void onCreate(SQLiteDatabase db) {
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
        }
        catch(SQLiteException ex) {
            Log.e("onCreate", ex.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
            Perform upgrade logic here.
         */
    }
    //endregion

    //region Car CRUD
    /**
     * Adds a car to the database. It will return 0 when the car has a
     * license plate which is already present in the database.
     * @param car The car entity to store in the database.
     * @return The database ID of the added Car entity or 0 when either a car
     * with the same license plate already exists in the database or an error
     * occurred during the database add.
     */
    public long addCar(Car car) {
        SQLiteDatabase db;
        try {
            db = _dbManager.openDatabase();

            ContentValues values = car.toContentValues();
            return db.insertOrThrow(TABLE_CAR,
                    null,
                    values);
        }
        catch(SQLiteException ex){
            Log.e("addCar", ex.getMessage());
            return 0;
        }
        finally {
            _dbManager.closeDatabase();
        }
    }

    /**
     * Updates the given car entity in the database. The id field
     * of the entity needs to contain the id of the car entity to
     * update. The rest of the fields are used as the updated information.
     * @param car The car entity to update in the database.
     * @return true for success, false for failure.
     */
    public boolean updateCar(Car car) {
        SQLiteDatabase db;
        try {
            db = _dbManager.openDatabase();

            ContentValues values = car.toContentValues();

            return db.update(TABLE_CAR,
                values,
                Car.KEY_ID + " = ?",
                new String[]{String.valueOf(car.getId())}) == 1;
        }
        catch(SQLiteException ex) {
            Log.e("updateCar", ex.getMessage());
            return false;
        }
        finally {
            _dbManager.closeDatabase();
        }
    }

    /**
     * Deletes the given car entity from the database. Only the
     * id field of the entity needs to be valid.
     * @param car The car entity to delete from the database.
     * @return true for success, false for failure.
     */
    public boolean deleteCar(Car car) {
        SQLiteDatabase db;

        try {
            db = _dbManager.openDatabase();

            return db.delete(TABLE_CAR,
                    Car.KEY_ID + " = ?",
                    new String[]{String.valueOf(car.getId())}) == 1;
        }
        catch(SQLiteException ex) {
            Log.e("deleteCar", ex.getMessage());
            return false;
        }
        finally {
            _dbManager.closeDatabase();
        }
    }

    /**
     * Helper function to either get all car entities from the database or to
     * get a single car entity from the database.
     * @param id The id of the car entity to get or 0 to get them all.
     * @return A List of car entities queried from the database.
     */
    private List<Car> getCars(int id) {
        List<Car> cars = new LinkedList<Car>();

        String query;
        String[] args = null;
        if ( id == 0 ) {
            query = "SELECT  * FROM " + TABLE_CAR;
        }
        else {
            query = "SELECT * FROM " + TABLE_CAR + " WHERE _Id = ?";
            args = new String[] { String.valueOf(id) };
        }

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = _dbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                do {
                    Car car = new Car(cursor);
                    cars.add(car);
                } while (cursor.moveToNext());
            }
            return cars;
        }
        catch(SQLiteException ex) {
            Log.e("getCars", ex.getMessage());
            cars.clear();
            return cars;
        }
        finally {
            if(cursor != null){
                cursor.close();
            }
            _dbManager.closeDatabase();
        }
    }

    /**
     * Get a car entity by it's database id.
     * @param id The database id of the car entity to get.
     * @return The car entity or null if it was not found.
     */
    public Car getCarById(int id) {
        List<Car> cars = getCars(id);
        if (cars.size() == 1) {
            return cars.get(0);
        }
        return null;
    }

    /**
     * Get all car entities from the database.
     * @return A List of Car entities or an empty list if there are no
     * cars in the database or an error occurred.
     */
    public List<Car> getAllCars() {
        return getCars(0);
    }


    /**
     * Gets a car with the given license plate.
     * @param licensePlate The license plate to look for.
     * @return The car entity or null if the car was not found in the database.
     */
    public Car getCarByLicensePlate(String licensePlate) {
        Car result = null;
        String query = "SELECT * FROM " + TABLE_CAR + " WHERE LicensePlate = ?";
        String[] args = new String[] { String.valueOf(licensePlate) };

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = _dbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.getCount() == 1) {
                if (cursor.moveToFirst()) {
                    result = new Car(cursor);
                }
            }
            return result;
        }
        catch(SQLiteException ex) {
            Log.e("getCars", ex.getMessage());
            return null;
        }
        finally {
            if(cursor != null){
                cursor.close();
            }
            _dbManager.closeDatabase();
        }
    }
    //endregion

    //region Fillup CRUD
    /**
     * Add a new fillup entity to the database.
     * @param fillup The fillup entity to add to the database.
     * @return The database id of the added entity or 0 in case of an error.
     */
    public long addFillup(Fillup fillup) {
        SQLiteDatabase db;
        try {
            db = _dbManager.openDatabase();

            ContentValues values = fillup.toContentValues();
            return db.insertOrThrow(TABLE_FILLUP,
                    null,
                    values);
        }
        catch(SQLiteException ex){
            Log.e("addFillup", ex.getMessage());
            return 0;
        }
        finally {
            _dbManager.closeDatabase();
        }
    }

    /**
     * Update a fillup entity in the database. The id field of the entity
     * should contain the database id of the fillup to update. The other fields
     * are used as the information to update.
     * @param fillup The fillup entity to update.
     * @return true for success, false for failure.
     */
    public boolean updateFillup(Fillup fillup) {
        SQLiteDatabase db;
        try {
            db = _dbManager.openDatabase();

            ContentValues values = fillup.toContentValues();

            return db.update(TABLE_FILLUP,
                    values,
                    Fillup.KEY_ID + " = ?",
                    new String[]{String.valueOf(fillup.getId())}) == 1;
        }
        catch(SQLiteException ex) {
            Log.e("updateFillup", ex.getMessage());
            return false;
        }
        finally {
            _dbManager.closeDatabase();
        }
    }

    /**
     * Delete the given fillup from the database. The id field on the entity
     * should contain the database ID of the fillup to delete. The other fields
     * in the entity are ignored.
     * @param fillup The fillup entity to delete.
     * @returntrue for success, false for failure.
     */
    public boolean deleteFillup(Fillup fillup) {
        SQLiteDatabase db;

        try {
            db = _dbManager.openDatabase();

            return db.delete(TABLE_FILLUP,
                    Fillup.KEY_ID + " = ?",
                    new String[]{String.valueOf(fillup.getId())}) == 1;
        }
        catch(SQLiteException ex) {
            Log.e("deleteFillup", ex.getMessage());
            return false;
        }
        finally {
            _dbManager.closeDatabase();
        }
    }

    /**
     * Gets the fillup with the given id.
     * @param id The id of the fillup get.
     * @return The fillup entity or null if the fillup was not found in the database.
     */
    public Fillup getFillupById(int id) {
        Fillup result = null;
        String query = "SELECT * FROM " + TABLE_FILLUP + " WHERE _id = ?";
        String[] args = new String[] { String.valueOf(id) };

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = _dbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.getCount() == 1) {
                if (cursor.moveToFirst()) {
                    result = new Fillup(cursor);
                }
            }
            return result;
        }
        catch(SQLiteException ex) {
            Log.e("getFillupById", ex.getMessage());
            return null;
        }
        finally {
            if(cursor != null){
                cursor.close();
            }
            _dbManager.closeDatabase();
        }
    }

    /**
     * Gets all the fillups belonging to the given car from the given year.
     * @param carId The id of the car for which to get the fillups or 0 to get them all..
     * @param year The year for which to get the fillups or 0 to get them all.
     * @return A List of Fillup entities.
     */
    public List<Fillup> getFillupsOfCar(int carId, int year) {
        List<Fillup> result = new LinkedList<Fillup>();
        String query = "SELECT * FROM " + TABLE_FILLUP + " WHERE (? = 0 OR ? = CarId) AND (? = 0 OR ? = CAST(strftime('%Y', Date) AS INT)) ORDER BY Date DESC";
        String[] args = new String[] { String.valueOf(carId),
                                       String.valueOf(carId),
                                       String.valueOf(year),
                                       String.valueOf(year) };

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = _dbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                do {
                    Fillup fillup = new Fillup(cursor);
                    result.add(fillup);
                } while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;

            // When we have items we need to try to get the first fillup that
            // is dated older than the oldest fillup in the list. This is then used
            // to compute the values for the oldest fillup in the list.
            if(result.size() > 0) {
                Fillup fillup = result.get(result.size() - 1);
                query = "SELECT * FROM " + TABLE_FILLUP + " WHERE Date < ? AND carId = ? ORDER BY Date DESC LIMIT 1";
                args = new String[] { String.valueOf(Utils.getDateTimeAsString(fillup.getDate())),
                                      String.valueOf(carId) };

                cursor = db.rawQuery(query, args);
                if(cursor.moveToFirst()) {
                    fillup = new Fillup(cursor);
                }
                else {
                    fillup = null;
                }
                Utils.recomputeFillupTotals(result, fillup);
                cursor.close();
                cursor = null;
            }
            return result;
        }
        catch(SQLiteException ex) {
            Log.e("getFillupByCar", ex.getMessage());
            result.clear();
            return result;
        }
        finally {
            if(cursor != null){
                cursor.close();
            }
            _dbManager.closeDatabase();
        }
    }
    //endregion

    //region Helper for AndroidDatabaseManager activity
    // See {@link https://github.com/sanathp/DatabaseManager_For_Android}
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
    //endregion
}
