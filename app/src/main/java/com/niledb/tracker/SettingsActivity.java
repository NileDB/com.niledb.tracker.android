package com.niledb.tracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    static LocationManager locationManager = null;
    static PendingIntent launchIntent = null;

    static Context applicationContext = null;
    static Activity activity = null;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            }
            else {
                if (preference.getKey().equals("agent_id")) {
                    try {
                        int agentId = Integer.parseInt(stringValue);
                        if (agentId <= 0 || !stringValue.equals("" + agentId)) {
                            throw new NumberFormatException();
                        }
                    }
                    catch(NumberFormatException e) {
                        Toast.makeText(applicationContext, "Please, enter a positive integer without trailing 0's", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            enableOrDisableService();
        }
    }

    static void enableOrDisableService() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0 );
        }

        boolean serviceEnabled = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("service_enabled", false);

        /*
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
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        startService(new Intent(this, LauncherService.class));

        if (applicationContext == null) {
            applicationContext = getApplicationContext();
        }

        if (activity == null) {
            activity = this;
        }

        if (locationManager == null) {
            locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (launchIntent == null) {
            Intent intent = new Intent("NILEDB_TRACKER_SEND_LOCATION_ACTION");
            launchIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0);
        }
        enableOrDisableService();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ServicePreferenceFragment.class.getName().equals(fragmentName)
                || ParametersPreferenceFragment.class.getName().equals(fragmentName)
                || CommunicationsPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ServicePreferenceFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_service);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("agent_id"));
            bindPreferenceSummaryToValue(findPreference("username"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("service_enabled")) {
                enableOrDisableService();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CommunicationsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_communications);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("protocol"));
            bindPreferenceSummaryToValue(findPreference("graphql_endpoint"));
            bindPreferenceSummaryToValue(findPreference("mqtt_endpoint"));
            bindPreferenceSummaryToValue(findPreference("min_distance"));
            bindPreferenceSummaryToValue(findPreference("min_periodicity"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ParametersPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_parameters);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("entity_name"));
            bindPreferenceSummaryToValue(findPreference("location_attribute_name"));
            bindPreferenceSummaryToValue(findPreference("altitude_attribute_name"));
            bindPreferenceSummaryToValue(findPreference("accuracy_attribute_name"));
            bindPreferenceSummaryToValue(findPreference("agent_id_attribute_name"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
