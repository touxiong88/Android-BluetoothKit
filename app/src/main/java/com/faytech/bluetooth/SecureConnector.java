package com.faytech.bluetooth;


import android.bluetooth.BluetoothDevice;

import com.faytech.bluetooth.library.Code;
import com.faytech.bluetooth.library.connect.response.BleNotifyResponse;
import com.faytech.bluetooth.library.connect.response.BleWriteResponse;
import com.faytech.bluetooth.library.search.SearchResult;
import com.faytech.bluetooth.library.utils.BluetoothLog;
import com.faytech.bluetooth.library.utils.ByteUtils;
import com.faytech.bluetooth.library.utils.UUIDUtils;

import java.util.UUID;

public class SecureConnector {

    private static BluetoothDevice mDevice;

    public static void processStep1(BluetoothDevice device) {
        mDevice = device;

        ClientManager.getClient().write(device.getAddress(), UUIDUtils.makeUUID(0xFE95), UUIDUtils.makeUUID(0x10),
                ByteUtils.fromInt(0xDE85CA90), new BleWriteResponse() {
                    @Override
                    public void onResponse(int code) {
                        if (code == Code.REQUEST_SUCCESS) {
                            processStep2();
                        }
                    }
                });
    }

    public static void processStep2() {
        ClientManager.getClient().notify(mDevice.getAddress(), UUIDUtils.makeUUID(0xFE95),
                UUIDUtils.makeUUID(0x01), new BleNotifyResponse() {
                    @Override
                    public void onNotify(UUID service, UUID character, byte[] value) {

                    }

                    @Override
                    public void onResponse(int code) {

                    }
                });
    }
}
