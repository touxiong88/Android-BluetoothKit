package com.faytech.bluetooth.library.connect.request;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.faytech.bluetooth.library.Code;
import com.faytech.bluetooth.library.Constants;
import com.faytech.bluetooth.library.connect.listener.WriteCharacterListener;
import com.faytech.bluetooth.library.connect.response.BleGeneralResponse;

import java.util.UUID;

public class BleWriteRequest extends BleRequest implements WriteCharacterListener {

    private UUID mServiceUUID;
    private UUID mCharacterUUID;
    private byte[] mBytes;

    public BleWriteRequest(UUID service, UUID character, byte[] bytes, BleGeneralResponse response) {
        super(response);
        mServiceUUID = service;
        mCharacterUUID = character;
        mBytes = bytes;
    }

    @Override
    public void processRequest() {
        switch (getCurrentStatus()) {
            case Constants.STATUS_DEVICE_DISCONNECTED:
                onRequestCompleted(Code.REQUEST_FAILED);
                break;

            case Constants.STATUS_DEVICE_CONNECTED:
                startWrite();
                break;

            case Constants.STATUS_DEVICE_SERVICE_READY:
                startWrite();
                break;

            default:
                onRequestCompleted(Code.REQUEST_FAILED);
                break;
        }
    }

    private void startWrite() {
        if (!writeCharacteristic(mServiceUUID, mCharacterUUID, mBytes)) {
            onRequestCompleted(Code.REQUEST_FAILED);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status, byte[] value) {
        stopRequestTiming();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            onRequestCompleted(Code.REQUEST_SUCCESS);
        } else {
            onRequestCompleted(Code.REQUEST_FAILED);
        }
    }
}
