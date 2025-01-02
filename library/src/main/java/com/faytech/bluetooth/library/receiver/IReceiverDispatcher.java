package com.faytech.bluetooth.library.receiver;

import com.faytech.bluetooth.library.receiver.listener.BluetoothReceiverListener;

import java.util.List;

public interface IReceiverDispatcher {

    List<BluetoothReceiverListener> getListeners(Class<?> clazz);
}
