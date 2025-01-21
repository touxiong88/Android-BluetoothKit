package com.faytech.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.faytech.bluetooth.library.RuntimeChecker;
import com.faytech.bluetooth.library.connect.IBleConnectWorker;
import com.faytech.bluetooth.library.connect.listener.IBluetoothGattResponse;
import com.faytech.bluetooth.library.BluetoothClient;
import com.faytech.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.faytech.bluetooth.library.connect.options.BleConnectOptions;
import com.faytech.bluetooth.library.connect.request.BleNotifyRequest;
import com.faytech.bluetooth.library.connect.response.BleConnectResponse;
import com.faytech.bluetooth.library.connect.BleConnectWorker;
import com.faytech.bluetooth.library.connect.response.BleGeneralResponse;
import com.faytech.bluetooth.library.connect.response.BleNotifyResponse;
import com.faytech.bluetooth.library.connect.response.BleWriteResponse;
import com.faytech.bluetooth.library.connect.response.BluetoothGattResponse;
import com.faytech.bluetooth.library.model.BleGattProfile;
import com.faytech.bluetooth.library.model.BleGattService;
import com.faytech.bluetooth.library.model.BleGattCharacter;
import com.faytech.bluetooth.library.search.SearchResult;
import com.faytech.bluetooth.library.utils.BluetoothLog;
import com.faytech.bluetooth.library.utils.BluetoothUtils;

