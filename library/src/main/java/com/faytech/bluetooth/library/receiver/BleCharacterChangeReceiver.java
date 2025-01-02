package com.faytech.bluetooth.library.receiver;

import android.content.Context;
import android.content.Intent;

import com.faytech.bluetooth.library.Constants;
import com.faytech.bluetooth.library.receiver.listener.BleCharacterChangeListener;
import com.faytech.bluetooth.library.receiver.listener.BluetoothReceiverListener;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleCharacterChangeReceiver extends AbsBluetoothReceiver {

    private static final String[] ACTIONS = {
            Constants.ACTION_CHARACTER_CHANGED
    };

    protected BleCharacterChangeReceiver(IReceiverDispatcher dispatcher) {
        super(dispatcher);
    }

    public static BleCharacterChangeReceiver newInstance(IReceiverDispatcher dispatcher) {
        return new BleCharacterChangeReceiver(dispatcher);
    }

    @Override
    List<String> getActions() {
        return Arrays.asList(ACTIONS);
    }

    @Override
    boolean onReceive(Context context, Intent intent) {
        String mac = intent.getStringExtra(Constants.EXTRA_MAC);
        UUID service = (UUID) intent.getSerializableExtra(Constants.EXTRA_SERVICE_UUID);
        UUID character = (UUID) intent.getSerializableExtra(Constants.EXTRA_CHARACTER_UUID);
        byte[] value = intent.getByteArrayExtra(Constants.EXTRA_BYTE_VALUE);
        onCharacterChanged(mac, service, character, value);
        return true;
    }

    private void onCharacterChanged(String mac, UUID service, UUID character, byte[] value) {
        List<BluetoothReceiverListener> listeners = getListeners(BleCharacterChangeListener.class);
        for (BluetoothReceiverListener listener : listeners) {
            listener.invoke(mac, service, character, value);
        }
    }
}
