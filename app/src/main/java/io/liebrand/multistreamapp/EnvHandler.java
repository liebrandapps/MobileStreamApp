package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class EnvHandler extends Thread {
    private static final String TAG ="EnvHandler";
    private String urlString = "https://api.brightsky.dev/weather?lat=%s&lon=%s&date=%s&last_date=%s";

    private Context ctx;
    private AppContext appCtx;
    private boolean terminate;
    private static boolean isRunning = false;

    public EnvHandler(Context context, AppContext appContext) {
        ctx = context;
        appCtx = appContext;
    }

        @Override
    public void run() {
        if(isRunning) return;
        isRunning = true;
        try {
            sleep(3000);
        } catch (InterruptedException ignored) {
        }

        Log.i(TAG, "Starting EnvHandler");
        terminate = false;
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");

        while(!terminate) {
            Date dt = new Date();
            String today = sdf.format(dt);
            Calendar cal = new GregorianCalendar();
            cal.setTime(dt);
            cal.add(Calendar.DATE, 2);
            String tomorrow = sdf.format(cal.getTime());

            double longitude = appCtx.configuration.longitude;
            double latitude = appCtx.configuration.latitude;

            DecimalFormat df = new DecimalFormat("0.000");
            Intent intent = new Intent(FullscreenActivity.INTENT_ENV);

            try {
                URL url = new URL(String.format(urlString, df.format(latitude).replace(",", "."), df.format(longitude).replace(",", "."), today, tomorrow));
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                InputStream weather = (InputStream) urlConn.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(weather));
                StringBuilder sb = new StringBuilder();

                String inputStr;
                while ((inputStr = br.readLine()) != null)
                    sb.append(inputStr);
                JSONObject js = new JSONObject(sb.toString());
                JSONArray ja = js.getJSONArray("weather");
                JSONObject js2 = (JSONObject) ja.get(cal.get(Calendar.HOUR));
                String tempToday = js2.getString("temperature");
                String humidityToday = js2.getString("relative_humidity");
                String iconToday = js2.getString("icon");
                intent.putExtra(FullscreenActivity.XTRA_TEMPTODAY, tempToday);
                intent.putExtra(FullscreenActivity.XTRA_HUMIDITYTODAY, humidityToday);
                intent.putExtra(FullscreenActivity.XTRA_ICONTODAY, iconToday);
                if (ja.length() > 24) {
                    js2 = (JSONObject) ja.get(12 + 24);
                    String tempTomorrow = js2.getString("temperature");
                    String iconTomorrow = js2.getString("icon");
                    intent.putExtra(FullscreenActivity.XTRA_TEMPTOMORROW, tempTomorrow);
                    intent.putExtra(FullscreenActivity.XTRA_ICONTOMORROW, iconTomorrow);
                }
                Log.i(TAG, js.toString());

                String[] sun = calcSunRiseSet(latitude, longitude);
                intent.putExtra(FullscreenActivity.XTRA_SUNRISE, sun[0]);
                intent.putExtra(FullscreenActivity.XTRA_SUNSET, sun[1]);
                ctx.sendBroadcast(intent);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch(UnknownHostException e) {
                Log.e(TAG, e.getMessage());
            }catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                sleep(3600000);
            } catch (InterruptedException ignored) {

            }

        }
        Log.i(TAG, "Terminating EnvHandler");
        isRunning = false;
    }

    public void terminate() {
        terminate = true;
        interrupt();
    }

    private String[] calcSunRiseSet(double latitude, double longitude) {
        SunRiseAndSetTime srast;
        String [] result = new String[2];

        srast = new SunRiseAndSetTime(latitude, longitude);
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        result[0] = sdf.format(srast.CalcSunTime(now, true).getTime());
        result[1] = sdf.format(srast.CalcSunTime(now, false).getTime());
        return result;
    }
}
