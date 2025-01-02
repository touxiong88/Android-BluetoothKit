package com.faytech.bluetooth.library.receiver;

import android.content.Context;
import android.content.Intent;

import com.faytech.bluetooth.library.Constants;
import com.faytech.bluetooth.library.receiver.listener.BleConnectStatusChangeListener;
import com.faytech.bluetooth.library.receiver.listener.BluetoothReceiverListener;
import com.faytech.bluetooth.library.utils.BluetoothLog;

import java.util.Arrays;
import java.util.List;


public class BleConnectStatusChangeReceiver extends AbsBluetoothReceiver {

    private static final String[] ACTIONS = {
            Constants.ACTION_CONNECT_STATUS_CHANGED
    };

    protected BleConnectStatusChangeReceiver(IReceiverDispatcher dispatcher) {
        super(dispatcher);
    }

    public static BleConnectStatusChangeReceiver newInstance(IReceiverDispatcher dispatcher) {
        return new BleConnectStatusChangeReceiver(dispatcher);
    }

    @Override
    List<String> getActions() {
        return Arrays.asList(ACTIONS);
    }

    @Override
    boolean onReceive(Context context, Intent intent) {
        String mac = intent.getStringExtra(Constants.EXTRA_MAC);
        int status = intent.getIntExtra(Constants.EXTRA_STATUS, 0);

        BluetoothLog.v(String.format("onConnectStatusChanged for %s, status = %d", mac, status));
        onConnectStatusChanged(mac, status);
        return true;
    }

    private void onConnectStatusChanged(String mac, int status) {
        List<BluetoothReceiverListener> listeners = getListeners(BleConnectStatusChangeListener.class);
        for (BluetoothReceiverListener listener : listeners) {
            listener.invoke(mac, status);
        }
    }
}
