package com.development.jaba.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manager class for handling the singleton SQLiteDatabase object.
 */
public class MoneyPitDbManager {
    //region Private fields.
    private int openCounter;                        // SQLiteDatabase open reference counter.
    private static MoneyPitDbManager instance;      // The singleton instance of the database manager.
    private static SQLiteOpenHelper databaseHelper; // The singleton instance of the opened SQLiteOpenHelper.
    private SQLiteDatabase database;                // The singleton instance of the database.
    //endregion

    //region Methods
    /**
     * Initializes the singleton. This needs to be called before any of the other
     * methods are called.
     * @param helper The SQLiteOpenHelper instance to associate with the manager.
     */
    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new MoneyPitDbManager();
            databaseHelper = helper;
        }
    }

    /**
     * Get the singleton instance of the manager.
     * @return The singleton instance of the manager.
     * @throws IllegalStateException Thrown when this is called before {@see initializeInstance}
     * was called.
     */
    public static synchronized MoneyPitDbManager getInstance() throws IllegalStateException {
        if (instance == null) {
            throw new IllegalStateException(MoneyPitDbManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    /**
     * Opens the database as writable when it was not yet opened. Adds a reference when the
     * database was already opened.
     * @return The SQLiteDatabase object.
     */
    public synchronized SQLiteDatabase openDatabase() {
        openCounter++;
        if(openCounter == 1) {
            database = databaseHelper.getWritableDatabase();
        }
        return database;
    }

    /**
     * Closes the database if this was the last opener. Otherwise the reference counter is
     * decreased.
     */
    public synchronized void closeDatabase() {
        openCounter--;
        if(openCounter == 0) {
            database.close();

        }
    }
    //endregion
}