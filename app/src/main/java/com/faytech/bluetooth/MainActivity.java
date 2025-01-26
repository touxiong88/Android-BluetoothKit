package com.faytech.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.faytech.bluetooth.library.connect.listener.BluetoothStateListener;
import com.faytech.bluetooth.library.search.SearchRequest;
import com.faytech.bluetooth.library.search.SearchResult;
import com.faytech.bluetooth.library.search.response.SearchResponse;
import com.faytech.bluetooth.library.utils.BluetoothLog;
import com.faytech.bluetooth.service.MyService;
import com.faytech.bluetooth.view.PullRefreshListView;
import com.faytech.bluetooth.view.PullToRefreshFrameLayout;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


import tp.faytech.serialport.SerialHelper;
import tp.faytech.serialport.bean.ComBean;

import android.os.CountDownTimer;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private PullToRefreshFrameLayout mRefreshLayout;
    private PullRefreshListView mListView;
    private DeviceListAdapter mAdapter;
    private TextView mTvTitle;
    private List<SearchResult> mDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDevices = new ArrayList<SearchResult>();

        mTvTitle = (TextView) findViewById(R.id.title);

        mRefreshLayout = (PullToRefreshFrameLayout) findViewById(R.id.pulllayout);

        mListView = mRefreshLayout.getPullToRefreshListView();
        mAdapter = new DeviceListAdapter(this);
        mListView.setAdapter(mAdapter);


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "This machine does not support low-power Bluetooth！");
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_COARSE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    break;
                }
            }
        }

        mListView.setOnRefreshListener(new PullRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                searchDevice();
            }

        });

        searchDevice();

        ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));
            }
        });

        startMyService();
    }

    private void startMyService(){
        Log.d(TAG,"startMyService");
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2).build();

        ClientManager.getClient().search(request, mSearchResponse);
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);
            mTvTitle.setText(R.string.string_refreshing);
            mDevices.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);

            }

            if (mDevices.size() > 0) {
                mRefreshLayout.showState(AppConstants.LIST);
            }
        }

        @Override
        public void onSearchStopped() {
            BluetoothLog.w("MainActivity.onSearchStopped");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);

            mTvTitle.setText(R.string.devices);
        }

        @Override
        public void onSearchCanceled() {
            BluetoothLog.w("MainActivity.onSearchCanceled");

            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);

            mTvTitle.setText(R.string.devices);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }
}
