package com.development.jaba.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Helper class to start location tracking.
 */
public class LocationHelper implements LocationListener {

    final Context mContext;
    final LocationManager mLocationManager;
    boolean mGpsEnabled, mNetworkEnabled, mTrackingStarted;

    /**
     * Constructor. Initializes an instance of the object.
     * This sets up the {@link android.location.LocationManager} and starts tracking the
     * location through the available providers.
     *
     * @param context The {@link android.content.Context}.
     */
    public LocationHelper(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        mNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Stops the location tracking.
     */
    public void stopLocationTracking() {
        if (mLocationManager != null && mTrackingStarted) {
            if (checkSelfPermission()) {
                mLocationManager.removeUpdates(this);
            }
        }
    }

    /**
     * Starts the location tracking using the available location services.
     */
    public void startLocationTracking() {
        try {
            if (checkSelfPermission()) {
                if (mGpsEnabled || mNetworkEnabled) {
                    if (mNetworkEnabled) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                0, 0, this);
                        mTrackingStarted = true;
                    }

                    if (mGpsEnabled) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0, 0, this);
                        mTrackingStarted = true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("LocationTracking", e.getMessage());
        }
    }

    private boolean checkSelfPermission() {
        return !(ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Derived classes should at least override this method to get updated
     * about location changes.
     *
     * @param location The newly recorded {@link android.location.Location}.
     */
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
