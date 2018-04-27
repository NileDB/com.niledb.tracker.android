package com.niledb.tracker;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class LauncherService extends Service {


    static LocationManager locationManager = null;
    static PendingIntent launchIntent = null;

    public LauncherService() {


    }

    @Override
    public void onCreate() {
        super.onCreate();

        Context applicationContext = getApplicationContext();

        if (locationManager == null) {
            locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
            Intent intent = new Intent("NILEDB_TRACKER_SEND_LOCATION_ACTION");
            launchIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0);
        }

        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 150);

        boolean serviceEnabled = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("service_enabled", false);

        if (serviceEnabled) {
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                int minDistance = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("min_distance", "500"));
                int minPeriodicity = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("min_periodicity", "60000"));
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minPeriodicity, minDistance, launchIntent);


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, launchIntent);
            }
        }
        else {
            locationManager.removeUpdates(launchIntent);
        }



    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
