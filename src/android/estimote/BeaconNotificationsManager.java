package com.red_folder.phonegap.plugin.backgroundservice.sample.estimote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.red_folder.phonegap.plugin.backgroundservice.sample.MyService;
import com.red_folder.phonegap.plugin.backgroundservice.sample.estimote.APIClient;

import com.topl.Topl.MainActivity;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONArray;
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
                myservice.setDoWorkStatus(false);
                myservice.setEnabled(true);
                myservice.setMilliseconds(15000);
                myservice.setupTimerTask();
                Log.d(TAG, "onExitedRegion: " + region.getIdentifier());
            }
        });
    }

    private void updateServer(String currentRegion) {

        try {

            RequestParams params = new RequestParams();
            params.put("beaconIds", [currentRegion]);

            APIClient.setBaseUrl(endpoint);

            APIClient.put("/users/me/paymentBeacons", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                    // Pull out the first event on the public timeline
                    JSONObject firstEvent = timeline.get(0);
                    String tweetText = firstEvent.getString("text");

                    // Do something with the response
                    System.out.println(tweetText);
                }
            });



        } catch(IOException ignored) {}

    }


    public static class InputStreamToStringExample {

        public static void main(String[] args) throws IOException {

            // intilize an InputStream
            InputStream is =
                    new ByteArrayInputStream("file content..blah blah".getBytes());

            String result = getStringFromInputStream(is);

            System.out.println(result);
            System.out.println("Done");

        }

        // convert InputStream to String
        private static String getStringFromInputStream(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }

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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
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
