package com.faytech.bluetooth.library.connect.request;

import com.faytech.bluetooth.library.Code;
import com.faytech.bluetooth.library.connect.response.BleGeneralResponse;

public class BleRefreshCacheRequest extends BleRequest {

    public BleRefreshCacheRequest(BleGeneralResponse response) {
        super(response);
    }

    @Override
    public void processRequest() {
        refreshDeviceCache();

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                onRequestCompleted(Code.REQUEST_SUCCESS);
            }
        }, 3000);
    }
}
