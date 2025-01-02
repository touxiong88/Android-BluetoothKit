package com.faytech.bluetooth.library.connect.listener;

public interface DisconnectListener extends GattResponseListener {
    void onDisconnected();
}
