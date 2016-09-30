package com.red_folder.phonegap.plugin.backgroundservice.sample.estimote;

/**
 * Created by Topl on 9/29/16.
 */

import com.loopj.android.http.*;

public class APIClient {
    private static String BASE_URL;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void setBaseUrl(String BASE_URL) {
        APIClient.BASE_URL = BASE_URL;
    }

    public static void get(String endpoint, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(endpoint), params, responseHandler);
    }

    public static void post(String endpoint, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(endpoint), params, responseHandler);
    }

    public static void put(String endpoint, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.put(getAbsoluteUrl(endpoint), params, responseHandler);
    }

    private static String getAbsoluteUrl(String endpoint) {
        return BASE_URL + endpoint;
    }
}
