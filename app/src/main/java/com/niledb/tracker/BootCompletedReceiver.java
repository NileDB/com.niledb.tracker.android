package com.niledb.tracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

public class BootCompletedReceiver extends BroadcastReceiver {

    static LocationManager locationManager = null;
    static PendingIntent launchIntent = null;
    static ToneGenerator toneGenerator = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            launchIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.niledb.tracker.SEND_LOCATION_ACTION"), 0);
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        }

        boolean debugBeep = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug_beep", false);

        if (debugBeep) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150);
            }
            else {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 150);
            }
        }

        boolean serviceEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("service_enabled", false);

        if (serviceEnabled) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                int minDistance = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("min_distance", "500"));
                int minPeriodicity = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("min_periodicity", "60000"));
                locationManager.removeUpdates(launchIntent);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minPeriodicity, minDistance, launchIntent);
            }
        }
        else {
            locationManager.removeUpdates(launchIntent);
        }
    }
}
