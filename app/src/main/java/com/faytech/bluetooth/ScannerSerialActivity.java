package com.faytech.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


import tp.faytech.serialport.SerialHelper;
import tp.faytech.serialport.bean.ComBean;

import android.os.CountDownTimer;

public class ScannerSerialActivity extends Activity {
    private static final String TAG = "SerialPort";
    private SerialHelper serialHelper;

    private ScannerStatusReceiver mScannerStatusReceiver;
    private MyTimer mMyTimer = new MyTimer();

    private int scannerStatus = 0; // 0:unplug 1:insert


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialHelper.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.faytech.bluetooth");
        mScannerStatusReceiver = new ScannerStatusReceiver();
        registerReceiver(mScannerStatusReceiver, filter);


        serialHelper = new SerialHelper("/dev/ttyS3", 115200) {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String valueStr = new String(comBean.bRec, StandardCharsets.UTF_8);
                        if (valueStr.startsWith("MAC=")) {
                            String macAddress = valueStr.substring(4, 21);
                            Log.d(TAG, "macAddress: " + macAddress);
                            MyApplication.setMacAddress(macAddress);
                            if(scannerStatus == 0) {
                                // scanner insert boardcast
                                Intent intent = new Intent();
                                intent.setAction("com.faytech.bluetooth");
                                intent.putExtra("status", "insert");
                                sendBroadcast(intent);
                                Log.d(TAG, "send insert  boardcast: ");
                            }
                            scannerStatus =1;
                            mMyTimer.reset();
                        }
                    }
                });
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
            throw new RuntimeException(e);
        }

        mMyTimer.start();
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
                    intent.setAction("com.faytech.bluetooth");
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
