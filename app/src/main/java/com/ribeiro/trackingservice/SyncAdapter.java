package com.ribeiro.trackingservice;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "RIBEIRO_SyncAdapter";
    // Global variables
    // Define a variable to contain a content resolver instance
    private ContentResolver mContentResolver;
    private Context mContext;
    private String serviceURL;

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
        configureServiceURL();
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
        configureServiceURL();

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
        configureServiceURL();
        Log.d(TAG, "inicio onPerformSync...");
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
                        }
                    }


                }
            }else{
                Log.d(TAG, "la base esta sincronizada.");
            }

        } catch (Exception e) {
            Log.d(TAG, "Error sending Location.");
            Log.d(TAG, e.getMessage());

        }




    }
    public void configureServiceURL(){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String codigoSucursal = sharedPref.getString("codigoSucursal", "");
        Boolean TestMode = sharedPref.getBoolean("TestMode",Boolean.FALSE);
        if (TestMode == Boolean.TRUE){
            serviceURL = sharedPref.getString("servicesUrl","");
            Log.d(TAG,"Test Mode On");
        }else{
            serviceURL = "http://web"+ codigoSucursal.trim()+".ribeiront.net.ar/rest/LocationHistory/";
            Log.d(TAG,"Test Mode Off");
            Log.d(TAG,"Sucursal: " + codigoSucursal);
        }


        //  String serviceURL = syncConnPref; //"http://172.18.6.103/GeolocationTest.NetEnvironment/rest/LocationHistory/";
        Log.d(TAG,"Service URL: "+serviceURL);


    }

}