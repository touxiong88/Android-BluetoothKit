package com.inuker.bluetooth;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothContext;

import java.nio.charset.StandardCharsets;

/**
 * Created by dingjikerbo on 2016/8/27.
 */
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

    }
    public static void toast(String txt, int duration) {
        String utf8Txt = new String(txt.getBytes(), StandardCharsets.UTF_8);
        sToast.setText(utf8Txt);
        sToast.setDuration(duration);
        sToast.show();
    }
}
