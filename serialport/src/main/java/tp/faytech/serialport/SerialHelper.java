package tp.faytech.serialport;

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
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private String sPort = "/dev/ttyS1";
    private int iBaudRate = 9600;
    private int stopBits = 1;
    private int dataBits = 8;
    private int parity = 0;
    private int flowCon = 0;
    private int flags = 0;
    private boolean _isOpen = false;
    private byte[] _bLoopData = {48};
    private int iDelay = 500;


    public SerialHelper(String sPort, int iBaudRate) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
    }

    public void open()
            throws SecurityException, IOException, InvalidParameterException {
        this.mSerialPort = new SerialPort(new File(this.sPort), this.iBaudRate, this.stopBits, this.dataBits, this.parity, this.flowCon, this.flags);
        this.mOutputStream = this.mSerialPort.getOutputStream();
        this.mInputStream = this.mSerialPort.getInputStream();
        this.mReadThread = new ReadThread();
        this.mReadThread.start();
        this.mSendThread = new SendThread();
        this.mSendThread.setSuspendFlag();
        this.mSendThread.start();
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

    public void send(byte[] bOutArray) {
        try {
            this.mOutputStream.write(bOutArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread
            extends Thread {
        private ReadThread() {
        }

        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (SerialHelper.this.mInputStream == null) {
                        return;
                    }

                    byte[] buffer = getStickPackageHelper().execute(SerialHelper.this.mInputStream);
                    if (buffer != null && buffer.length > 0) {
                        ComBean ComRecData = new ComBean(SerialHelper.this.sPort, buffer, buffer.length);
                        SerialHelper.this.onDataReceived(ComRecData);
                    }

                } catch (Throwable e) {
                     if (e.getMessage() != null) {
                        Log.e("error", e.getMessage());
                    }
                    return;
                }
            }
        }
    }

    private class SendThread
            extends Thread {
        public boolean suspendFlag = true;

        private SendThread() {
        }

        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (this.suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                SerialHelper.this.send(SerialHelper.this.getbLoopData());
                try {
                    Thread.sleep(SerialHelper.this.iDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setSuspendFlag() {
            this.suspendFlag = true;
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




    protected abstract void onDataReceived(ComBean paramComBean);

    private AbsStickPackageHelper mStickPackageHelper = new BaseStickPackageHelper();

    public AbsStickPackageHelper getStickPackageHelper() {
        return mStickPackageHelper;
    }


}
