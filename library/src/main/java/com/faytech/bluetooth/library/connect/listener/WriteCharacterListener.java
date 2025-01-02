package com.faytech.bluetooth.library.connect.listener;

import android.bluetooth.BluetoothGattCharacteristic;

public interface WriteCharacterListener extends GattResponseListener {
    void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status, byte[] value);
}
