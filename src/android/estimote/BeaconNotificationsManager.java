package com.red_folder.phonegap.plugin.backgroundservice.sample.estimote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.red_folder.phonegap.plugin.backgroundservice.sample.MyService;
import com.red_folder.phonegap.plugin.backgroundservice.sample.estimote.APIClient;

import com.topl.Topl.MainActivity;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class BeaconNotificationsManager {

    private static final String TAG = "BeaconNotifications";

    private BeaconManager beaconManager;

    private List<Region> regionsToMonitor = new ArrayList<>();
    private HashMap<String, String> enterMessages = new HashMap<>();
    private HashMap<String, String> exitMessages = new HashMap<>();
    private String accessToken = "";
    private String endpoint = "";

    private Context context;

    private int notificationID = 0;

    public BeaconNotificationsManager(final Context context) {

        this.context = context;
        beaconManager = new BeaconManager(context);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                MyService myservice = (MyService) context;
                myservice.onEnter();
                for(Beacon beacon:list){
                    int[] oldMajorMinor = myservice.getMajorMinor();

                    if(oldMajorMinor[0]==beacon.getMajor() && oldMajorMinor[1]==beacon.getMinor() && myservice.getEnabled()){
                        Log.d(TAG, "onEnteredRegion: " + beacon.getMajor() + " " + beacon.getMinor());
                    }
                    // if the timer is not active, post to API server
                    else {
                        myservice.setMajorMinor(beacon.getMajor(), beacon.getMinor());
                        Log.d(TAG, "onEnteredRegion: " + beacon.getMajor() + " " + beacon.getMinor());

                        String message = "onEnteredRegion: " + beacon.getMajor()+ " " + beacon.getMinor();
                        showNotification(message);
                        updateServer(beacon.getMajor() + "-" + beacon.getMinor());
                    }
                    myservice.setEnabled(false);
                    myservice.stopTimerTask();
                    myservice.restartTimer();
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                // start a 15 sec timer when exit region is triggered
                MyService myservice = (MyService) context;
                myservice.onExit();
                showNotification("onExited");
                updateServer(null);

                Log.d(TAG, "onExitedRegion: " + region.getIdentifier());
            }
        });
    }

    private void updateServer(String currentRegion) {


        ArrayList<String> beacons = new ArrayList<>();

        if(currentRegion != null) {
            beacons.add(currentRegion);
        }

        JSONObject jsonParams = new JSONObject();

        try {

            jsonParams.put("beaconIds", new JSONArray(beacons));
            Log.d(TAG, "BEACONS: " + jsonParams.toString());
            StringEntity entity = new StringEntity(jsonParams.toString());

            APIClient.setBaseUrl(endpoint);

            APIClient.put(context, "/users/me/paymentBeacons", entity, accessToken, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                    // Do something with the response
                    Log.d(TAG, "Request success: " + res.toString());
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray res) {
                    // Do something with the response
                    Log.d(TAG, "Request success: " + res.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, "Failed request: " + errorResponse.toString());
                }

            });

        } catch (Exception e) {}

    }

    public void setToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void addNotification(BeaconID beaconID, String enterMessage, String exitMessage) {
//        Region region = beaconID.toBeaconRegion();
        Region region = new Region(beaconID.getProximityUUID().toString(), beaconID.getProximityUUID(), beaconID.getMajor(), beaconID.getMinor());
        enterMessages.put(region.getIdentifier(), enterMessage);
        exitMessages.put(region.getIdentifier(), exitMessage);
        regionsToMonitor.add(region);
    }

    public void startMonitoring() {
        beaconManager.setBackgroundScanPeriod(1000,1000);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                for (Region region : regionsToMonitor) {
                    beaconManager.startMonitoring(region);
                }
            }
        });
    }

    private void showNotification(String message) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Beacon Notifications")
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID++, builder.build());
    }
}
