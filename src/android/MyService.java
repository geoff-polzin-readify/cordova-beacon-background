package com.red_folder.phonegap.plugin.backgroundservice.sample;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	private String mHelloTo = "World";

	@Override
	protected JSONObject doWork() {
		JSONObject result = new JSONObject();
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String now = df.format(new Date(System.currentTimeMillis()));

			if(getDoWorkStatus() == false){
				result.put("Message", "skip");
				setDoWorkStatus(true);
				Log.d(TAG, "skip");
			}else{
				String msg = "OnExit " + getConfig().getString("HelloTo") + " - its currently " + now;
				result.put("Message", msg);
				Log.d(TAG, msg);
				// cancel the timer after work is finished
				setEnabled(false);
				stopTimerTask();
				restartTimer();
			}

		} catch (JSONException e) {
		}
		
		return result;	
	}

	@Override
	protected JSONObject getConfig() {
		JSONObject result = new JSONObject();
		
		try {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			String msg = sharedPrefs.getString(this.getClass().getName() + ".HelloTo", "blah");
			result.put("HelloTo", msg);
		} catch (JSONException e) {
		}
		
		return result;
	}

	@Override
	protected void setConfig(JSONObject config) {
		try {
			if (config.has("HelloTo")){
//				this.mHelloTo = config.getString("HelloTo");
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString(this.getClass().getName() + ".HelloTo", config.getString("HelloTo"));
				editor.commit(); // Very important
			}
		} catch (JSONException e) {
		}
		
	}     

	@Override
	protected JSONObject initialiseLatestResult() {
		// TODO Auto-generated method stub
		BeaconNotificationsManager beaconNotificationsManager = new BeaconNotificationsManager(this);
		beaconNotificationsManager.addNotification(
				new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 5865, 32046),
//				new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", null, null),
				"Hello, world.",
				"Goodbye, world.");
		beaconNotificationsManager.startMonitoring();
		Toast.makeText(this, "service start", Toast.LENGTH_SHORT).show();
		return null;
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
		int[] majorMinor = {sharedPrefs.getInt(this.getClass().getName() + ".Major", -1),
				sharedPrefs.getInt(this.getClass().getName() + ".Minor", -1)};
		return majorMinor;
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

	public void onEnter(){
		Toast.makeText(this, "region entered", Toast.LENGTH_SHORT).show();
	}

	public void onExit(){
		Toast.makeText(this, "region exited", Toast.LENGTH_SHORT).show();
	}
}
