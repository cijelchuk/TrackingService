package com.ribeiro.trackingservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {
    private static final String TAG = "RIBEIRO_BootUpReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, TrackingStatus.class);  //MyActivity can be anything which you want to start on bootup...
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        //context.startService(new Intent(context, trackSrv.class));
        Log.d(TAG, "Servicio iniciado.");
    }

}