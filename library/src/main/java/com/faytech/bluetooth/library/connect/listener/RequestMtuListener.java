package com.faytech.bluetooth.library.connect.listener;

public interface RequestMtuListener extends GattResponseListener {
    void onMtuChanged(int mtu, int status);
}
