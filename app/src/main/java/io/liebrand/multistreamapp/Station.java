package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.SharedPreferences;

public class Station {

    public static final String KEY_STATION_IDS = "stationIds";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String SLOT = "slot";
    public static final String MEDIA_TYPE = "mediaType";
    private static final String MEDIA_TYPE_HLS = "hls";
    private static final String MEDIA_TYPE_RTSP = "rtsp";
    public static final String SERVICE_ID = "serviceId";

    public static final int MTYPE_HLS = 0;
    public static final int MTYPE_RTSP = 1;
    public static final int MTYPE_FTP = 2;

    public int index;
    public String text;
    public String url;
    public int slot;
    public int mediaType;
    public String user;
    public String password;
    public String serviceId;

    public Station() {
        index = -1;
        mediaType = -1;
        serviceId = "";
        user = "";
    }

    /*
        @returns true if ok
     */
    public boolean valid(StringBuilder sb) {
        boolean result = true;
        String TAG = "STATION";
        if (index==-1) {
            Configuration.printMessage(Configuration.SEV_WARN, TAG, "Class seems to be not initialized. Index is -1", sb);
        }
        if(text == null || text.length()==0) {
            Configuration.printMessage(Configuration.SEV_ERROR, TAG, String.format("Station Name not set for station %d", index), sb);
            result = false;
        }
        if(url == null || url.length()==0) {
            Configuration.printMessage(Configuration.SEV_ERROR, TAG, String.format("URL is not set for station %d", index), sb);
            result = false;
        }
        if (!(slot >0 && slot<16)) {
            Configuration.printMessage(Configuration.SEV_ERROR, TAG, String.format("Slot %d is not in range (1-15) for station %d", slot, index), sb);
            result = false;
        }
        if(mediaType != 0 && mediaType != 1) {
            Configuration.printMessage(Configuration.SEV_ERROR, TAG, String.format("Mediatype %d not HLS(0) or RTSP(1) for station %d", mediaType, index), sb);
            result = false;
        }
        if(user == null) {
            Configuration.printMessage(Configuration.SEV_INFO, TAG, String.format("No user/password set/required for station %d", index), sb);
            result = false;
        }
        else if(password == null || password.length()==0) {
            Configuration.printMessage(Configuration.SEV_INFO, TAG, String.format("user but password is empty for station %d", index), sb);
            password = "";
        }
        return result;
    }

    public void save(SharedPreferences.Editor editor, int id) {
        String strgIndex = "S" + String.valueOf(id);
        editor.putString(strgIndex + ":" + NAME, text);
        editor.putString(strgIndex + ":" + URL, url);
        editor.putInt(strgIndex + ":" + SLOT, slot);
        editor.putInt(strgIndex + ":" + MEDIA_TYPE, mediaType);
        editor.putString(strgIndex + ":" + USER, user);
        editor.putString(strgIndex + ":" + PASSWORD, password);
        editor.putString(strgIndex + ":" + SERVICE_ID, serviceId);
    }

    public void load(SharedPreferences sPrefs, String id) {
        text = sPrefs.getString("S" + id + ":" + NAME, "");
        url = sPrefs.getString("S" + id + ":" + URL, "");
        slot = sPrefs.getInt("S" + id + ":" + SLOT, 1);
        mediaType = sPrefs.getInt("S" + id + ":" + MEDIA_TYPE, MTYPE_HLS);
        user = sPrefs.getString("S" + id + ":" + USER, "");
        password = sPrefs.getString("S" + id + ":" + PASSWORD, "");
        serviceId = sPrefs.getString("S" + id+ ":" + SERVICE_ID, "");
    }

    public void exportToIni(StringBuilder sb) {
        Configuration.addKeyValue(NAME, text, sb);
        Configuration.addKeyValue(URL, url, sb);
        Configuration.addKeyValue(MEDIA_TYPE, mediaType, sb);
        Configuration.addKeyValue(SLOT, slot, sb);
        if (user!=null && user.length() > 0) {
            Configuration.addKeyValue(USER, user, sb);
            Configuration.addKeyValue(PASSWORD, password, sb);
        }
        Configuration.addComment("In case the stream comes from an enigma2 receiver, configure the service id", sb);
        Configuration.addComment("This function requires the section 'enigma2' to be configured properly", sb);
        if(serviceId!= null && serviceId.length()>0) {
            Configuration.addKeyValue(SERVICE_ID, serviceId, sb);
        }
        else {
            Configuration.addComment(SERVICE_ID + "=", sb);
        }
    }
}
