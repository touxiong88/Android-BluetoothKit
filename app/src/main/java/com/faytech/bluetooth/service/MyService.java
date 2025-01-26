package com.faytech.bluetooth.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.faytech.bluetooth.DeviceDetailActivity;
import com.faytech.bluetooth.MyApplication;
import com.faytech.bluetooth.library.utils.BluetoothLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import tp.faytech.serialport.SerialHelper;
import tp.faytech.serialport.bean.ComBean;

public class MyService extends Service {

    private static final String TAG = "MyService";
    private static final int MSG_PLUG_OUT = 1;
    private static final int MSG_PLUGIN_IN = 2;
    private static final long DELAY_TIME_RECEIVE = 5 * 1000L;//5秒倒计时
    private SerialHelper serialHelper;
    private int scannerStatus = 0; // 0:unplug 1:insert
    private static Handler mHandler;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_PLUG_OUT:
                        //接收数据超时，执行逻辑
                        Log.d(TAG,"send unplug  boardcast: ");
                        scannerStatus = 0;
                        Intent plugoutIntent = new Intent();
                        plugoutIntent.setAction("com.faytech.serialport");
                        plugoutIntent.putExtra("status", "unplug");
                        sendBroadcast(plugoutIntent);

                        break;
                    case MSG_PLUGIN_IN:
                        Intent pluginIntent = new Intent();
                        pluginIntent.setAction("com.faytech.serialport");
                        pluginIntent.putExtra("status", "insert");
                        sendBroadcast(pluginIntent);
                        Log.d(TAG,"send insert broadcast: ");

                        try {
                            Intent detailIntent = new Intent(MyService.this, DeviceDetailActivity.class);
                            detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                            startActivity(detailIntent);
                        } catch (Exception e) {
                            Log.e(TAG, " start DeviceDetailActivity Exception", e);
                        }
                        break;
                }
                return false;
            }
        });
        openSerial();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    private void openSerial() {
        serialHelper = new SerialHelper("/dev/ttyS3", 115200) {

            @Override
            protected void onDataReceived(byte[] data) {
                String valueStr = new String(data, StandardCharsets.UTF_8);
                Log.d(TAG,"onDataReceived: valueStr=" + valueStr);
                try {
                    if (valueStr.startsWith("MAC=")) {
                        String macAddress = valueStr.substring(4, 21);
                        BluetoothLog.d("macAddress: " + macAddress);
                        if (scannerStatus == 0) {
                            MyApplication.macAddress = macAddress;
                            // Scanner inserted, send broadcast
                            mHandler.sendEmptyMessage(MSG_PLUGIN_IN);
                            scannerStatus = 1;
                        }
                    }
                    mHandler.removeMessages(MSG_PLUG_OUT);
                    mHandler.sendEmptyMessageDelayed(MSG_PLUG_OUT, DELAY_TIME_RECEIVE);
                } catch (Exception e) {
                    Log.e(TAG, "onDataReceivedException ", e);
                }
            }
        };

        serialHelper.close();
        serialHelper.setPort("/dev/ttyS3");
        serialHelper.setBaudRate("115200");
        serialHelper.setDataBits(8);//8 bit data
        serialHelper.setStopBits(1);//1 bit stop bit
        serialHelper.setFlowCon(0);//NONE

        try {
            serialHelper.open();
        } catch (IOException e) {
            Log.e(TAG, "openSerial IOException", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        serialHelper.close();
        mHandler.removeMessages(MSG_PLUG_OUT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}