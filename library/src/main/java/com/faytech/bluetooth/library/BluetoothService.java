package com.faytech.bluetooth.library;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.faytech.bluetooth.library.utils.BluetoothLog;

public class BluetoothService extends Service {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothLog.v(String.format("BluetoothService onCreate"));
        mContext = getApplicationContext();
        BluetoothContext.set(mContext);
    }

    @Override
    public IBinder onBind(Intent intent) {
        BluetoothLog.v(String.format("BluetoothService onBind"));
        return BluetoothServiceImpl.getInstance();
    }
}
