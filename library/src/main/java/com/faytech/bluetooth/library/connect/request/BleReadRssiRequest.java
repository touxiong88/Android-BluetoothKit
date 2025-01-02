package com.faytech.bluetooth.library.connect.request;

import android.bluetooth.BluetoothGatt;

import com.faytech.bluetooth.library.Code;
import com.faytech.bluetooth.library.Constants;
import com.faytech.bluetooth.library.connect.listener.ReadRssiListener;
import com.faytech.bluetooth.library.connect.response.BleGeneralResponse;

public class BleReadRssiRequest extends BleRequest implements ReadRssiListener {

    public BleReadRssiRequest(BleGeneralResponse response) {
        super(response);
    }

    @Override
    public void processRequest() {
        switch (getCurrentStatus()) {
            case Constants.STATUS_DEVICE_DISCONNECTED:
                onRequestCompleted(Code.REQUEST_FAILED);
                break;

            case Constants.STATUS_DEVICE_CONNECTED:
                startReadRssi();
                break;

            case Constants.STATUS_DEVICE_SERVICE_READY:
                startReadRssi();
                break;

            default:
                onRequestCompleted(Code.REQUEST_FAILED);
                break;
        }
    }

    private void startReadRssi() {
        if (!readRemoteRssi()) {
            onRequestCompleted(Code.REQUEST_FAILED);
        } else {
            startRequestTiming();
        }
    }

    @Override
    public void onReadRemoteRssi(int rssi, int status) {
        stopRequestTiming();
        
        if (status == BluetoothGatt.GATT_SUCCESS) {
            putIntExtra(Constants.EXTRA_RSSI, rssi);
            onRequestCompleted(Code.REQUEST_SUCCESS);
        } else {
            onRequestCompleted(Code.REQUEST_FAILED);
        }
    }
}

