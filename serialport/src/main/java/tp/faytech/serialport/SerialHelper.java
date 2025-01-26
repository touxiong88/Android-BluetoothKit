package tp.faytech.serialport;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;
import tp.faytech.serialport.bean.ComBean;
import tp.faytech.serialport.stick.AbsStickPackageHelper;
import tp.faytech.serialport.stick.BaseStickPackageHelper;

public abstract class SerialHelper {
    private static final String TAG = "FtBLE";
    private SerialPort mSerialPort;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String sPort = "/dev/ttyS3";
    private int iBaudRate = 115200;
    private int stopBits = 1;
    private int dataBits = 8;
    private int parity = 0;
    private int flowCon = 0;
    private int flags = 0;
    private boolean _isOpen = false;
    private byte[] _bLoopData = {48};
    private int iDelay = 500;
    private boolean loop = false;


    public SerialHelper(String sPort, int iBaudRate) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
    }

    public void open()
            throws SecurityException, IOException, InvalidParameterException {
        this.mSerialPort = new SerialPort(new File(this.sPort), this.iBaudRate, this.stopBits, this.dataBits, this.parity, this.flowCon, this.flags);
        this.mInputStream = this.mSerialPort.getInputStream();
        loop = true;
        this.mReadThread = new ReadThread();
        this.mReadThread.start();
        this._isOpen = true;
    }

    public void close() {
        if (this.mReadThread != null) {
            this.mReadThread.interrupt();
        }
        if (this.mSerialPort != null) {
            this.mSerialPort.close();
            this.mSerialPort = null;
        }
        this._isOpen = false;
    }

    private class ReadThread
            extends Thread {
        private ReadThread() {
        }

        public void run() {
            super.run();
            Log.d(TAG, "ReadThread 0");
            while (loop && !isInterrupted()) {
                try {
//                    Log.d(TAG, "ReadThread 1");
                    if (SerialHelper.this.mInputStream == null) {
                        return;
                    }
//                    Log.d(TAG, "ReadThread 2");
                    byte[] buffer = getStickPackageHelper().execute(SerialHelper.this.mInputStream);
                    if (buffer != null && buffer.length > 0) {
                        SerialHelper.this.onDataReceived(buffer);
                    }
                    try {
                        Thread.sleep(2000L);
                    }catch (Exception e){
                    }
                } catch (Throwable e) {
                    if (e.getMessage() != null) {
//                         Log.d(TAG, "ReadThread 3");
                        Log.e(TAG, e.getMessage());
                    }
                    return;
                }
            }
        }
    }

    public boolean setBaudRate(int iBaud) {
        if (this._isOpen) {
            return false;
        }
        this.iBaudRate = iBaud;
        return true;
    }

    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    public boolean setStopBits(int stopBits) {
        if (this._isOpen) {
            return false;
        }
        this.stopBits = stopBits;
        return true;
    }

    public boolean setDataBits(int dataBits) {
        if (this._isOpen) {
            return false;
        }
        this.dataBits = dataBits;
        return true;
    }

    public boolean setFlowCon(int flowCon) {
        if (this._isOpen) {
            return false;
        }
        this.flowCon = flowCon;
        return true;
    }

    public boolean setPort(String sPort) {
        if (this._isOpen) {
            return false;
        }
        this.sPort = sPort;
        return true;
    }


    public byte[] getbLoopData() {
        return this._bLoopData;
    }


    protected abstract void onDataReceived(byte[] data);

    private AbsStickPackageHelper mStickPackageHelper = new BaseStickPackageHelper();

    public AbsStickPackageHelper getStickPackageHelper() {
        return mStickPackageHelper;
    }


}
