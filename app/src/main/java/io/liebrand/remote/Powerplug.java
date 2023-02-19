package io.liebrand.remote;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

import io.liebrand.multistreamapp.Configurable;
import io.liebrand.multistreamapp.Configuration;

/**
 * This class implements an interface to a powerplug with measurement for power consumption
 *
 * Idea is to compensate for the problem of linux receivers that sometimes do not go into
 * Wake on LAN mode, but go to full power of instead.
 */
public class Powerplug implements Configurable {
    private static final String TAG = "Powerplug";

    public static final String SECTION = "Powerplug";
    private static final String ENABLE = "enable";
    private static final String PLUGIP = "plugIp";
    private static final String POWERCYCLETIME = "powerCycleTime";
    private static final String CONSIDERWOL = "considerWoL";

    private static final int DEFAULT_CYCLE_FILE = 30;
    private static final String URL_POWER = "http://%s/cm?cmnd=status 8";
    private static final String URL_ONOFF = "http://%s/cm?cmnd=Power %s";

    public boolean isEnabled;
    private String plugIp;
    private int powerCycleTime;


    public Powerplug() {
        isEnabled = false;
        powerCycleTime = DEFAULT_CYCLE_FILE;
        plugIp = "";
    }

    /**
     *
     * @return
     */
    public Powerplug.PlugStatus getStatus() {
        PlugStatus plugStatus = new PlugStatus();
        try {
            InetAddress inet;
            boolean isReachable=false;
            try {
                inet = InetAddress.getByName(plugIp);
                isReachable = inet.isReachable(2500);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            plugStatus.isReachable = isReachable;
            if(isReachable) {
                URL url = new URL(String.format(URL_POWER, plugIp));
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                int responseCode = urlConn.getResponseCode();
                String content;
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    content = "";
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                        content += inputLine;
                    in.close();
                    JSONObject json = new JSONObject(content);
                    JSONObject statusSNS = json.getJSONObject("StatusSNS");
                    JSONObject energy = statusSNS.getJSONObject("ENERGY");
                    String power = energy.getString("Power");
                    if ("0".equals(power)) {
                        plugStatus.isRunning = false;
                    } else {
                        plugStatus.isRunning = true;
                    }
                    String current = energy.getString("Current");
                    if("0".equals(current)) {
                        plugStatus.powerOn = false;
                    }
                    else {
                        plugStatus.powerOn = true;
                    }
                }
            }
        }
        catch(IOException | JSONException e) {
            e.printStackTrace();
        }
        return plugStatus;
    }

    public boolean switchPower(boolean on) {
        try {
            URL url = new URL(String.format(URL_ONOFF, plugIp, on? "on" : "off"));
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            int responseCode = urlConn.getResponseCode();
            return responseCode==200;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public void doPowerCycle() {
        new PowerTask().execute();
    }

    @Override
    public void save(SharedPreferences.Editor editor) {
        editor.putBoolean(ENABLE, isEnabled);
        editor.putString(PLUGIP, plugIp);
        editor.putInt(POWERCYCLETIME, powerCycleTime);
    }

    @Override
    public void load(SharedPreferences sPrefs) {
        isEnabled = sPrefs.getBoolean(ENABLE, isEnabled);
        plugIp = sPrefs.getString(PLUGIP, plugIp);
        powerCycleTime = sPrefs.getInt(POWERCYCLETIME, powerCycleTime);
    }

    @Override
    public void exportToIni(StringBuilder sb) {
        Configuration.addComment("Default is no - do not use plug", sb);
        Configuration.addKeyValue(ENABLE, isEnabled? "yes" : "no", sb);
        Configuration.addComment("IP of the plug", sb);
        Configuration.addKeyValue(PLUGIP, plugIp, sb);
        Configuration.addComment("Time in seconds for power cycle OFF ...wait(x seconds) ... ON", sb);
        Configuration.addKeyValue(POWERCYCLETIME, powerCycleTime, sb);
    }

    @Override
    public void importFromIni(Map<String, String> map) {
        isEnabled = Configuration.convertToBoolean(map.getOrDefault(ENABLE, "no"));
        plugIp = map.get(PLUGIP);
        powerCycleTime = Integer.parseInt(map.get(POWERCYCLETIME));
    }

    private class PowerTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String ... params) {
            Log.i(TAG, "Starting Powercycle");
            switchPower(false);
            try {
                Thread.sleep(powerCycleTime * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switchPower(true);
            Log.i(TAG, "Finished Powercycle");
            return  null;
        }
    }

    public class PlugStatus {
        public boolean isReachable;
        public boolean powerOn;
        public boolean isRunning;

        public PlugStatus() {
            isReachable = false;
            powerOn = false;
            isRunning = false;
        }
    }
}
