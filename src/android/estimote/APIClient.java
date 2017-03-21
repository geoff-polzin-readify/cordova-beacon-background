package com.red_folder.phonegap.plugin.backgroundservice.sample.estimote;

/**
 * Created by Topl on 9/29/16.
 */

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.entity.StringEntity;

public class APIClient {
    private static String BASE_URL;
    private static final String TAG = "BeaconNotifications";

    private static AsyncHttpClient client = new AsyncHttpClient();


    public static void setBaseUrl(String BASE_URL) {
        APIClient.BASE_URL = BASE_URL;
    }

    private static void before(String accessToken) {
        client.removeAllHeaders();
        client.addHeader("Authorization", accessToken);
        Log.d(TAG, "AccessToken: " + accessToken);
    }

    public static void get(Context context, String endpoint, StringEntity entity, String accessToken, AsyncHttpResponseHandler responseHandler) {
        before(accessToken);
        client.get(context, getAbsoluteUrl(endpoint), entity, "application/json", responseHandler);
    }

    public static void post(Context context, String endpoint, StringEntity entity, String accessToken, AsyncHttpResponseHandler responseHandler) {
        before(accessToken);
        client.post(context, getAbsoluteUrl(endpoint), entity, "application/json", responseHandler);
    }

    public static void put(Context context, String endpoint, StringEntity entity, String accessToken, AsyncHttpResponseHandler responseHandler) {
        before(accessToken);
        client.put(context, getAbsoluteUrl(endpoint), entity, "application/json", responseHandler);
    }

    private static String getAbsoluteUrl(String endpoint) {
        return BASE_URL + endpoint;
    }
}
