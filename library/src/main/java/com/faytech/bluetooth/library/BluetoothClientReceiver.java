package com.faytech.bluetooth.library;

import com.faytech.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.faytech.bluetooth.library.connect.response.BleNotifyResponse;
import com.faytech.bluetooth.library.receiver.listener.BluetoothBondListener;
import com.faytech.bluetooth.library.connect.listener.BluetoothStateListener;

import java.util.HashMap;
import java.util.List;

public class BluetoothClientReceiver {

    private HashMap<String, HashMap<String, List<BleNotifyResponse>>> mNotifyResponses;
    private HashMap<String, List<BleConnectStatusListener>> mConnectStatusListeners;
    private List<BluetoothStateListener> mBluetoothStateListeners;
    private List<BluetoothBondListener> mBluetoothBondListeners;
}
