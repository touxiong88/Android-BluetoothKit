package com.faytech.bluetooth.library.search;

import com.faytech.bluetooth.library.search.response.BluetoothSearchResponse;

public interface IBluetoothSearchHelper {

    void startSearch(BluetoothSearchRequest request, BluetoothSearchResponse response);

    void stopSearch();
}
