package com.faytech.bluetooth;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Toast;

import com.faytech.bluetooth.library.BluetoothContext;

import java.nio.charset.StandardCharsets;
import android.util.Log;

import java.io.IOException;

import tp.faytech.serialport.SerialHelper;
import tp.faytech.serialport.bean.ComBean;

import android.os.CountDownTimer;


public class MyApplication extends Application {

    private static MyApplication instance;
    private static Toast sToast;
    private static  String macAddress = null;
    public static Application getInstance() {
        return instance;
    }
    private static final String TAG = "SerialPort";
    private SerialHelper serialHelper;

    private ScannerStatusReceiver mScannerStatusReceiver;
    private MyTimer mMyTimer = new MyTimer();

    private int scannerStatus = 0; // 0:unplug 1:insert

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BluetoothContext.set(this);
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.faytech.serialport");
        mScannerStatusReceiver = new ScannerStatusReceiver();
        registerReceiver(mScannerStatusReceiver, filter);

        serialHelper = new SerialHelper("/dev/ttyS3", 115200) {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                String valueStr = new String(comBean.bRec, StandardCharsets.UTF_8);
                if (valueStr.startsWith("MAC=")) {
                    String macAddress = valueStr.substring(4, 21);
                    Log.d(TAG, "macAddress: " + macAddress);
                    if (scannerStatus == 0) {
                        MyApplication.setMacAddress(macAddress);
                        // Scanner inserted, send broadcast
                        Intent intent = new Intent();
                        intent.setAction("com.faytech.serialport");
                        intent.putExtra("status", "insert");
                        sendBroadcast(intent);
                        Log.d(TAG, "send insert broadcast: ");
                        scannerStatus = 1;
                        mMyTimer.reset();
                    }
                }
            }
        };

        try {
            serialHelper.open();
        } catch (IOException e) {
            Log.e(TAG, "Error opening serial port", e);
        }

        mMyTimer.start();
    }
    public static void toast(String txt, int duration) {
        String utf8Txt = new String(txt.getBytes(), StandardCharsets.UTF_8);
        sToast.setText(utf8Txt);
        sToast.setDuration(duration);
        sToast.show();
    }

    public static void setMacAddress(String mac) {
        macAddress = mac;
    }

    public static String getMacAddress() {
        return macAddress;
    }

    public class MyTimer {
        private CountDownTimer countDownTimer;
        private long millisInFuture = 5000; // 5秒
        private long countDownInterval = 1000; // 每秒回调一次
        public void start() {
            countDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

                @Override
                public void onTick(long millisUntilFinished) {
                    // every senond call this method
                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent();
                    intent.setAction("com.faytech.serialport");
                    intent.putExtra("status", "unplug");
                    sendBroadcast(intent);
                    Log.d(TAG, "send unplug  boardcast: ");
                    scannerStatus = 0;
                }
            }.start();
        }

        public void stop() {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }

        public void reset() {
            stop();
            start();
        }
    }
}
