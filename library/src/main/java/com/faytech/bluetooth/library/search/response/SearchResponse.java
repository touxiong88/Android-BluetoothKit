package com.faytech.bluetooth.library.search.response;

import com.faytech.bluetooth.library.search.SearchResult;

public interface SearchResponse {

    void onSearchStarted();

    void onDeviceFounded(SearchResult device);

    void onSearchStopped();

    void onSearchCanceled();
}
