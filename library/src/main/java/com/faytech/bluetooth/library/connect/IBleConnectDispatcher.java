package com.faytech.bluetooth.library.connect;

import com.faytech.bluetooth.library.connect.request.BleRequest;

public interface IBleConnectDispatcher {

    void onRequestCompleted(BleRequest request);
}
