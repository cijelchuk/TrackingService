package com.ribeiro.trackingservice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.Strings;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.UUID;

public class trackSrv extends Service{
    private static final String TAG = "RIBEIROTRACKING_trackSrv";
    private LocationManager mLocationManager = null;
    private static int LOCATION_INTERVAL = 600000; //10 minutos
    private static float LOCATION_DISTANCE = 100f; //100 metros la f es de float

    private void saveLocationError(Location location, String message){
        //creates a LocationHistory Class with the assigned Location
        LocationHistory loc = new LocationHistory(location, message, getApplicationContext());
        //inserta el registro en la bb local.
        try {

            // Gets the data repository in write mode
            LocationHistoryDbHelper mDbHelper = new LocationHistoryDbHelper(getApplicationContext());
            mDbHelper.addHandler(loc);
            Log.d(TAG, "location saved:"+loc.getId());
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error inserting Location.saveLocationError");
            Log.d(TAG, e.getMessage());
        }
    }

    private void saveLocation(Location location){
        //creates a LocationHistory Class with the assigned Location
        String message = "ok";
        LocationHistory loc = new LocationHistory(location, message, getApplicationContext());

        //inserta el registro en la bb local.
        try {

            // Gets the data repository in write mode
            LocationHistoryDbHelper mDbHelper = new LocationHistoryDbHelper(getApplicationContext());
            mDbHelper.addHandler(loc);
            Log.d(TAG, "location saved:"+loc.getId());
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error inserting Location.");
            Log.d(TAG, e.getMessage());
        }
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;



        public LocationListener(String provider)
        {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.d(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            saveLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {

            Location location = new Location(provider);
            location.setLatitude(0);
            location.setLongitude(0);
            mLastLocation.set(location);
            Log.d(TAG, "onProviderDisabled: " + provider);
            saveLocationError(location,"Provider Disabled" + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String GPSDistance = "";
        String GPSTime = "";
        GPSDistance = sharedPref.getString("GPSDistance", "");
        GPSTime = sharedPref.getString("GPSTime", "");
        if(!Strings.isNullOrEmpty(GPSDistance)) {
            LOCATION_DISTANCE = Float.valueOf(GPSDistance);
        }
        if(!Strings.isNullOrEmpty(GPSTime)) {
            LOCATION_INTERVAL = Integer.valueOf(GPSTime)*60000; //convierto minutos a milisegundos
        }
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            String message = "fail to request location update, ignore"+ ex.getMessage();
            Log.e(TAG, message);

            Location location= new Location("NETWORK_PROVIDER");
            location.setLongitude(0);
            location.setLatitude(0);
            saveLocationError(location,"Provider Disabled:" + message);

        } catch (IllegalArgumentException ex) {
            String message = "network provider does not exist, " + ex.getMessage();
            Log.e(TAG, message);
            Location location= new Location("NETWORK_PROVIDER");
            location.setLongitude(0);
            location.setLatitude(0);
            saveLocationError(location,"Provider Disabled:" + message);

        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            String message = "fail to request location update, ignore"+ ex.getMessage();
            Log.e(TAG, message);
            Location location= new Location("GPS_PROVIDER");
            location.setLongitude(0);
            location.setLatitude(0);
            saveLocationError(location,"Provider Disabled:" + message);

        } catch (IllegalArgumentException ex) {
            String message = "gps provider does not exist " + ex.getMessage();
            Log.e(TAG, message);
            Location location= new Location("GPS_PROVIDER");
            location.setLongitude(0);
            location.setLatitude(0);
            saveLocationError(location,"Provider Disabled:" + message);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.e(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


}
