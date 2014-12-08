package com.development.jaba.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Manager class for handling the singleton SQLiteDatabase object.
 */
public class MoneyPitDbManager {
    //region Private fields.
    private int mOpenCounter;                        // SQLiteDatabase open reference counter.
    private static MoneyPitDbManager mInstance;      // The singleton instance of the mDatabase manager.
    private static SQLiteOpenHelper mDatabaseHelper; // The singleton mInstance of the opened SQLiteOpenHelper.
    private SQLiteDatabase mDatabase;                // The singleton mInstance of the mDatabase.
    //endregion

    //region Methods
    /**
     * Initializes the singleton. This needs to be called before any of the other
     * methods are called.
     * @param helper The SQLiteOpenHelper mInstance to associate with the manager.
     */
    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (mInstance == null) {
            mInstance = new MoneyPitDbManager();
            mDatabaseHelper = helper;
        }
    }

    /**
     * Get the singleton mInstance of the manager.
     * @return The singleton mInstance of the manager.
     * @throws IllegalStateException Thrown when this is called before {@see initializeInstance}
     * was called.
     */
    public static synchronized MoneyPitDbManager getInstance() throws IllegalStateException {
        if (mInstance == null) {
            throw new IllegalStateException(MoneyPitDbManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return mInstance;
    }

    /**
     * Opens the mDatabase as writable when it was not yet opened. Adds a reference when the
     * database was already opened.
     * @return The SQLiteDatabase object.
     */
    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    /**
     * Closes the database if this was the last opener. Otherwise the reference counter is
     * decreased.
     */
    public synchronized void closeDatabase() {
        if(mOpenCounter > 0) {
            mOpenCounter--;
            if (mOpenCounter == 0) {
                mDatabase.close();
            }
        }
    }
    //endregion
}