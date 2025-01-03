package com.faytech.bluetooth;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import com.faytech.bluetooth.library.BluetoothContext;

import java.nio.charset.StandardCharsets;

public class MyApplication extends Application {

    private static MyApplication instance;
    private static Toast sToast;
    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BluetoothContext.set(this);
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }
    public static void toast(String txt, int duration) {
        String utf8Txt = new String(txt.getBytes(), StandardCharsets.UTF_8);
        sToast.setText(utf8Txt);
        sToast.setDuration(duration);
        sToast.show();
    }
}