import static com.faytech.bluetooth.library.Constants.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class DeviceDetailActivity extends Activity {
    private TextView mTvTitle;
    private TextView mTips;
    private ProgressBar mPbar;
    private ListView mListView;
    private Button unlockBtn,lockBtn;
    private DeviceDetailAdapter mAdapter;

    private SearchResult mResult;

    private BluetoothDevice mDevice;

    private boolean mConnected;
    private String mac;
    RuntimeChecker runtimeChecker;
    UUID serviceUuid,notifyUuid,writeUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail_activity);

        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        mResult = intent.getParcelableExtra("device");

        mDevice = BluetoothUtils.getRemoteDevice(mac);

        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(mDevice.getAddress());
        mTips = findViewById(R.id.tv_tips);
        mPbar = (ProgressBar) findViewById(R.id.pbar);
        unlockBtn = (Button) findViewById(R.id.btn_unlock);
        lockBtn = (Button) findViewById(R.id.btn_lock);
        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new DeviceDetailAdapter(this, mDevice);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mConnected) {
                    return;
                }
                DetailItem item = (DetailItem) mAdapter.getItem(position);
                if (item.type == DetailItem.TYPE_CHARACTER) {
                    BluetoothLog.v(String.format("click service = %s, character = %s", item.service, item.uuid));
                    startCharacterActivity(item.service, item.uuid);
                }
            }
        });

        unlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothLog.d("Scanner unlock");
                unlockScanner();
            }
        });

        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothLog.d("Scanner lock");
                lockScanner();
            }
        });

        ClientManager.getClient().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);

        connectDeviceIfNeeded();
    }

    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);
            connectDeviceIfNeeded();
        }
    };

    private void startCharacterActivity(UUID service, UUID character) {
        Intent intent = new Intent(this, CharacterActivity.class);
        intent.putExtra("mac", mDevice.getAddress());
        intent.putExtra("service", service);
        intent.putExtra("character", character);
        startActivity(intent);
    }

    private void connectDevice() {
        mTvTitle.setText(String.format("%s%s", getString(R.string.connecting), mDevice.getAddress()));
        mPbar.setVisibility(View.VISIBLE);
        mTips.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        ClientManager.getClient().connect(mDevice.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                //BluetoothLog.v(String.format("profile:\n%s", profile));

                mTvTitle.setText(String.format("%s", mDevice.getAddress()));
                mPbar.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                unlockBtn.setVisibility(View.VISIBLE);

                if (code == REQUEST_SUCCESS) {
                    mAdapter.setGattProfile(profile);
                    for (BleGattService service : profile.getServices()) {
                        serviceUuid = service.getUUID();
                        BluetoothLog.d("Service UUID: " + serviceUuid);

                        for (BleGattCharacter characteristic : service.getCharacters()) {

                            BluetoothLog.d("Characteristic UUID: " + characteristic.getUuid() + "  PROPERTY: " + characteristic.getProperty());
                            if ((characteristic.getProperty() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                notifyUuid = characteristic.getUuid();
                                //characteristic.setUuid(new ParcelUuid(notifyUuid));
                                BluetoothLog.d("Notify Characteristic UUID: " + notifyUuid);
                            } else if ((characteristic.getProperty() & (BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT + BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                                writeUuid = characteristic.getUuid();
                                BluetoothLog.d("Write Characteristic UUID: " + writeUuid);
                            }
                        }
                    }
/*
                    found service and characteristic
                    BleGattService service = profile.getService(serviceUuid);//your_service_uuid 722e0001-4553-4523-5539-35022233cd4e
                    BluetoothGattCharacteristic notifyCharacteristic = service.getCharacteristic(notifyUuid);//your notify uuid 722e0003-4553-4523-5539-35022233cd4e
                    BluetoothGattCharacteristic writeCharacteristic =  service.getCharacteristic(UUID.fromString("722e0002-4553-4523-5539-35022233cd4e"));
                     set notify
                    mBluetoothGatt.setCharacteristicNotification(notifyCharacteristic, true);
                    BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
*/


                    ClientManager.getClient().notify(mac, serviceUuid, notifyUuid, new BleNotifyResponse() {
                        @Override
                        public void onNotify(UUID service, UUID character, byte[] data) {//notify receive value

                            if (data.length == 6 && data[0] == 0x56 && data[5] == 0x76) {//battery charge info 1byte head + 2 level + 1 charge status + 1CRC+ 1 tail
                                // battery level
                                int batteryLevel = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 3)).getShort() & 0xFFFF;
                                BluetoothLog.d("Battery level: " + batteryLevel + " %");

                                short level = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 3)).order(ByteOrder.BIG_ENDIAN).getShort();
                                byte chargingStatus = data[3];
                                byte crc = data[4];

                                @SuppressLint("DefaultLocale") String formattedData = String.format("Battery level: %d , Charging Status:  %d, CRC: 0x%02X", level, chargingStatus, crc);

//                    BluetoothLog.d(String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, formattedData));
                                BluetoothLog.d(formattedData);
                                MyApplication.toast(formattedData,0);
                                logTv(formattedData);

                            }else {
                                String valueStr = new String(data, StandardCharsets.UTF_8);
                                String modifiedStr = valueStr.substring(1, valueStr.length() - 1);//remove head tail

//                    BluetoothLog.d(String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, modifiedStr));
                                MyApplication.toast(" QR: " + modifiedStr,0);
                                logTv("QR: " + modifiedStr);
                            }
                        }

                        @Override
                        public void onResponse(int code) {
                            mPbar.setVisibility(View.GONE);
                            mTips.setVisibility(View.VISIBLE);
                            mListView.setVisibility(View.GONE);
                            if (code == REQUEST_SUCCESS) {

                            }
                        }
                    });
                }
            }
        });
    }

    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    private void unlockScanner(){
        byte[] unlockCmd = {0x31,0x32,0x33,0x34,0x35,0x36};//"123456" unlock scanner
        ClientManager.getClient().write(mac, serviceUuid, writeUuid, unlockCmd, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code != REQUEST_SUCCESS) {
                    BluetoothLog.e("unlock write err %d" + code);
                }
            }
        });
    }

    private void lockScanner(){
        byte[] lockCmd = {0x36,0x35,0x34,0x33,0x32,0x31};//"654321" lock scanner
        ClientManager.getClient().write(mac, serviceUuid, writeUuid,lockCmd, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code != REQUEST_SUCCESS) {
                    BluetoothLog.e("lock write err %d" + code);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        ClientManager.getClient().disconnect(mDevice.getAddress());
        ClientManager.getClient().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        super.onDestroy();
    }

    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyApplication.toast(msg, 0);
                mTips.append(msg + "\n\n");
            }
        });
    }
}
