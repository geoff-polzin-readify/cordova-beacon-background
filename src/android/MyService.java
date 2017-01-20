package com.red_folder.phonegap.plugin.backgroundservice.sample;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;
import com.red_folder.phonegap.plugin.backgroundservice.sample.estimote.BeaconID;
import com.red_folder.phonegap.plugin.backgroundservice.sample.estimote.BeaconNotificationsManager;

public class MyService extends BackgroundService {

	private final static String TAG = MyService.class.getSimpleName();

	@Override
	protected JSONObject doWork() {
		JSONObject result = new JSONObject();

		return result;
	}

	@Override
	protected JSONObject getConfig() {
		JSONObject configResult = new JSONObject();

		try {

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

			String accessToken = sharedPrefs.getString(this.getClass().getName() + ".accessToken", "");
			String APIendpoint = sharedPrefs.getString(this.getClass().getName() + ".APIendpoint", "https://test-api.topl.me/api");
			Set<String> paymentRegions = sharedPrefs.getStringSet(this.getClass().getName() + ".paymentRegions", null);
			Set<String> pushRegions = sharedPrefs.getStringSet(this.getClass().getName() + ".pushRegions", null);


			configResult.put("accessToken", accessToken);
			configResult.put("APIendpoint", APIendpoint);
			configResult.put("paymentRegions", new JSONArray(paymentRegions));
			configResult.put("pushRegions", new JSONArray(pushRegions));

		} catch (JSONException ignored) {}

		return configResult;
	}

	@Override
	protected void setConfig(JSONObject config) {

		Iterator<String> keys = config.keys();

		try {

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

			SharedPreferences.Editor editor = sharedPrefs.edit();

			while(keys.hasNext()) {

				String key = keys.next();

				if(config.get(key) instanceof JSONArray) {

					String[] strings = config.getJSONArray(key).join(",").replace("\"", "").split(",");
					Set<String> stringSet = new HashSet<>(Arrays.asList(strings));

					editor.putStringSet(this.getClass().getName() + '.' + key, stringSet);
				} else if (config.get(key) instanceof String){
					editor.putString(this.getClass().getName() + '.' + key, config.getString(key));
				}
			}

			editor.putBoolean(this.getClass().getName() + ".configSet", true);

			editor.apply(); // Very important

		} catch (JSONException e) {
			Log.e(TAG, "BEACON CONFIG ERROR: " + e.getMessage());
		}

	}

	@Override
	protected JSONObject initialiseLatestResult() {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(sharedPrefs.getBoolean(this.getClass().getName()+".configSet", false)) {
			startMonitoring();
		}
		return null;
	}

	public void startMonitoring() {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		String accessToken = sharedPrefs.getString(this.getClass().getName() + ".accessToken", "");
		String APIendpoint = sharedPrefs.getString(this.getClass().getName() + ".APIendpoint", "https://api.topl.me/api");
		Set<String> paymentRegions = sharedPrefs.getStringSet(this.getClass().getName() + ".paymentRegions", new HashSet<>());
		Set<String> pushRegions = sharedPrefs.getStringSet(this.getClass().getName() + ".pushRegions", new HashSet<>());

		BeaconNotificationsManager beaconNotificationsManager = new BeaconNotificationsManager(this);
		beaconNotificationsManager.setToken(accessToken);
		beaconNotificationsManager.setEndpoint(APIendpoint);

		ArrayList<BeaconID> paymentRegionBeaconIDs = new ArrayList<>();
		ArrayList<BeaconID> pushRegionBeaconIDs = new ArrayList<>();

		for(String region : paymentRegions) {
			paymentRegionBeaconIDs.add(new BeaconID(region, null, null));
		}

		for(String region : pushRegions) {
			pushRegionBeaconIDs.add(new BeaconID(region, null, null));
		}

		beaconNotificationsManager.setPaymentRegionsToMonitor(paymentRegionBeaconIDs);
		beaconNotificationsManager.setPushRegionsToMonitor(pushRegionBeaconIDs);

		beaconNotificationsManager.startMonitoring();
	}

	@Override
	protected void onTimerEnabled() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTimerDisabled() {
		// TODO Auto-generated method stub

	}

	public int[] getMajorMinor() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		return new int[]{
				sharedPrefs.getInt(this.getClass().getName() + ".Major", -1),
				sharedPrefs.getInt(this.getClass().getName() + ".Minor", -1)
		};
	}

	public void setMajorMinor(int major, int minor) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(this.getClass().getName() + ".Major", major);
		editor.putInt(this.getClass().getName() + ".Minor", minor);
		editor.commit(); // Very important
	}

	public Boolean getDoWorkStatus() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		return sharedPrefs.getBoolean(this.getClass().getName() + ".DoWork", false);
	}

	public void setDoWorkStatus(boolean status) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(this.getClass().getName() + ".DoWork", status);
		editor.commit(); // Very important
	}

	public void onEnter(){}

	public void onExit(){}
}
