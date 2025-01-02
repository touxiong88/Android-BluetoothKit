package com.faytech.bluetooth.library.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.faytech.bluetooth.library.BluetoothContext;
import com.faytech.bluetooth.library.receiver.listener.BluetoothReceiverListener;
import com.faytech.bluetooth.library.utils.ListUtils;

import java.util.Collections;
import java.util.List;

public abstract class AbsBluetoothReceiver {

    protected Context mContext;

    protected Handler mHandler;

    protected IReceiverDispatcher mDispatcher;

    protected AbsBluetoothReceiver(IReceiverDispatcher dispatcher) {
        mDispatcher = dispatcher;
        mContext = BluetoothContext.get();
        mHandler = new Handler(Looper.getMainLooper());
    }

    boolean containsAction(String action) {
        List<String> actions = getActions();
        if (!ListUtils.isEmpty(actions) && !TextUtils.isEmpty(action)) {
            return actions.contains(action);
        }
        return false;
    }

    protected List<BluetoothReceiverListener> getListeners(Class<?> clazz) {
        List<BluetoothReceiverListener> listeners = mDispatcher.getListeners(clazz);
        return listeners != null ? listeners : Collections.EMPTY_LIST;
    }

    abstract List<String> getActions();

    abstract boolean onReceive(Context context, Intent intent);
}
