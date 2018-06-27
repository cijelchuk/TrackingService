package com.ribeiro.trackingservice;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;



//this is the main class
public class TrackingStatus extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback
{

    // Constants
    // The authority for the sync adapter's content provider
    private static final String AUTHORITY = "com.ribeiro.trackingservice.datasync.provider";
    // An account type, in the form of a domain name
    //public static final String ACCOUNT_TYPE = "example.com";
    private static final String ACCOUNT_TYPE = "com.ribeiro.trackingservice.datasync";
    // The account name
    private static  final String ACCOUNT = "ribeiro_tracking";
    // Instance fields
    private Account mAccount;


    private static final String TAG = "RIBEIRO_Main";
    // Constants

    // Sync interval constants
    private static final long SECONDS_PER_MINUTE = 60L;
    private static long SYNC_INTERVAL_IN_MINUTES = 15L; //15 minutos es el minimo
    private static long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;




    // Global variables
    // A content resolver for accessing the provider
    private ContentResolver mResolver;
    //variables para almacenar los permisos que se solicitan al usuario para ejecutar la app
    private static int REQUEST_LOCATION = 0;
    private static int REQUEST_READ_PHONE_STATE = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean hasPermission = false;


        super.onCreate(savedInstanceState);

        /*
         * Create the dummy account. The code for CreateSyncAccount
         * is listed in the lesson Creating a Sync Adapter
         */

        mAccount = CreateSyncAccount(this);

        // Get the content resolver for your app
        mResolver = getContentResolver();
        /*
         * Turn on periodic syncing
         */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String SyncMinutes= "";
        SyncMinutes = sharedPref.getString("sync_frequency", "");
        if(!Strings.isNullOrEmpty(SyncMinutes)) {
            SYNC_INTERVAL_IN_MINUTES = Long.valueOf(SyncMinutes);
            SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;
        }


        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL);

        setContentView(R.layout.activity_tracking_status);

        final TextView texto = findViewById(R.id.textView1);
        texto.setMovementMethod(new ScrollingMovementMethod());
        texto.setText(refresh());




        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final TextView texto = findViewById(R.id.textView1);
                texto.setText(refresh());
            }
        });
        final Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                restartServices();
            }
        });

        ImageView mImageView = findViewById(R.id.imageRibeiro);
       // mImageView.setImageResource(R.drawable.ic_logoribeiro2);

        checkPermission();
        if (REQUEST_READ_PHONE_STATE == 1 && REQUEST_LOCATION == 1){
            startService(new Intent(this, trackSrv.class));
            // Ensure the right menu is setup
            moveTaskToBack(true);
        }
        showLogin();

    }

    private void showLogin() {
        if(setupInicial()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private boolean setupInicial() {
        //chequeo si esta configurado como TESTMODE y si tiene URL para poder loguearse
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String codigoSucursal = sharedPref.getString("codigoSucursal", "");
        Boolean TestMode = sharedPref.getBoolean("TestMode", Boolean.FALSE);
        String serviceURL = sharedPref.getString("servicesUrl", "");
        if ((TestMode && serviceURL.isEmpty())|| (!TestMode && codigoSucursal.isEmpty()))
        {
            return Boolean.FALSE;
        } else{
            return Boolean.TRUE;
        }


    }


    private void restartServices() {
        final TextView texto = findViewById(R.id.textView1);
        texto.setMovementMethod(new ScrollingMovementMethod());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String SyncMinutes= "";
        String message ="";
        SyncMinutes = sharedPref.getString("sync_frequency", "");
        if(!Strings.isNullOrEmpty(SyncMinutes)) {
            SYNC_INTERVAL_IN_MINUTES = Long.valueOf(SyncMinutes);
            SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;
        }

        try {
            message = "Restarting Periodic Sync with Period " + Long.toString(SYNC_INTERVAL_IN_MINUTES) + " minutes."  + System.getProperty ("line.separator");
            ContentResolver.removePeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY);
            ContentResolver.addPeriodicSync(
                    mAccount,
                    AUTHORITY,
                    Bundle.EMPTY,
                    SYNC_INTERVAL);
            ContentResolver.requestSync(mAccount, AUTHORITY, Bundle.EMPTY);
        }catch (Exception e){
            Log.d(TAG, "Error Restarting Sync Service: "+ e.getMessage());
        }
        try {
            message += "Restarting Tracking Service" +   System.getProperty ("line.separator");
            getApplicationContext().stopService(new Intent(this, trackSrv.class));
            startService(new Intent(this, trackSrv.class));

        }catch (Exception e){
            Log.d(TAG, "Error Restarting Tracking Service: "+ e.getMessage());
        }
        texto.setText(message);
        Log.d(TAG, message);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    private static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account( ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */

        Boolean okacc;
        try {
            okacc = accountManager.addAccountExplicitly(newAccount, null, null);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            okacc = false;
        }

        if (okacc) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d(TAG,"The account exists or some other error occurred.");
            return newAccount;
        }
    }

    private String refresh() {
        List<LocationHistory> list = new ArrayList<LocationHistory>();
        LocationHistoryDbHelper mDbHelper = new LocationHistoryDbHelper(getApplicationContext());
        list = mDbHelper.loadHandler();
        String locationstxt = "";
        Integer i = 0;
        for (LocationHistory loc1 : list) {
            i += 1;
            StringBuilder tmp = new StringBuilder(50); // Using default 16 character size
            tmp.append("Time: ");
            tmp.append(loc1.getDateTime());
            tmp.append(System.getProperty ("line.separator"));
            tmp.append("Location: ");
            tmp.append(loc1.getLocation());
            tmp.append(System.getProperty ("line.separator"));
            tmp.append("Message: ");
            tmp.append(loc1.getMessage());
            tmp.append(System.getProperty ("line.separator"));
            tmp.append("----------");
            tmp.append(System.getProperty ("line.separator"));
            locationstxt += tmp.toString();

        }

        if (locationstxt.isEmpty()){
            locationstxt = "nothing to show";
        }
        return locationstxt;
    }

    private void closeApplication(TrackingStatus view) {
        finish();
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.preferences:
                showPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPreferences() {
        Intent myIntent = new Intent(this, SettingsActivity.class);
        startActivity(myIntent);
    }

    private void checkPermission(){

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION};

        if(!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        else{
            REQUEST_LOCATION = 1;
            REQUEST_READ_PHONE_STATE = 1;
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    restartServices();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "Permission denied.");
                    closeApplication(this);
                }
            }
        }
    }
}
