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
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class LocationChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            if (location != null) {
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
                final double altitude = location.getAltitude();
                final float accuracy = location.getAccuracy();

                final boolean serviceEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("service_enabled", false);
                final int agentId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("agent_id", "1"));
                final boolean authenticationRequired = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("authentication_required", false);
                final String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", null);
                final String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", null);
                final String adminPassword = PreferenceManager.getDefaultSharedPreferences(context).getString("admin_password", null);

                final int protocol = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("protocol", "0"));
                final String graphqlEndpoint = PreferenceManager.getDefaultSharedPreferences(context).getString("graphql_endpoint", "https://niledb.com/graphql");
                final String mqttEndpoint = PreferenceManager.getDefaultSharedPreferences(context).getString("mqtt_endpoint", "https://niledb.com:1883");

                final String entityName = PreferenceManager.getDefaultSharedPreferences(context).getString("entity_name", "CarrierAgentLocation");
                final String agentIdParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("agent_id_attribute_name", "carrierAgent");
                final String locationParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("location_attribute_name", "location");
                final String altitudeParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("altitude_attribute_name", null);
                final String accuracyParameterName = PreferenceManager.getDefaultSharedPreferences(context).getString("accuracy_attribute_name", null);
                final boolean debugBeep = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug_beep", false);
                final boolean debugVibrate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug_vibrate", false);

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

                RequestQueue queue = Volley.newRequestQueue(context);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, graphqlEndpoint, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("bufff", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("lkjlkj", error.toString());
                    }
                }) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String json = "{\"query\":\"mutation {" + entityName + "Create(entity: {" + agentIdParameterName + ": " + agentId + " " + locationParameterName + ": \\\"(" + latitude + "," + longitude + ")\\\"}) {id}}\",\"variables\":null}";
                        Log.i("json", json);
                        return json.getBytes();
                    }
                };
                queue.add(stringRequest);
            }
        }
    }
}
