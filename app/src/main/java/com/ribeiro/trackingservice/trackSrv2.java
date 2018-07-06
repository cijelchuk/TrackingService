package com.ribeiro.trackingservice;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.common.base.Strings;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class trackSrv2 extends Service {
    private LocationRequest mLocationRequest;
    private static final String TAG = "RIBEIRO_trackSrv2";
    private static int LOCATION_INTERVAL = 600000; //10 minutos
    private static float LOCATION_DISTANCE = 100f; //100 metros la f es de float

    private Boolean timeIsEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String initStr = sharedPref.getString("initialTime", "9");
        Integer init = Integer.valueOf(initStr);
        if ((init < 1 || init > 23) && (init == 0)) {
            init = 9;
            Log.d(TAG, "Initial time is not correct, default value set to 9.");
        }
        Calendar calIni = Calendar.getInstance();

        calIni.set(Calendar.HOUR_OF_DAY, init);
        calIni.set(Calendar.MINUTE, 0);
        calIni.set(Calendar.SECOND, 0);
        calIni.set(Calendar.MILLISECOND, 0);
        Date initialTime = calIni.getTime();

        Calendar calEnd = Calendar.getInstance();
        String endStr = sharedPref.getString("endingTime", "18");
        Integer end = Integer.valueOf(endStr);
        if ((end < 1 || end > 23) && (end == 0)) {
            end = 18;
            Log.d(TAG, "Ending time is not correct, default value set to 18.");
        }
        if (init > end) {
            init = 9;
            end = 18;
            Log.d(TAG, "Initial / Ending time is not correct, default period set 9-18.");
        }
        calEnd.set(Calendar.HOUR_OF_DAY, end);
        calEnd.set(Calendar.MINUTE, 0);
        calEnd.set(Calendar.SECOND, 0);
        calEnd.set(Calendar.MILLISECOND, 0);
        Date endingTime = calEnd.getTime();

        Calendar now = Calendar.getInstance();
        Date nowDate = now.getTime();


        Log.d(TAG, "initial time:" + initialTime.toString());
        Log.d(TAG, "ending time:" + endingTime.toString());

        String workingDays = sharedPref.getString("workingDays", "");
        Boolean dayOfWeekOK = Boolean.FALSE;
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        switch (workingDays) {
            case "LD": //lunes a domingo
                dayOfWeekOK = Boolean.TRUE;
                break;

            case "LS": //lunes a sabados
                if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.SATURDAY) {
                    dayOfWeekOK = Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
                break;
            default: //Lunes a Viernes
                if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
                    dayOfWeekOK = Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
        }
        String weekdays[] = new DateFormatSymbols(Locale.US).getWeekdays();
        Log.d(TAG, "Working day enabled: " + workingDays + " today is: " + weekdays[dayOfWeek]);
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

    private void saveLocationError(Location location, String message) {
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
        } else {
            Log.d(TAG, "Time not enabled to log location.");
        }
    }

    private void saveLocation(Location location) {
        //check if time is enabled
        if (timeIsEnabled()) {
            //creates a LocationHistory Class with the assigned Location
            String message = "provider: " + location.getProvider();
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
        } else {
            Log.d(TAG, "Time not enabled to log location.");
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String GPSDistance = "";
        String GPSTime = "";
        GPSDistance = sharedPref.getString("GPSDistance", "");
        GPSTime = sharedPref.getString("GPSTime", "");
        Log.d(TAG, "created Listener with parameters Distance: " + GPSDistance + "meters & Time: " + GPSTime + " minute.") ;
        if(!Strings.isNullOrEmpty(GPSDistance)) {
            LOCATION_DISTANCE = Float.valueOf(GPSDistance);
        }
        if(!Strings.isNullOrEmpty(GPSTime)) {
            LOCATION_INTERVAL = Integer.valueOf(GPSTime)*60000; //convierto minutos a milisegundos
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL / 2);
        mLocationRequest.setSmallestDisplacement(LOCATION_DISTANCE);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "missing permission");
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d(TAG, msg);

        saveLocation(location);
    }
}
