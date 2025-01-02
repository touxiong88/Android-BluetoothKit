package com.faytech.bluetooth.library.receiver;

import com.faytech.bluetooth.library.receiver.listener.BluetoothReceiverListener;


public interface IBluetoothReceiver {

    void register(BluetoothReceiverListener listener);
}
