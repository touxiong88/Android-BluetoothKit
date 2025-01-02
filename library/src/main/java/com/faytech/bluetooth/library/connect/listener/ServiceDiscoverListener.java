package com.faytech.bluetooth.library.connect.listener;

import com.faytech.bluetooth.library.model.BleGattProfile;

public interface ServiceDiscoverListener extends GattResponseListener {
    void onServicesDiscovered(int status, BleGattProfile profile);
}
