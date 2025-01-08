![image-20250108085315875](C:\Users\hogan.sun\AppData\Roaming\Typora\typora-user-images\image-20250108085315875.png)



![image-20250108090216752](C:\Users\hogan.sun\AppData\Roaming\Typora\typora-user-images\image-20250108090216752.png)



The scanner lock will remain unlocked for 5 seconds and then automatically lock

Code for key parts

app\src\main\java\com\faytech\bluetooth\DeviceDetailActivity.java

```
private Button unlockBtn;
unlockBtn = (Button) findViewById(R.id.btn_unlock);
unlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothLog.d("Scanner unlock");
                unlockScanner();
            }
        });
        
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
    
    
if ((characteristic.getProperty() & (BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT + BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                                writeUuid = characteristic.getUuid();
                                BluetoothLog.d("Write Characteristic UUID: " + writeUuid);
                            }
```



