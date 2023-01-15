package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class M3U8Handler extends Thread {

    private static final String EXT_INF = "#EXT-X-STREAM-INF";
    private static final String EXT_MEDIA = "#EXT-X-MEDIA";
    private static final int resX = 1280;
    private static final int resY = 720;
    private static final int bandwidth = 10000000;
    private static final String KEY_BANDWIDTH ="BANDWIDTH";
    private static final String KEY_TYPE = "TYPE";
    private static final String TYPE_AUDIO = "AUDIO";
    private static final String KEY_GROUP_ID = "GROUP-ID";
    private static final String KEY_DEFAULT = "DEFAULT";
    private static final String YES = "YES";
    private static final String KEY_URI = "URI";
    private static final String KEY_AUDIO = "AUDIO";


    private Station station;
    private Context ctx;
    private String stationName;

    public M3U8Handler(Context context, Station s) {
        super();
        station = s;
        ctx = context;
        this.stationName = stationName;
    }

    @Override
    public void run() {
        URL url = null;
        try {
            url = new URL(station.url);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

            if (station.user!=null && station.user.length()>0) {
                byte[] toEncrypt = (station.user + ":" + station.password).getBytes();
                String encoded = Base64.encodeToString(toEncrypt, Base64.DEFAULT);
                urlConn.setRequestProperty("Authorization", "Basic " + encoded);
            }
            InputStream M3U8 = (InputStream) urlConn.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(M3U8));
            String line;
            String foundUrl = "";
            String foundAudioId = null;
            int foundBandwidth = 0;
            String delims = "[:,]";
            String delims2 = "[=]";
            Map<String, String> audioMap = new HashMap<>();
            String lastLine = "";
            while ((line=br.readLine())!=null) {
                lastLine = line;
                Map<String, String> kvPairs = new HashMap<>();
                if (line.startsWith(EXT_MEDIA) || line.startsWith(EXT_INF)) {
                    // Hack to prevent URLs to be split
                    String[] tokens = line.replace("://", "!//").split(delims);
                    for (String token : tokens) {
                        String[] kv = token.split(delims2);
                        if (kv.length > 1) {
                            kvPairs.put(kv[0], kv[1].replace("!//", "://").replace("\"", ""));
                        }
                    }
                }

                if (line.startsWith(EXT_INF)) {

                    if (kvPairs.containsKey(KEY_BANDWIDTH)) {
                        int targetBandwidth = Integer.parseInt(kvPairs.get(KEY_BANDWIDTH));
                        if (targetBandwidth > foundBandwidth && targetBandwidth <= bandwidth) {
                            foundBandwidth = targetBandwidth;
                            if (kvPairs.containsKey(KEY_AUDIO)) {
                                foundAudioId = kvPairs.get(KEY_AUDIO);
                            }
                            else {
                                foundAudioId = null;
                            }
                            foundUrl = br.readLine();
                        }
                    }
                }

                if (line.startsWith(EXT_MEDIA)) {
                    if (kvPairs.containsKey(KEY_TYPE) && kvPairs.containsKey(KEY_GROUP_ID) && kvPairs.containsKey(KEY_DEFAULT)
                            && kvPairs.containsKey(KEY_URI)) {
                        if (kvPairs.get(KEY_TYPE).equals(TYPE_AUDIO) && kvPairs.get(KEY_DEFAULT).equals(YES)) {
                            audioMap.put(kvPairs.get(KEY_GROUP_ID), kvPairs.get(KEY_URI).replace("!//", "://"));
                        }
                    }
                }
            }
            br.close();
            String audioUrl = "";
            if(foundAudioId!=null && audioMap.containsKey(foundAudioId)) {
                audioUrl = audioMap.get(foundAudioId);
            }
            String videoUrl;

            if ("".equals(foundUrl)) {
                videoUrl = lastLine;
            }
            else if (foundUrl.startsWith("http")) {
                videoUrl = foundUrl;
            }
            else {
                StringBuilder mediaUrl = new StringBuilder();
                String[] segments = station.url.split("/");

                for (int i = 0; i < segments.length - 1; i++) {
                    mediaUrl.append(segments[i]).append("/");
                }
                videoUrl = mediaUrl.toString() + foundUrl;
                if(audioUrl != null && audioUrl.length()>0) {
                    audioUrl = mediaUrl.toString() + audioUrl;
                }
            }

            Intent intent = new Intent(FullscreenActivity.INTENT_LOAD_M3U8);
            intent.putExtra(FullscreenActivity.XTRA_STATUS, FullscreenActivity.XTRA_OK);
            intent.putExtra(FullscreenActivity.XTRA_STATION_ID, station.slot);
            intent.putExtra(FullscreenActivity.XTRA_URL_VIDEO, videoUrl);
            intent.putExtra(FullscreenActivity.XTRA_URL_AUDIO, audioUrl);
            ctx.sendBroadcast(intent);

        } catch(FileNotFoundException e) {
            Intent intent = new Intent(FullscreenActivity.INTENT_LOAD_M3U8);
            intent.putExtra(FullscreenActivity.XTRA_STATUS, FullscreenActivity.XTRA_FAIL);
            intent.putExtra(FullscreenActivity.XTRA_MESSAGE, "File not found: " + e.getMessage());
            ctx.sendBroadcast(intent);
        } catch(NoRouteToHostException e) {
            Intent intent = new Intent(FullscreenActivity.INTENT_LOAD_M3U8);
            intent.putExtra(FullscreenActivity.XTRA_STATUS, FullscreenActivity.XTRA_FAIL);
            intent.putExtra(FullscreenActivity.XTRA_MESSAGE, "No Route to Host: " + station.url);
            ctx.sendBroadcast(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
