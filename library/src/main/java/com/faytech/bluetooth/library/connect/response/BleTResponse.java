package com.faytech.bluetooth.library.connect.response;

public interface BleTResponse<T> {
    void onResponse(int code, T data);
}
