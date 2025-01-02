// IBleResponse.aidl
package com.faytech.bluetooth.library;

// Declare any non-default types here with import statements

interface IResponse {
    void onResponse(int code, inout Bundle data);
}
