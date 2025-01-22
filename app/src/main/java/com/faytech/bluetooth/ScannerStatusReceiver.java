package com.faytech.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScannerStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "SerialPort";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, intent.toString());

        String status = intent.getStringExtra("status");
        if ("insert".equals(status)) {
            Log.d(TAG, "Received scanner insert boardcast");
        } else if ("unplug".equals(status)) {
            Log.d(TAG, "Received scanner unplug broadcast");
        }
    }
}