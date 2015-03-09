package com.development.jaba.moneypit;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Application derived class with some static helper methods.
 */
public class MoneyPitApp extends Application {

    private static Context mContext;
    private final String TAG = "APP_STARTUP";
    private final String ERR_FOLDER = "Failed to create app folders.";

    /**
     * Checks to see whether or not the necessary folders exists on the
     * external storage. If not it creates it.
     */
    private void checkFolders() {
        File f = new File(Environment.getExternalStorageDirectory() + "/MoneyPit");
        if (!f.exists() || (f.exists() && !f.isDirectory())) {
            if (!f.mkdir()) {
                Log.e(TAG, ERR_FOLDER);
                return;
            }
        }
        f = new File(Environment.getExternalStorageDirectory() + "/MoneyPit/mapcache");
        if (!f.exists() || (f.exists() && !f.isDirectory())) {
            if (!f.mkdir()) {
                Log.e(TAG, ERR_FOLDER);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        checkFolders();
    }

    public static Context getContext(){
        return mContext;
    }
}