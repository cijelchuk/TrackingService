package com.ribeiro.trackingservice;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//this is the main class
public class TrackingStatus extends AppCompatActivity {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.ribeiro.trackingservice.datasync.provider";
    // An account type, in the form of a domain name
    //public static final String ACCOUNT_TYPE = "example.com";
    public static final String ACCOUNT_TYPE = "com.ribeiro.trackingservice.datasync";
    // The account name
    public static  final String ACCOUNT = "default_account";
    // Instance fields
    Account mAccount;


    static String TAG = "RIBEIROTRACKING_TrackingStatus";
    // Constants

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static long SYNC_INTERVAL_IN_MINUTES = 15L; //15 minutos es el minimo
    public static long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;




    // Global variables
    // A content resolver for accessing the provider
    ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean hasPermission = false;

        int MY_PERMISSIONS_REQUEST_LOCATION = 0;
        super.onCreate(savedInstanceState);
        // Create the dummy account
        //mAccount = CreateSyncAccount(this);

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

        final TextView texto = (TextView) findViewById(R.id.textView1);
        texto.setText(refresh());

        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final TextView texto = (TextView) findViewById(R.id.textView1);
                texto.setText(refresh());
            }
        });
        final Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                restartServices();
            }
        });



        // Here, thisActivity is the current activity
        // request the permission
        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
            hasPermission = false;
        }

        else {
            // Permission has already been granted
            hasPermission = true;
        }
        if (hasPermission){
            startService(new Intent(this, trackSrv.class));}
        else{
            Log.d(TAG, "Application needs permission to access Location Provider services");
            closeApplication(this);
        }
        // Ensure the right menu is setup
        moveTaskToBack(true);

    }



    private void restartServices() {
        final TextView texto = (TextView) findViewById(R.id.textView1);
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
    public static Account CreateSyncAccount(Context context) {
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
            tmp.append("Location:");
            tmp.append(loc1.getLocation());
            tmp.append(System.getProperty ("line.separator"));
            tmp.append("Message:");
            tmp.append(loc1.getMessage());
            tmp.append(System.getProperty ("line.separator"));
            locationstxt += tmp.toString();
            //locationstxt += String.format().getLocation() + System.getProperty ("line.separator");
        }

        if (locationstxt.isEmpty()){
            locationstxt = "nothing to show";
        }
        return locationstxt;
    }

    public void closeApplication(TrackingStatus view) {
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
}
