package com.red_folder.phonegap.plugin.backgroundservice.sample.estimote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.red_folder.phonegap.plugin.backgroundservice.sample.MyService;
import com.startapplabs.ionTheme1.MainActivity;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BeaconNotificationsManager {

    private static final String TAG = "BeaconNotifications";

    private BeaconManager beaconManager;

    private List<Region> regionsToMonitor = new ArrayList<>();
    private HashMap<String, String> enterMessages = new HashMap<>();
    private HashMap<String, String> exitMessages = new HashMap<>();

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
                    // TODO: if entered the same store and the timer is active, don't post to API server
                    if(oldMajorMinor[0]==beacon.getMajor() && oldMajorMinor[1]==beacon.getMinor() && myservice.getEnabled()){
                        Log.d(TAG, "onEnteredRegion: " + beacon.getMajor() + " " + beacon.getMinor());
                    }
                    // if the timer is not active, post to API server
                    else {
                        myservice.setMajorMinor(beacon.getMajor(), beacon.getMinor());
                        Log.d(TAG, "onEnteredRegion: " + beacon.getMajor() + " " + beacon.getMinor());

//                    String message = enterMessages.get(region.getIdentifier());
                        String message = "onEnteredRegion: " + beacon.getMajor()+ " " + beacon.getMinor();
                        if (message != null) {
                            showNotification(message);
                        }
                    }
                    myservice.setEnabled(false);
                    myservice.stopTimerTask();
                    myservice.restartTimer();
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                // start a 30 sec timer when exit region is triggered
                MyService myservice = (MyService) context;
                myservice.onExit();
                myservice.setDoWorkStatus(false);
                myservice.setEnabled(true);
                myservice.setMilliseconds(15000);
                myservice.setupTimerTask();
                Log.d(TAG, "onExitedRegion: " + region.getIdentifier());
//                String message = exitMessages.get(region.getIdentifier());
//                if (message != null) {
//                    showNotification(message);
//                }
            }
        });
    }

    public void addNotification(BeaconID beaconID, String enterMessage, String exitMessage) {
//        Region region = beaconID.toBeaconRegion();
        Region region = new Region("My_Beacons", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),null,null);
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
