package com.ribeiro.trackingservice;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    static String TAG = "RIBEIROTRACKING_SyncAdapter";
    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    Context mContext;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        mContext = context;

    }

    /*
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        /*
         * Put the data transfer code here.
         */
        Log.d(TAG, "inicio onPerformSync...");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String serviceURL = "";
        serviceURL = sharedPref.getString("servicesUrl", "");
      //  String serviceURL = syncConnPref; //"http://172.18.6.103/GeolocationTest.NetEnvironment/rest/LocationHistory/";
        Log.d(TAG,"service"+serviceURL);

        //graba el registro en el server
        String endPoint = "";
        if (serviceURL.isEmpty()) {
            Log.d(TAG, "empty ServiceURL");
        }

        RestService rsrv = new RestService();

        rsrv.setHTTPMethod("POST");
        try {
            LocationHistoryDbHelper mDbHelper = new LocationHistoryDbHelper(mContext);
            List<LocationHistory> list = new ArrayList<LocationHistory>();
            list = mDbHelper.loadHandler();
            Log.d(TAG,list.toString());
            if (list.size() > 0) {
                for (LocationHistory loc1 : list) {
                    endPoint = serviceURL + loc1.getDeviceId() + "," + loc1.getId();
                    rsrv.setEndPoint(endPoint);

                    if(rsrv.Send(loc1.getJson())){
                        Log.d(TAG, "location sent:" + loc1.getId());
                        Log.d(TAG, "location deleted:" + loc1.getId());
                        mDbHelper = new LocationHistoryDbHelper(mContext);
                        if (mDbHelper.deleteOneHandler(loc1.getId())) {
                            Log.d(TAG, "deleted ok");
                        };
                    };


                }
            }else{
                Log.d(TAG, "la base esta sincronizada.");
            }

        } catch (Exception e) {
            Log.d(TAG, "Error sending Location.");
            Log.d(TAG, e.getMessage());

        }




    }


}