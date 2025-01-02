package com.faytech.bluetooth.library.receiver.listener;

public abstract class BluetoothReceiverListener extends AbsBluetoothListener {

    abstract public String getName();

    @Override
    final public void onSyncInvoke(Object... args) {
        throw new UnsupportedOperationException();
    }
}
