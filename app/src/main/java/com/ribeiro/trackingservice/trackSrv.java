package com.ribeiro.trackingservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.base.Strings;

import java.sql.Time;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class trackSrv extends Service{
    private static final String TAG = "RIBEIRO_trackSrv";
    private LocationManager mLocationManager = null;
    private static int LOCATION_INTERVAL = 600000; //10 minutos
    private static float LOCATION_DISTANCE = 100f; //100 metros la f es de float

    private Boolean timeIsEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String initStr = sharedPref.getString("initialTime", "9");
        Integer init = Integer.valueOf(initStr);
        if ((init < 1 || init > 23)&&(init == 0)){
            init = 9;
            Log.d(TAG,"Initial time is not correct, default value set to 9.");
        }
        Calendar calIni = Calendar.getInstance();

        calIni.set(Calendar.HOUR_OF_DAY,init);
        calIni.set(Calendar.MINUTE,0);
        calIni.set(Calendar.SECOND,0);
        calIni.set(Calendar.MILLISECOND,0);
        Date initialTime = calIni.getTime();

        Calendar calEnd = Calendar.getInstance();
        String endStr = sharedPref.getString("endingTime", "18");
        Integer end = Integer.valueOf(endStr);
        if ((end < 1 || end > 23)&&(end ==0)){
            end = 18;
            Log.d(TAG,"Ending time is not correct, default value set to 18.");
        }
        if (init > end){
            init = 9;
            end = 18;
            Log.d(TAG,"Initial / Ending time is not correct, default period set 9-18.");
        }
        calEnd.set(Calendar.HOUR_OF_DAY,end);
        calEnd.set(Calendar.MINUTE,0);
        calEnd.set(Calendar.SECOND,0);
        calEnd.set(Calendar.MILLISECOND,0);
        Date endingTime = calEnd.getTime();

        Calendar now = Calendar.getInstance();
        Date nowDate = now.getTime();


        Log.d(TAG,"initial time:"+ initialTime.toString());
        Log.d(TAG,"ending time:"+ endingTime.toString());

        String workingDays = sharedPref.getString("workingDays", "");
        Boolean dayOfWeekOK = Boolean.FALSE;
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        switch (workingDays){
            case "LD": //lunes a domingo
                dayOfWeekOK = Boolean.TRUE;
                break;

            case "LS": //lunes a sabados
                if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.SATURDAY){
                    dayOfWeekOK = Boolean.TRUE;
                }else{
                    return Boolean.FALSE;
                }
                break;
            default: //Lunes a Viernes
                if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY){
                    dayOfWeekOK = Boolean.TRUE;
                }else{
                    return Boolean.FALSE;
                }
        }
        String weekdays[] = new DateFormatSymbols(Locale.US).getWeekdays();
        Log.d(TAG, "Working day enabled: "+ workingDays + " today is: " + weekdays[dayOfWeek]);
        if (dayOfWeekOK) {
            if (initialTime.before(nowDate) && endingTime.after(nowDate)) {
                Log.d(TAG, "Traking period is enabled.");
                return Boolean.TRUE;
            } else {
                Log.d(TAG, "Tracking period is disabled.");
                return Boolean.FALSE;
            }
        }
        //algo raroooo
        return Boolean.FALSE;
    }
    private void saveLocationError(Location location, String message){
        //check if time is enabled
        if (timeIsEnabled()) {
            //creates a LocationHistory Class with the assigned Location
            LocationHistory loc = new LocationHistory(location, message, getApplicationContext());
            //inserta el registro en la bb local.
            try {

                // Gets the data repository in write mode
                LocationHistoryDbHelper mDbHelper = new LocationHistoryDbHelper(getApplicationContext());
                mDbHelper.addHandler(loc);
                Log.d(TAG, "location saved:" + loc.getId());
            } catch (Exception e) {
                Log.d(TAG, "Error inserting Location.saveLocationError");
                Log.d(TAG, e.getMessage());
            }
        }else{
            Log.d(TAG,"Time not enabled to log location.");
        }
    }

    private void saveLocation(Location location){
        //check if time is enabled
        if (timeIsEnabled()) {
            //creates a LocationHistory Class with the assigned Location
            String message = "ok";
            LocationHistory loc = new LocationHistory(location, message, getApplicationContext());

            //inserta el registro en la bb local.
            try {

                // Gets the data repository in write mode
                LocationHistoryDbHelper mDbHelper = new LocationHistoryDbHelper(getApplicationContext());
                mDbHelper.addHandler(loc);
                Log.d(TAG, "location saved:" + loc.getId());
            } catch (Exception e) {
                Log.d(TAG, "Error inserting Location.");
                Log.d(TAG, e.getMessage());
            }
        }else{
            Log.d(TAG,"Time not enabled to log location.");
        }
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;



        LocationListener(String provider)
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
//kjhk
    private LocationListener[] mLocationListeners = new LocationListener[] {
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
