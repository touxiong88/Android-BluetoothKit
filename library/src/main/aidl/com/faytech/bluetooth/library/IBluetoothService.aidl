// IBluetoothManager.aidl
package com.faytech.bluetooth.library;

// Declare any non-default types here with import statements

import com.faytech.bluetooth.library.IResponse;

interface IBluetoothService {
    void callBluetoothApi(int code, inout Bundle args, IResponse response);
}
