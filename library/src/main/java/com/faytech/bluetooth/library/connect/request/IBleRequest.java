package com.faytech.bluetooth.library.connect.request;

import com.faytech.bluetooth.library.connect.IBleConnectDispatcher;

public interface IBleRequest {

    void process(IBleConnectDispatcher dispatcher);

    void cancel();
}
