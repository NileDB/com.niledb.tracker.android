package com.niledb.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

public class LocationChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                double altitude = location.getAltitude();
                float accuracy = location.getAccuracy();

                Log.i("NileDB", latitude + ", " + longitude + ", " + altitude + ", " + accuracy);

                boolean serviceEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("service_enabled", false);
                int agentId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("agent_id", "1"));
                boolean authenticationRequired = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("authentication_required", false);
                String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", null);
                String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", null);
                String adminPassword = PreferenceManager.getDefaultSharedPreferences(context).getString("admin_password", null);

                int protocol = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("protocol", "0"));
                String graphqlEndpoint = PreferenceManager.getDefaultSharedPreferences(context).getString("graphql_endpoint", "https://niledb.com/graphql");
                String mqttEndpoint = PreferenceManager.getDefaultSharedPreferences(context).getString("mqtt_endpoint", "https://niledb.com:1883");
                int minDistance = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("min_distance", "500"));
                int minPeriodicity = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("min_periodicity", "60000"));

                String entityName = PreferenceManager.getDefaultSharedPreferences(context).getString("entity_name", "CarrierAgentLocation");
                String agentIdParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("agent_id_attribute_name", "carrierAgent");
                String locationParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("location_attribute_name", "location");
                String altitudeParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("altitude_attribute_name", null);
                String accuracyParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("accuracy_attribute_name", null);
                boolean debugBeep = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug_beep", false);
                boolean debugVibrate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug_vibrate", false);

                Log.i("NileDB", new Date() + "," + serviceEnabled + ", " + agentId + ", " + authenticationRequired + ", " + username + ", " + password + ", " + adminPassword + ", " + protocol + ", " + graphqlEndpoint + ", " + mqttEndpoint + ", " + minDistance + ", " + minPeriodicity + ", " + entityName + ", " + agentIdParameterName + ", " + locationParameterName + ", " + altitudeParameterName + ", " + accuracyParameterName);

                if (debugBeep) {
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 1000);
                }

                if (debugVibrate) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(500);
                        }
                    }
                }
            }
        }
    }
}
