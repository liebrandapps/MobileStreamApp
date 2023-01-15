package io.liebrand.multistreamapp;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Enigma2Handler extends Thread {
    private static final String TAG = "E2HDL";

    private static final String TAG_EVENT = "e2event";
    private static final String TAG_REFERENCE="e2eventservicereference";
    private static final String TAG_TITLE = "e2eventtitle";
    private static final String TAG_START = "e2eventstart";
    private static final String TAG_DURATION = "e2eventduration";
    private Enigma2 enigma2;
    private Station s;
    private Context ctx;

    public Enigma2Handler(Context context, AppContext appCtx, Station s) {
        this.enigma2 = appCtx.enigma2;
        this.s = s;
        ctx = context;
    }

    @Override
    public void run() {
        HashMap<String, String> result = new HashMap<>();
        boolean fail = false;
        if(s.serviceId==null || s.serviceId.length()==0) {
            result.put("status", "fail");
            result.put("message", String.format("no serviceId configure for %s", s.text));
            fail = true;
        }
        if(!fail) {
            if (s.serviceId.contains("%")) {
                try {
                    s.serviceId = java.net.URLDecoder.decode(s.serviceId, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    // not going to happen - value came from JDK's own StandardCharsets
                }
            }
        }
        if(!enigma2.isEnabled) {
            result.put("status", "fail");
            result.put("message", String.format("enigma2 interface is disabled.", s.text));
            fail = true;

        }
        if(!fail) {
            try {
                String encRef = java.net.URLEncoder.encode(enigma2.reference, StandardCharsets.UTF_8.name());
                URL url = new URL(String.format(enigma2.epgNow, enigma2.receiverIp, encRef));
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

                if (enigma2.user != null && enigma2.user.length() > 0) {
                    byte[] toEncrypt = (enigma2.user + ":" + enigma2.password).getBytes();
                    String encoded = Base64.encodeToString(toEncrypt, Base64.DEFAULT);
                    urlConn.setRequestProperty("Authorization", "Basic " + encoded);
                }
                InputStream epg = (InputStream) urlConn.getContent();
                parseXml("now", epg, result);
                url = new URL(String.format(enigma2.epgNext, enigma2.receiverIp, encRef));
                urlConn = (HttpURLConnection) url.openConnection();

                if (enigma2.user != null && enigma2.user.length() > 0) {
                    byte[] toEncrypt = (enigma2.user + ":" + enigma2.password).getBytes();
                    String encoded = Base64.encodeToString(toEncrypt, Base64.DEFAULT);
                    urlConn.setRequestProperty("Authorization", "Basic " + encoded);
                }
                epg = (InputStream) urlConn.getContent();
                parseXml("next", epg, result);
            } catch (FileNotFoundException e) {
                result.put("status", "fail");
                result.put("message", "URL not found: " + e.getMessage());
            } catch (MalformedURLException e) {
                result.put("status", "fail");
                result.put("message", "Error reading from receiver (Malformed URL?): " + e.getMessage());
            } catch (IOException e) {
                result.put("status", "fail");
                result.put("message", "Error reading from receiver: " + e.getMessage());
            }

        }
        Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
        for(String key : result.keySet()) {
            intent.putExtra(key, result.get(key));
        }
        ctx.sendBroadcast(intent);

    }

    private void parseXml(String tag, InputStream inputStream, Map<String, String> kvMap){
        String key = null;
        Map<String, String> tmpMap=null;
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new InputStreamReader( inputStream) ); // pass input whatever xml you have
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().equals(TAG_EVENT)) {
                        tmpMap = new HashMap<>();
                    }
                    else {
                        key = xpp.getName();
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if(xpp.getName().equals(TAG_EVENT)) {
                        if(tmpMap!=null && tmpMap.containsKey(TAG_REFERENCE) && tmpMap.get(TAG_REFERENCE).equals(s.serviceId)) {
                            Date startTime = new Date(Integer.parseInt(tmpMap.get(TAG_START)));
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            kvMap.put(tag+ ":title", tmpMap.get(TAG_TITLE));
                            kvMap.put(tag + ":start", sdf.format(startTime));
                            kvMap.put(tag + ":unixtime", tmpMap.get(TAG_START));
                            kvMap.put(tag + ":duration", tmpMap.get(TAG_DURATION));
                            StringBuilder sb = new StringBuilder();
                            for(String key1 : tmpMap.keySet()) {
                                sb.append(key1);
                                sb.append(":");
                                sb.append(tmpMap.get(key1));
                                sb.append(" | ");
                            }
                            Log.i(TAG, sb.toString());
                        }
                        tmpMap = null;
                    }
                    key = null;
                } else if(eventType == XmlPullParser.TEXT && tmpMap!=null & key!=null) {
                    tmpMap.put(key, xpp.getText());
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
