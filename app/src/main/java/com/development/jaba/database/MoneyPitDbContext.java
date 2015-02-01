package com.development.jaba.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.development.jaba.model.Car;
import com.development.jaba.model.CarAverage;
import com.development.jaba.model.Fillup;
import com.development.jaba.model.SurroundingFillups;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.ConditionalHelper;
import com.development.jaba.utilities.DateHelper;
import com.jjoe64.graphview.series.DataPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Database context class containing the MoneyPit database functionality.
 */
public class MoneyPitDbContext extends SQLiteOpenHelper {

    //region Private fields
    private static final int DATABASE_VERSION = 1;              // Database version.
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

            if (ConditionalHelper.DebugData) {
                db.execSQL("INSERT INTO Car([Make], [Model], [Picture], [LicensePlate], [BuildYear], [Currency], [VolumeUnit], [DistanceUnit]) VALUES('Peugeot','207',null,'98-TX-NV',2007,'€',1,1);");
                db.execSQL("INSERT INTO Car([Make], [Model], [Picture], [LicensePlate], [BuildYear], [Currency], [VolumeUnit], [DistanceUnit]) VALUES('Waypoint','Markers',null,'AA-11-BB',2013,'€',1,1);");

                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(2,'2013-10-28 21:42:53',3,1,1,1,'In de Keteldiep',4.72045971,51.81249661);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(2,'2013-10-25 17:32:43',2,1,1,1,'EuroStar Pattaya',100.86944286,12.89844516);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(2,'2013-10-25 10:59:22',1,1,1,1,'Het eetcafe bij de markt in Pattaya.',100.88068384,12.92432244);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(2,'2013-12-13 23:52:14',4,1,1,1,'Thuis in Prachin Buri',101.373045,14.08668);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-12-16 19:04:21',107218,33.48,1.619,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-12-08 21:03:56',106750,36.79,1.629,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-11-28 00:00:00',106250,33.15,1.619,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-11-20 16:43:45',105778,33.04,1.619,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-11-12 00:00:00',105326,31.59,1.609,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-11-04 00:00:00',104894,30.7,1.619,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-10-28 16:51:10',104473,32.75,1.625,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-09-24 00:00:00',104037,31.26,1.704,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-09-16 00:00:00',103591,30.56,1.724,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-09-08 00:00:00',103152,35.49,1.739,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-08-31 00:00:00',102614,35.66,1.729,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-08-20 00:00:00',102113,35.71,1.71,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-08-17 00:00:00',101534,23.86,1.709,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-08-11 00:00:00',101196,32.51,1.719,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-08-03 00:00:00',100674,37.16,1.759,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-07-30 00:00:00',100117,32.78,1.724,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-07-21 00:00:00',99601,32.32,1.719,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-07-15 00:00:00',99094,32.59,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-07-07 00:00:00',98594,35.78,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-06-27 00:00:00',98057,31.5,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-06-19 00:00:00',97612,37.14,1.689,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-06-10 00:00:00',97041,32.63,1.689,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-06-02 00:00:00',96559,33.66,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-05-31 00:00:00',96000,34.98,1.739,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-05-26 00:00:00',95483,31.77,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-05-18 00:00:00',94992,36.09,1.789,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-05-10 00:00:00',94487,30.12,1.669,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-05-02 00:00:00',94037,34.08,1.649,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-04-23 00:00:00',93567,31.41,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-04-16 00:00:00',93115,31.46,1.689,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-04-09 00:00:00',92658,31.2,1.719,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-04-03 00:00:00',92208,32.8,1.709,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-03-29 00:00:00',91738,32.28,1.689,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2013-12-26 09:36:45',107604,28.91,1.619,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(2,'2013-12-29 10:46:53',5,1,1,1,'Thuis in de Dintelstraat',4.699684827064417,51.817291690445295);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(2,'2013-12-31 16:30:07',6,1,1,1,'Bij Catun thuis.',4.98044604,51.67755739);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-01-03 07:23:33',108119,35.75,1.629,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-01-13 16:36:38',108605,34.02,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-01-21 19:08:00',109040,30.49,1.629,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-01-29 20:29:17',109460,30.49,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-02-04 17:29:33',109881,29.43,1.629,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-02-15 14:15:28',110336,34.02,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-02-20 16:50:56',110749,26.61,1.629,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-02-27 16:23:29',111201,30.54,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-03-03 19:17:51',111655,27.57,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-03-13 20:14:12',112161,34.81,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-03-22 13:23:08',112612,30.42,1.619,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-03-29 13:09:53',113085,30.31,1.639,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-04-08 16:45:25',113623,34.88,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-04-13 20:02:25',114099,30.61,1.669,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-04-23 16:45:18',114582,31.2,1.689,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-04-29 16:52:18',115018,27.86,1.689,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-05-08 19:10:13',115538,33.6,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-05-15 16:50:28',116052,34.62,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-05-22 18:10:33',116617,34.62,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-05-29 17:01:31',117035,29.07,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-06-06 20:07:42',117593,37.14,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-06-14 14:29:55',118097,34.29,1.699,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-06-20 19:55:40',118545,31.1,1.719,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-06-30 18:02:50',119049,33.48,1.719,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-07-07 21:01:40',119621,37.16,1.709,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-07-13 18:36:39',120070,30.78,1.709,1,'',NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-07-21 16:41:12',120543,31.81,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-07-28 19:05:32',121046,31.5,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-08-05 18:58:54',121586,33.41,1.669,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-08-11 18:32:53',122047,29.19,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-08-20 18:24:19',122539,32.48,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-08-27 12:01:57',122981,30.49,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-09-03 18:04:54',123529,35.26,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-09-12 16:01:37',124053,34.7,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-09-20 17:49:48',124588,33.73,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-09-27 11:37:46',125015,30.28,1.669,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-10-06 18:01:50',125592,37.64,1.679,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-10-13 18:07:59',126157,35.46,1.659,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-10-20 18:12:47',126569,28.42,1.609,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-10-29 18:15:38',127064,34.75,1.589,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-11-04 16:44:21',127566,32.86,1.589,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-11-16 13:27:04',128079,34.78,1.579,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-11-23 15:36:06',128544,31.98,1.579,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-11-28 19:04:04',128856,22.6,1.569,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-12-04 19:24:56',129336,35.45,1.549,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-12-15 16:55:40',129866,37.3,1.509,1,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2014-12-22 07:35:26',130278,29.41,1.469,1,NULL,NULL,NULL);");

                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2015-01-23 07:35:26',130478,14.5,1.419,0,NULL,NULL,NULL);");
                db.execSQL("INSERT INTO Fillup([CarId], [Date], [Odometer], [Volume], [Price], [FullTank], [Note], [Longitude], [Latitude]) VALUES(1,'2015-02-01 13:35:26',130969,31.82,1.419,1,NULL,NULL,NULL);");
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException ex) {
            Log.e("onCreate", ex.getMessage());
        } finally {
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

    //region Backup/restore

    /**
     * Helper to log backup/restore errors and to signal the user that
     * the backup or restore action failed.
     */
    private void backupRestoreError(IOException ex, String message) {
        Log.e("backup_restore", ex.getMessage());
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void restoreDatabase() {
        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite()) {
            final String currentDBPath = mContext.getDatabasePath(DATABASE_NAME).getPath();
            final String backupDBPath = "MoneyPit.bak.sqlite"; // From SD directory.

            final File backupDB = new File(currentDBPath);
            final File currentDB = new File(sd, backupDBPath);

            FileChannel src = null, dst = null;
            try {
                src = new FileInputStream(currentDB).getChannel();
                dst = new FileOutputStream(backupDB).getChannel();

                dst.transferFrom(src, 0, src.size());
                Toast.makeText(mContext, mContext.getString(R.string.restore_ok), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                backupRestoreError(e, mContext.getString(R.string.restore_error));
            } finally {
                try {
                    if (src != null) src.close();
                    if (dst != null) dst.close();
                } catch (IOException e) {
                    backupRestoreError(e, mContext.getString(R.string.restore_error));
                }
            }
        }
    }

    public void backupDatabase() {
        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite()) {
            final String currentDBPath = mContext.getDatabasePath(DATABASE_NAME).getPath();
            String backupDBPath = "MoneyPit.bak.sqlite";
            File currentDB = new File(currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            FileChannel src = null, dst = null;
            try {
                src = new FileInputStream(currentDB).getChannel();
                dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                Toast.makeText(mContext, mContext.getString(R.string.backup_ok), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                backupRestoreError(e, mContext.getString(R.string.backup_error));
            } finally {
                try {
                    if (src != null) src.close();
                    if (dst != null) dst.close();
                } catch (IOException e) {
                    backupRestoreError(e, mContext.getString(R.string.backup_error));
                }
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
                "WHERE Fillup.FullTank = 1 " +
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
                "WHERE Fillup.FullTank = 1 AND Car._id = ?";
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
        String query = "SELECT *, " +
                "Odometer - (IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1), Odometer)) AS Distance, " +
                "CASE WHEN (SELECT CAST(strftime('%s', Date) AS INT) FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1) IS NULL THEN 0 ELSE " +
                "(CAST(strftime('%s', Date) AS INT) - (SELECT CAST(strftime('%s', Date) AS INT) FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1)) / 86400 END AS Days, " +
                "(Odometer - (IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1), Odometer))) / Volume AS Economy, " +
                "Price * Volume AS TotalPrice " +
                "FROM Fillup AS T1 " +
                "WHERE (CarId = ?) AND (? = 0 OR ? = CAST(strftime('%Y', Date) AS INT)) " +
                "ORDER BY Date DESC";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(carId),
                String.valueOf(year),
                String.valueOf(year)};

        SQLiteDatabase db;
        Cursor cursor = null;
        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            if (cursor.moveToFirst()) {
                do {
                    Fillup fillup = new Fillup(cursor);
                    result.add(fillup);

                } while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;
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

        String query1 = "SELECT * FROM " + TABLE_FILLUP + " WHERE (Date < ?) AND (CarId = ?) AND (_id <> ?) ORDER BY Date DESC LIMIT 1";
        String query2 = "SELECT * FROM " + TABLE_FILLUP + " WHERE (Date > ?) AND (CarId = ?) AND (_id <> ?) ORDER BY Date ASC LIMIT 1";
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

        String query = "SELECT Date FROM " + TABLE_FILLUP + " WHERE (CarId = ?) ORDER BY Date ASC LIMIT 1";
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
    public DataPoint[] getFuelCostPerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), SUM(Price * Volume) FROM Fillup WHERE CarId = ? AND CAST(strftime('%Y', Date) AS INT) = ? GROUP BY CAST(strftime('%m', Date) AS INT)";
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
    public DataPoint[] getFuelCostPerKilometerPerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), SUM(Price*Volume) / " +
                "SUM(Odometer - (SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1)) " +
                "FROM Fillup AS T1 WHERE CarId=? AND CAST(strftime('%Y', Date) AS INT) = ?" +
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
    public DataPoint[] getDistancePerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), " +
                "SUM(Odometer - IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1), Odometer)) " +
                "FROM Fillup AS T1 WHERE CarId=? AND CAST(strftime('%Y', Date) AS INT) = ?" +
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
    public DataPoint[] getEconomyPerMonth(int carId, int year) {
        String query = "SELECT CAST(strftime('%m', Date) AS INT), " +
                "SUM(Odometer - IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1), Odometer)) / SUM(Volume)" +
                "FROM Fillup AS T1 WHERE CarId=? AND CAST(strftime('%Y', Date) AS INT) = ?" +
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
     * @return An array of 12 {@link DataPoint} objects containing the information.
     */
    public DataPoint[] getGraphDataPoints(String query, String[] args) {
        DataPoint[] result = new DataPoint[12];
        for (int m = 0; m < 12; m++) {
            result[m] = new DataPoint(m, 0);
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
                        result[m] = new DataPoint(m, cursor.getDouble(1));
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

        String query = "SELECT Odometer - IFNULL((SELECT Odometer FROM Fillup WHERE Date < T1.Date AND CarId = ? ORDER BY Date DESC LIMIT 1), Odometer), " +
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
     * @return true if data was found, false if not.
     */
    public boolean hasData(int carId, int year) {
        SQLiteDatabase db;
        Cursor cursor = null;

        String query = "SELECT COUNT(1) FROM Fillup WHERE CarId= ? AND CAST(strftime('%Y', Date) AS INT) = ?";
        String[] args = new String[]{String.valueOf(carId),
                String.valueOf(year)};

        try {
            db = mDbManager.openDatabase();
            cursor = db.rawQuery(query, args);

            boolean hasData = false;
            if (cursor.moveToFirst()) {
                hasData = cursor.getInt(0) > 0;
            }

            cursor.close();
            cursor = null;
            return hasData;
        } catch (SQLiteException ex) {
            Log.e("hasData", ex.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            mDbManager.closeDatabase();
        }
    }
    //endregion
}
