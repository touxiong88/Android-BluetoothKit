package com.faytech.bluetooth.library.connect.listener;

public interface ReadRssiListener extends GattResponseListener {
    void onReadRemoteRssi(int rssi, int status);
}
