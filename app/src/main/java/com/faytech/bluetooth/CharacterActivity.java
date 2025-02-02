package com.faytech.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.faytech.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.faytech.bluetooth.library.connect.response.BleMtuResponse;
import com.faytech.bluetooth.library.connect.response.BleNotifyResponse;
import com.faytech.bluetooth.library.connect.response.BleReadResponse;
import com.faytech.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.faytech.bluetooth.library.connect.response.BleWriteResponse;
import com.faytech.bluetooth.library.utils.BluetoothLog;
import com.faytech.bluetooth.library.utils.ByteUtils;

import static com.faytech.bluetooth.library.Constants.*;

import java.util.UUID;


public class CharacterActivity extends Activity implements View.OnClickListener {

    private String mMac;
    private UUID mService;
    private UUID mCharacter;

    private TextView mTvTitle;

    private Button mBtnRead;

    private Button mBtnWrite;
    private EditText mEtInput;

    private Button mBtnNotify;
    private Button mBtnUnnotify;
    private EditText mEtInputMtu;
    private Button mBtnRequestMtu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_activity);

        Intent intent = getIntent();
        mMac = intent.getStringExtra("mac");
        mService = (UUID) intent.getSerializableExtra("service");
        mCharacter = (UUID) intent.getSerializableExtra("character");

        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(String.format("%s", mMac));

        mBtnRead = (Button) findViewById(R.id.read);

        mBtnWrite = (Button) findViewById(R.id.write);
        mEtInput = (EditText) findViewById(R.id.input);

        mBtnNotify = (Button) findViewById(R.id.notify);
        mBtnUnnotify = (Button) findViewById(R.id.unnotify);

        mEtInputMtu = (EditText) findViewById(R.id.et_input_mtu);
        mBtnRequestMtu = (Button) findViewById(R.id.btn_request_mtu);

        mBtnRead.setOnClickListener(this);
        mBtnWrite.setOnClickListener(this);

        mBtnNotify.setOnClickListener(this);
        mBtnNotify.setEnabled(true);

        mBtnUnnotify.setOnClickListener(this);
        mBtnUnnotify.setEnabled(false);

        mBtnRequestMtu.setOnClickListener(this);
    }

    private final BleReadResponse mReadRsp = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
            if (code == REQUEST_SUCCESS) {
                mBtnRead.setText(String.format("read: %s", ByteUtils.byteToString(data)));
                MyApplication.toast("success",0);
            } else {
                MyApplication.toast("failed",0);
                mBtnRead.setText("read");
            }
        }
    };

    private final BleWriteResponse mWriteRsp = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                MyApplication.toast("success",0);
            } else {
                MyApplication.toast("failed",0);
            }
        }
    };

    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (service.equals(mService) && character.equals(mCharacter)) {
                mBtnNotify.setText(String.format("%s", ByteUtils.byteToString(value)));
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                mBtnNotify.setEnabled(false);
                mBtnUnnotify.setEnabled(true);
                MyApplication.toast("success",0);
            } else {
                MyApplication.toast("failed",0);
            }
        }
    };

    private final BleUnnotifyResponse mUnnotifyRsp = new BleUnnotifyResponse() {
        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                MyApplication.toast("success",0);
                mBtnNotify.setEnabled(true);
                mBtnUnnotify.setEnabled(false);
            } else {
                MyApplication.toast("failed",0);
            }
        }
    };

    private final BleMtuResponse mMtuResponse = new BleMtuResponse() {
        @Override
        public void onResponse(int code, Integer data) {
            if (code == REQUEST_SUCCESS) {
                MyApplication.toast("request mtu success,mtu = " + data,0);
            } else {
                MyApplication.toast("request mtu failed",0);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.read) {
            ClientManager.getClient().read(mMac, mService, mCharacter, mReadRsp);
        } else if (id == R.id.write) {
            ClientManager.getClient().write(mMac, mService, mCharacter,
                    ByteUtils.stringToBytes(mEtInput.getText().toString()), mWriteRsp);
        } else if (id == R.id.notify) {
            ClientManager.getClient().notify(mMac, mService, mCharacter, mNotifyRsp);
        } else if (id == R.id.unnotify) {
            ClientManager.getClient().unnotify(mMac, mService, mCharacter, mUnnotifyRsp);
        } else if (id == R.id.btn_request_mtu) {
            String mtuStr = mEtInputMtu.getText().toString();
            if (TextUtils.isEmpty(mtuStr)) {
                MyApplication.toast("MTU cannot be empty",0);
                return;
            }
            int mtu = Integer.parseInt(mtuStr);
            if (mtu < GATT_DEF_BLE_MTU_SIZE || mtu > GATT_MAX_MTU_SIZE) {
                MyApplication.toast("MTU not in range",0);
                return;
            }
            ClientManager.getClient().requestMtu(mMac, mtu, mMtuResponse);
        }
    }

    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("CharacterActivity.onConnectStatusChanged status = %d", status));

            if (status == STATUS_DISCONNECTED) {
                MyApplication.toast("disconnected",0);
                mBtnRead.setEnabled(false);
                mBtnWrite.setEnabled(false);
                mBtnNotify.setEnabled(false);
                mBtnUnnotify.setEnabled(false);

                mTvTitle.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        finish();
                    }
                }, 300);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ClientManager.getClient().registerConnectStatusListener(mMac, mConnectStatusListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().unregisterConnectStatusListener(mMac, mConnectStatusListener);
    }
}
