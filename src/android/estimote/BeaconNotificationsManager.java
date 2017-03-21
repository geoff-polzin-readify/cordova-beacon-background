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
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class BeaconNotificationsManager {

    private static final String TAG = "BeaconNotifications";

    private BeaconManager beaconManager;

    private List<Region> paymentRegionsToMonitor = new ArrayList<>();
    private List<Region> pushRegionsToMonitor = new ArrayList<>();
    private Map<String, String> enterMessages = new HashMap<>();
    private Map<String, String> exitMessages = new HashMap<>();
    private String accessToken = "";
    private String endpoint = "";
    private Map<Region, List<Beacon>> paymentBeacons = new HashMap<>();
    private Map<Region, List<Beacon>> pushBeacons = new HashMap<>();


    private Context context;

    private int notificationID = 0;

    public BeaconNotificationsManager(final Context context) {

        this.context = context;
        beaconManager = new BeaconManager(context);

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                MyService myservice = (MyService) context;

                Log.d(TAG, "onEnteredRegion: " + region.getIdentifier());

                for(Beacon b : list) {
                    Log.d(TAG, "onEnteredRegion: Beacon " + b.getMajor() + " " + b.getMinor());
                }

                if (paymentRegionsToMonitor.contains(region)) {
                    paymentBeacons.put(region, list);
                } else {
                    pushBeacons.put(region, list);
                }

                myservice.onEnter();
                updateServer(region);
            }

            @Override
            public void onExitedRegion(Region region) {
                MyService myservice = (MyService) context;
                myservice.onExit();

                if (paymentRegionsToMonitor.contains(region)) {
                    paymentBeacons.put(region, new ArrayList<Beacon>());
                } else {
                    pushBeacons.put(region, new ArrayList<Beacon>());
                }

                updateServer(region);

                Log.d(TAG, "onExitedRegion: " + region.getIdentifier());
            }
        });
    }

    private void updateServer(Region region) {

        List<String> beaconMajorMinors =  new ArrayList<>();


        if(paymentRegionsToMonitor.contains(region)) {

            for(List<Beacon> beaconList : paymentBeacons.values()) {
                for(Beacon b : beaconList) {
                    beaconMajorMinors.add(b.getMajor() + "-" + b.getMinor());
                }
            }


            JSONObject jsonParams = new JSONObject();

            try {

                jsonParams.put("beaconIds", new JSONArray(beaconMajorMinors));
                Log.d(TAG, "BEACONS: " + jsonParams.toString());
                StringEntity entity = new StringEntity(jsonParams.toString());

                APIClient.setBaseUrl(endpoint);

                APIClient.put(context, "/users/me/paymentBeacons", entity, accessToken, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                        // Do something with the response
                        Log.d(TAG, "Request success: " + res);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray res) {
                        // Do something with the response
                        Log.d(TAG, "Request success: " + res.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d(TAG, "Failed request: " + errorResponse);
                    }

                });

            } catch (Exception e) {}

        } else {

            /* Get the beacons for this region, and pick one randomly */
            List<Beacon> beacons = pushBeacons.get(region);

            if(beacons.size() > 0) {

                Random rnd = new Random();
                int i = rnd.nextInt(beacons.size());

                Beacon b = beacons.get(i);
                beaconMajorMinors.add(b.getMajor() + "-" + b.getMinor());

                JSONObject jsonParams = new JSONObject();

                try {

                    jsonParams.put("beaconId", new JSONArray(beaconMajorMinors));
                    Log.d(TAG, "BEACON: " + jsonParams.toString());
                    StringEntity entity = new StringEntity(jsonParams.toString());

                    APIClient.setBaseUrl(endpoint);

                    APIClient.put(context, "/users/me/pushBeacons", entity, accessToken, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                            // Do something with the response
                            Log.d(TAG, "Request success: " + res);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray res) {
                            // Do something with the response
                            Log.d(TAG, "Request success: " + res.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.d(TAG, "Failed request: " + errorResponse);
                        }

                    });

                } catch (Exception e) {}
            }
        }

    }

    public void setToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    public void setPaymentRegionsToMonitor(ArrayList<BeaconID> beaconIDs) {
        paymentRegionsToMonitor = new ArrayList<>();

        for(BeaconID beaconID : beaconIDs) {
            Region region = new Region(beaconID.getProximityUUID().toString(), beaconID.getProximityUUID(), beaconID.getMajor(), beaconID.getMinor());
            paymentRegionsToMonitor.add(region);
            Log.d(TAG, "MONITORING REGION: " + region.toString());
        }
    }

    public void setPushRegionsToMonitor(ArrayList<BeaconID> beaconIDs) {
        pushRegionsToMonitor = new ArrayList<>();

        for(BeaconID beaconID : beaconIDs) {
            Region region = new Region(beaconID.getProximityUUID().toString(), beaconID.getProximityUUID(), beaconID.getMajor(), beaconID.getMinor());
            pushRegionsToMonitor.add(region);
            Log.d(TAG, "MONITORING REGION: " + region.toString());
        }
    }

    public void startMonitoring() {
        beaconManager.setBackgroundScanPeriod(1000,1000);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Log.d(TAG, "SERVICE READY, MONITORING: " + paymentRegionsToMonitor.toString());
                for (Region region : paymentRegionsToMonitor) {
                    beaconManager.startMonitoring(region);
                }

                for (Region region : pushRegionsToMonitor) {
                    beaconManager.startMonitoring(region);
                }
            }
        });
    }

}
