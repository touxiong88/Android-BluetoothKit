package com.faytech.bluetooth;

import com.faytech.bluetooth.library.BluetoothClient;

public class ClientManager {

    private static BluetoothClient mClient;

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(MyApplication.getInstance());
                }
            }
        }
        return mClient;
    }
}
