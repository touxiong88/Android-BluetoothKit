package com.faytech.bluetooth.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.faytech.bluetooth.library.connect.BleConnectManager;
import com.faytech.bluetooth.library.connect.options.BleConnectOptions;
import com.faytech.bluetooth.library.connect.response.BleGeneralResponse;
import com.faytech.bluetooth.library.search.BluetoothSearchManager;
import com.faytech.bluetooth.library.search.SearchRequest;
import com.faytech.bluetooth.library.utils.BluetoothLog;

import java.util.UUID;

import static com.faytech.bluetooth.library.Constants.CODE_CLEAR_REQUEST;
import static com.faytech.bluetooth.library.Constants.CODE_CONNECT;
import static com.faytech.bluetooth.library.Constants.CODE_DISCONNECT;
import static com.faytech.bluetooth.library.Constants.CODE_INDICATE;
import static com.faytech.bluetooth.library.Constants.CODE_NOTIFY;
import static com.faytech.bluetooth.library.Constants.CODE_READ;
import static com.faytech.bluetooth.library.Constants.CODE_READ_DESCRIPTOR;
import static com.faytech.bluetooth.library.Constants.CODE_READ_RSSI;
import static com.faytech.bluetooth.library.Constants.CODE_REFRESH_CACHE;
import static com.faytech.bluetooth.library.Constants.CODE_REQUEST_MTU;
import static com.faytech.bluetooth.library.Constants.CODE_SEARCH;
import static com.faytech.bluetooth.library.Constants.CODE_STOP_SESARCH;
import static com.faytech.bluetooth.library.Constants.CODE_UNNOTIFY;
import static com.faytech.bluetooth.library.Constants.CODE_WRITE;
import static com.faytech.bluetooth.library.Constants.CODE_WRITE_DESCRIPTOR;
import static com.faytech.bluetooth.library.Constants.CODE_WRITE_NORSP;
import static com.faytech.bluetooth.library.Constants.EXTRA_BYTE_VALUE;
import static com.faytech.bluetooth.library.Constants.EXTRA_CHARACTER_UUID;
import static com.faytech.bluetooth.library.Constants.EXTRA_DESCRIPTOR_UUID;
import static com.faytech.bluetooth.library.Constants.EXTRA_MAC;
import static com.faytech.bluetooth.library.Constants.EXTRA_MTU;
import static com.faytech.bluetooth.library.Constants.EXTRA_OPTIONS;
import static com.faytech.bluetooth.library.Constants.EXTRA_REQUEST;
import static com.faytech.bluetooth.library.Constants.EXTRA_SERVICE_UUID;
import static com.faytech.bluetooth.library.Constants.EXTRA_TYPE;

public class BluetoothServiceImpl extends IBluetoothService.Stub implements Handler.Callback {

    private static BluetoothServiceImpl sInstance;

    private Handler mHandler;

    private BluetoothServiceImpl() {
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static BluetoothServiceImpl getInstance() {
        if (sInstance == null) {
            synchronized (BluetoothServiceImpl.class) {
                if (sInstance == null) {
                    sInstance = new BluetoothServiceImpl();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void callBluetoothApi(int code, Bundle args, final IResponse response) throws RemoteException {
        Message msg = mHandler.obtainMessage(code, new BleGeneralResponse() {

            @Override
            public void onResponse(int code, Bundle data) {
                if (response != null) {
                    if (data == null) {
                        data = new Bundle();
                    }
                    try {
                        response.onResponse(code, data);
                    } catch (Throwable e) {
                        BluetoothLog.e(e);
                    }
                }
            }
        });

        args.setClassLoader(getClass().getClassLoader());
        msg.setData(args);
        msg.sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        Bundle args = msg.getData();
        String mac = args.getString(EXTRA_MAC);
        UUID service = (UUID) args.getSerializable(EXTRA_SERVICE_UUID);
        UUID character = (UUID) args.getSerializable(EXTRA_CHARACTER_UUID);
        UUID descriptor = (UUID) args.getSerializable(EXTRA_DESCRIPTOR_UUID);
        byte[] value = args.getByteArray(EXTRA_BYTE_VALUE);
        BleGeneralResponse response = (BleGeneralResponse) msg.obj;

        switch (msg.what) {
            case CODE_CONNECT:
                BleConnectOptions options = args.getParcelable(EXTRA_OPTIONS);
                BleConnectManager.connect(mac, options, response);
                break;

            case CODE_DISCONNECT:
                BleConnectManager.disconnect(mac);
                break;

            case CODE_READ:
                BleConnectManager.read(mac, service, character, response);
                break;

            case CODE_WRITE:
                BleConnectManager.write(mac, service, character, value, response);
                break;

            case CODE_WRITE_NORSP:
                BleConnectManager.writeNoRsp(mac, service, character, value, response);
                break;

            case CODE_READ_DESCRIPTOR:
                BleConnectManager.readDescriptor(mac, service, character, descriptor, response);
                break;

            case CODE_WRITE_DESCRIPTOR:
                BleConnectManager.writeDescriptor(mac, service, character, descriptor, value, response);
                break;

            case CODE_NOTIFY:
                BleConnectManager.notify(mac, service, character, response);
                break;

            case CODE_UNNOTIFY:
                BleConnectManager.unnotify(mac, service, character, response);
                break;

            case CODE_READ_RSSI:
                BleConnectManager.readRssi(mac, response);
                break;

            case CODE_SEARCH:
                SearchRequest request = args.getParcelable(EXTRA_REQUEST);
                BluetoothSearchManager.search(request, response);
                break;

            case CODE_STOP_SESARCH:
                BluetoothSearchManager.stopSearch();
                break;

            case CODE_INDICATE:
                BleConnectManager.indicate(mac, service, character, response);
                break;

            case CODE_REQUEST_MTU:
                int mtu = args.getInt(EXTRA_MTU);
                BleConnectManager.requestMtu(mac, mtu, response);
                break;

            case CODE_CLEAR_REQUEST:
                int clearType = args.getInt(EXTRA_TYPE, 0);
                BleConnectManager.clearRequest(mac, clearType);
                break;

            case CODE_REFRESH_CACHE:
                BleConnectManager.refreshCache(mac);
                break;
        }
        return true;
    }
}
