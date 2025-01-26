package com.faytech.bluetooth;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Toast;

import com.faytech.bluetooth.library.BluetoothContext;
import com.faytech.bluetooth.library.utils.BluetoothLog;
import com.faytech.bluetooth.service.MyService;

import java.nio.charset.StandardCharsets;

import android.util.Log;

import java.io.IOException;

import tp.faytech.serialport.SerialHelper;
import tp.faytech.serialport.bean.ComBean;

import android.os.CountDownTimer;


public class MyApplication extends Application {

    private static MyApplication instance;
    private static Toast sToast;
    public static String macAddress = null;

    public static Application getInstance() {
        return instance;
    }
    private static final String TAG = "MyApplication";

    private ScannerStatusReceiver mScannerStatusReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        instance = this;
        BluetoothContext.set(this);
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.faytech.serialport");
        mScannerStatusReceiver = new ScannerStatusReceiver();
        registerReceiver(mScannerStatusReceiver, filter);
        startMyService();
    }

    private void startMyService(){
        Log.d(TAG,"startMyService");
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    public static void toast(String txt, int duration) {
        String utf8Txt = new String(txt.getBytes(), StandardCharsets.UTF_8);
        sToast.setText(utf8Txt);
        sToast.setDuration(duration);
        sToast.show();
    }

}
