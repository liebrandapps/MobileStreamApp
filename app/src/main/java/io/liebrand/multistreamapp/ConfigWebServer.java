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
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigWebServer {

    private static final String TAG = "ConfigWebServer";

    private final ExecutorService executor;
    private final Context ctx;
    private final AppContext appCtx;
    private ServerSocket serverSocket;
    //private Handler handler = new Handler(Looper.getMainLooper());

    public ConfigWebServer(Context context, AppContext appContext) {
        ctx = context;
        appCtx = appContext;
        executor = Executors.newSingleThreadExecutor();
        serverSocket = null;
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch(IOException ex) {}
        }
        serverSocket = null;
    }

    public void start() {
        assert executor != null;
        executor.execute(() -> {

            try {
                serverSocket= new ServerSocket(8181);
                while (true) {
                    //Log.i(TAG, "start: Waiting for Connection");
                    final Socket socket = serverSocket.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                boolean doRedirect = false;
                                int bytesRead = 0;
                                byte [] arrData = new byte[12000];

                                InputStream sockIS = socket.getInputStream();
                                    Thread.sleep(200);
                                while(sockIS.available()>0) {
                                    bytesRead += sockIS.read(arrData, bytesRead, arrData.length - bytesRead);
                                    Thread.sleep(100);
                                }
                                Log.i("CWS", String.format("Read %d bytes from socket", bytesRead));

                                String file = "index.html";
                                InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(arrData));
                                BufferedReader is = new BufferedReader( isr );
                                PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
                                String header = is.readLine();
                                Log.i("CWS", header);
                                if (header.startsWith("GET")) {
                                    String[] parts = header.split("\\s+");
                                    header = is.readLine();
                                    Map<String, String> headerFields = new HashMap<>();
                                    while (header != null && header.length()>0) {
                                        String[] arr = header.split(": ");
                                        headerFields.put(arr[0], arr[1]);
                                        header = is.readLine();
                                    }
                                    file = parts[1];
                                    if (file.equals("/")) {
                                        file = "index.html";
                                    }
                                }
                                assert header != null;
                                if (header.startsWith("POST")) {
                                    int processedLength = header.length() + 1;
                                    String[] parts = header.split("\\s+");
                                    if(parts[1].equals("/saveConfig")) {
                                        header = is.readLine();
                                        Map<String, String> headerFields = new HashMap<>();
                                        while (header != null && header.length() > 0) {
                                            String[] arr = header.split(": ");
                                            headerFields.put(arr[0], arr[1]);
                                            header = is.readLine();
                                            processedLength += header.length() + 1;
                                        }
                                        int expLength = Integer.parseInt(headerFields.get("Content-Length"));
                                        Log.i("CWS", String.format("Processed %d bytes, Content is %d bytes, read %d bytes, missing %d bytes", processedLength, expLength, bytesRead, expLength+processedLength-bytesRead));
                                        while(bytesRead<(expLength+processedLength)) {
                                            bytesRead += socket.getInputStream().read(arrData, bytesRead, expLength+processedLength-bytesRead);
                                        }

                                        char [] buffer = new char[expLength];
                                        int iRead = is.read(buffer);
                                        Log.i("CWS", String.format("Expected to read %d bytes, actually read %d bytes",
                                                        expLength, iRead));
                                        String body = String.copyValueOf(buffer,0, iRead);
                                        String encodedBody="";
                                        try {
                                            encodedBody = URLDecoder.decode(body, StandardCharsets.UTF_8.name());

                                            Map<String, String> kvPairs = new HashMap<>();
                                            String lines[] = encodedBody.substring("cfgview=".length()).split("\\r?\\n");
                                            appCtx.configuration.importFromIni(lines);
                                            Intent intent = new Intent(FullscreenActivity.INTENT_CFG_UPDATE);
                                            ctx.sendBroadcast(intent);
                                            doRedirect = true;
                                            file = "302.html";
                                        } catch (UnsupportedEncodingException e) {
                                            // not going to happen - value came from JDK's own StandardCharsets
                                        } catch (IllegalArgumentException e) {
                                            file = "errArg.html";
                                        }
                                    }
                                }
                                if(file.startsWith("/")) {
                                    file = file.substring(1);
                                }

                                Scanner scanner;
                                try {
                                    scanner = new Scanner(ctx.getAssets().open(file));
                                }
                                catch(FileNotFoundException e) {
                                    scanner = new Scanner(ctx.getAssets().open("404.html"));
                                }
                                /* scanner w/ Z will match the end of the file excluding a possible
                                   last new line
                                 */
                                String response = scanner.useDelimiter("\\Z").next();
                                if (response.contains("###")) {
                                    response = replaceTags(response);
                                }
                                if(doRedirect) {
                                    os.print("HTTP/1.0 302" + "\r\n");
                                    os.print("Location: /index.html\n\n");
                                }
                                else {
                                    os.print("HTTP/1.0 200" + "\r\n");
                                }
                                if(file.endsWith(".js")) {
                                    os.print("Content-Type: text/javascript" + "\r\n");
                                } else if(file.endsWith(".css")) {
                                    os.print("Content-Type: text/css" + "\r\n");
                                } else if(file.endsWith(".ico")) {
                                    os.print("Content-Type: image/x-icon" + "\r\n");
                                } else if(file.endsWith(".png")) {
                                    os.print("Content-Type: image/png" + "\r\n");
                                } else if(file.endsWith(".xml")) {
                                    os.print("Content-Type: application/xml" + "\r\n");
                                } else
                                {
                                    os.print("Content-Type: text/html" + "\r\n");
                                }
                                os.print("Content-length: " + response.length() + "\r\n");
                                os.print("\r\n");
                                os.print(response + "\r\n");
                                os.flush();
                                socket.close();
                            } catch (IOException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

    private Map<String, String> getServices() {
        if(!appCtx.enigma2.isEnabled) {
            return null;
        }
        else {
            try {
                String encRef = java.net.URLEncoder.encode(appCtx.enigma2.reference, StandardCharsets.UTF_8.name());
                URL url = new URL(String.format(appCtx.enigma2.epgNow, appCtx.enigma2.receiverIp, encRef));
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

                if (appCtx.enigma2.user != null && appCtx.enigma2.user.length() > 0) {
                    byte[] toEncrypt = (appCtx.enigma2.user + ":" + appCtx.enigma2.password).getBytes();
                    String encoded = Base64.encodeToString(toEncrypt, Base64.DEFAULT);
                    urlConn.setRequestProperty("Authorization", "Basic " + encoded);
                }
                InputStream epg = (InputStream) urlConn.getContent();
                return parseXml(epg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private Map<String, String> parseXml(InputStream inputStream){
        final String TAG_REFERENCE = "e2eventservicereference";
        final String TAG_SERVICENAME  = "e2eventservicename";
        final String TAG_EVENT = "e2event";
        String key = null;
        Map<String, String> tmpMap=null;
        Map<String, String> result = new HashMap<>();
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new InputStreamReader( inputStream) ); // pass input whatever xml you have
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                } else if(eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().equals(TAG_EVENT)) {
                        tmpMap = new HashMap<>();
                    }
                    key = xpp.getName();
                } else if(eventType == XmlPullParser.END_TAG) {
                    key = null;
                    if(xpp.getName().equals(TAG_EVENT)) {
                        if(tmpMap!=null && tmpMap.containsKey(TAG_REFERENCE) ) {
                            result.put(tmpMap.get(TAG_REFERENCE), tmpMap.get(TAG_SERVICENAME));
                        }
                        tmpMap = null;
                    }
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
        return result;
    }


    private String replaceTags(String replaceMe) {
        StringBuilder sb = new StringBuilder();
        int index=0;
        int lastIndex = 0;

        while ((index=replaceMe.indexOf("###", lastIndex))!=-1) {
            sb.append(replaceMe.substring(lastIndex, index));
            lastIndex = index + 3;
            index=replaceMe.indexOf("###", lastIndex);
            String tag = replaceMe.substring(lastIndex, index);
            // look up tag
            if(tag.equals("M3U8")) {
                for(Station station : appCtx.stationMap.values()) {
                    //sb.append(station.storeAsM3U());
                }
            }
            if(tag.equals("INI")) {
                sb.append(appCtx.configuration.exportToIniString());
            }
            if(tag.equals("SHOW_SERVICES")) {
                if (appCtx.enigma2.isEnabled) {
                    sb.append("display:inline-block");
                }
                else {
                    sb.append("display:none");
                }
            }
            if(tag.equals("SERVICES")) {
                Map<String,String> map = getServices();
                if (map!=null) {
                    for(String serviceId : map.keySet()) {
                        sb.append("<tr><td>");
                        sb.append(serviceId);
                        sb.append("</td><td>");
                        sb.append(map.get(serviceId));
                        sb.append("</td><td><button onclick=\"copyURL('");
                        sb.append(appCtx.enigma2.receiverIp);
                        sb.append("', '");
                        sb.append(serviceId);
                        sb.append("', '");
                        sb.append(map.get(serviceId));
                        sb.append("'");
                        sb.append(")\">URL</button>");
                        sb.append("</td></tr>");
                    }
                }
                else {
                    sb.append("<tr><td>No services</td></tr>");
                }
            }
            for(int i=1; i<9; i++) {
                String nameSlot = "NAME_SLOT" + String.valueOf(i);
                String urlSlot = "URL_SLOT" + String.valueOf(i);
                String userSlot = "_USER_SLOT" + String.valueOf(i);
                String passwordSlot ="_PASSWORD_SLOT" + String.valueOf(i);
                String checkSlot = "CHECK" + String.valueOf(i);
                if (tag.equals(nameSlot)) {
                    for (Station station : appCtx.stationMap.values()) {
                        if (station.slot == i) {
                            sb.append(station.text);
                            break;
                        }
                    }
                }
                if (tag.equals(urlSlot)) {
                    for (Station station : appCtx.stationMap.values()) {
                        if (station.slot == i) {
                            sb.append(station.url);
                            break;
                        }
                    }
                }
                if (tag.equals(userSlot)) {
                    for (Station station : appCtx.stationMap.values()) {
                        if (station.slot == i) {
                            if(station.user!=null && station.user.length()>0) {
                                sb.append(station.user);
                            }
                            break;
                        }
                    }
                }
                if (tag.equals(passwordSlot)) {
                    for (Station station : appCtx.stationMap.values()) {
                        if (station.slot == i) {
                            if(station.user!=null && station.user.length()>0) {
                                sb.append(station.password);
                            }
                            break;
                        }
                    }
                }
                if (tag.equals(checkSlot)) {
                    for (Station station : appCtx.stationMap.values()) {
                        if (station.slot == i) {
                            if(station.user!=null && station.user.length()>0) {
                                sb.append("checked");
                            }
                            break;
                        }
                    }
                }
                if (tag.equals(AppContext.KEY_LATITUDE)) {
                    SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    sb.append(String.valueOf(sPrefs.getFloat(AppContext.KEY_LATITUDE, (float) 52.52)));
                }
                if (tag.equals(AppContext.KEY_LONGITUDE)) {
                    SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    sb.append(String.valueOf(sPrefs.getFloat(AppContext.KEY_LONGITUDE, (float) 13.405)));
                }
            }
            lastIndex = index + 3;
        }
        sb.append(replaceMe.substring(lastIndex));
        return sb.toString();
    }

}
