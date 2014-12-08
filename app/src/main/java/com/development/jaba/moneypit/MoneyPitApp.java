package com.development.jaba.moneypit;

import android.app.Application;
import android.content.Context;

/**
 * Application derived class to make the application context available
 * to static helper methods.
 * TODO Find another way to do this. This does not feel right...
 */
public class MoneyPitApp extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}