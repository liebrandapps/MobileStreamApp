package io.liebrand.remote;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.liebrand.multistreamapp.Configurable;
import io.liebrand.multistreamapp.Configuration;
import io.liebrand.multistreamapp.FullscreenActivity;

public class FritzBox implements Configurable {

    private static final String TAG = "FritzBox";

    public static final String SECTION = "fritzbox";
    public static final String ENABLE = "enable";
    public static final String BOXIP = "boxIp";
    public static final String USE_SSL = "useSSL";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String MAC = "mac";
    public static final String SATIP = "receiver";

    public boolean isEnabled;
    public String boxIp;
    public boolean useSSL;
    public String user;
    public String password;
    public String mac;
    public String receiverHost;

    private static final String url1 = "%s://%s/login_sid.lua";
    private static final String url2 = "%s://%s/login_sid.lua?username=%s&response=%s";
    private static final String url3 = "%s://%s/data.lua";
    //wol=$($WEBCLIENT "$FritzBoxURL/net/network_user_devices.lua?sid=$SID" | grep '"name"] = ' -B2 | grep $2 -B2 |grep mac | sed -e 's/\["//g' -e 's/\"]//g' -e 's/\"//g' -e 's/mac =//' -e 's/,//' -e 's/^[ \t]*//;s/[ \t]*$//')
    private static WhitelistHostnameVerifier whitelistHostnameVerifier;
    private Context ctx;
    private boolean statusChanged;
    private boolean msgShown1;
    private boolean msgShown2;

    public FritzBox(Context ctx) {
        isEnabled = false;
        this.ctx = ctx;
        statusChanged = false;
        msgShown1 = false;
        msgShown2 = false;
    }

    public boolean isReachable() {
        boolean isReachable = false;
        InetAddress inet;
        try {
            inet = InetAddress.getByName(receiverHost);
            isReachable = inet.isReachable(2500);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return isReachable;
    }

    public boolean wakeup() {
        Log.i(TAG, "Trying to wake up " + receiverHost);
        boolean result = false;

        if(useSSL) {
            whitelistHostnameVerifier = new WhitelistHostnameVerifier(boxIp);
            HttpsURLConnection.setDefaultHostnameVerifier(whitelistHostnameVerifier);
        }

        InetAddress inet;
        boolean isReachable = false;
        try {
            inet = InetAddress.getByName(receiverHost);
            isReachable = inet.isReachable(2500);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        if(isReachable) {
            String msg = "Receiver is running, no need to wake up";
            Log.i(TAG, msg);
            if(statusChanged) {
                Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
                intent.putExtra("message", msg);
                ctx.sendBroadcast(intent);
            }
            result = true;
        }
        else {
            statusChanged = true;
            try {
                inet = InetAddress.getByName(boxIp);
                isReachable = inet.isReachable(2500);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            if(!isReachable) {
                String msg =String.format("Receiver [%s] AND Fritzbox [%s] are not reachable, cannot wake up, waiting for VPN",
                        receiverHost, boxIp);
                Log.i(TAG, msg);
                if(!msgShown1) {
                    Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
                    intent.putExtra("message", msg);
                    ctx.sendBroadcast(intent);
                    msgShown1 = true;
                }
                return false;
            }
            if(!msgShown2) {
                String msg = "Receiver is not reachable, trying to wakeup, please be patient";
                Log.i(TAG, msg);
                Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
                intent.putExtra("message", msg);
                ctx.sendBroadcast(intent);
                msgShown2 = true;
            }

            String sessionId = getSessionId();

            if (sessionId != null) {
                String version = getVersion(sessionId);
                Log.d(TAG, "Success getting a session id");
                if(version != null) {
                    Log.d(TAG, String.format("Success getting fb version (%s)", version));
                    String uid = getUID(sessionId);

                    if(uid!=null) {
                        Log.d(TAG, String.format("Success getting uid (%s) for mac (%s)", uid, mac));
                        HashMap<String, String> kvPairs = new HashMap<>();
                        kvPairs.put("sid", sessionId);
                        kvPairs.put("dev", uid);
                        kvPairs.put("oldpage", "net/edit_device.lua");
                        kvPairs.put("page", version.compareTo("7.25")==-1? "edit_device2" : "edit_device");
                        kvPairs.put("btn_wake", "");
                        try {
                            HttpResult httpResult = doPost(String.format(url3, useSSL ? "https" : "http", boxIp), kvPairs);
                            if(httpResult.responseCode==200) {
                                if (httpResult.contentType.startsWith("application/json")) {
                                    JSONObject json = new JSONObject(httpResult.content);
                                    String btn = json.getJSONObject("data").getString("btn_wake");
                                    if (btn.equals("ok")) {
                                        Log.i(TAG, String.format("Success sending wake up request for mac %s", mac));
                                        result = true;
                                    }
                                }
                                else {
                                    result = true;
                                }
                            }
                            else {
                                Log.e(TAG, String.format("Something went wrong communicating with fb (http code %d)", httpResult.responseCode));
                            }
                        } catch (IOException | JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    else {
                        Log.e(TAG, String.format( "Failed to retrieve uid for mac %s from fb", mac));
                    }
                }
                else {
                    Log.e(TAG, "Failed to retrieve fb version");
                }
            } else {
                Log.e(TAG, String.format("Failed to get session id from fb at %s", boxIp));
            }
        }
        Log.i(TAG, "Finished wake up");
        return result;
    }

    private String getSessionId() {
        String sessionId = null;
        String challenge=null;
        try {
            URL url = new URL(String.format(url1, useSSL? "https" : "http", boxIp));
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            InputStream data = (InputStream) urlConn.getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(data);
            NodeList elemList= document.getElementsByTagName("SID");
            if(elemList!=null && elemList.getLength()>0) {
                sessionId = elemList.item(0).getTextContent();
            }
            elemList= document.getElementsByTagName("Challenge");
            if(elemList!=null && elemList.getLength()>0) {
                challenge = elemList.item(0).getTextContent();
            }
            if(sessionId!=null && sessionId.equals("0000000000000000")) {
                sessionId = null;
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte [] hash = md5.digest((challenge+"-"+password).getBytes(StandardCharsets.UTF_16LE));
                String hashString = new BigInteger(1,hash).toString(16);
                String response = challenge + "-" + hashString;
                url = new URL(String.format(url2, useSSL? "https" : "http", boxIp, user, response));
                urlConn = (HttpURLConnection) url.openConnection();
                data = (InputStream) urlConn.getContent();
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = builder.parse(data);
                elemList= document.getElementsByTagName("SID");
                if(elemList!=null && elemList.getLength()>0) {
                    sessionId = elemList.item(0).getTextContent();
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sessionId;
    }

    private String getVersion(String sessionId) {
        String version = null;
        HashMap<String, String> kvPairs = new HashMap<>();
        kvPairs.put("sid", sessionId);
        kvPairs.put("page", "overview");
        try {
            HttpResult httpResult = doPost(String.format(url3, useSSL ? "https" : "http", boxIp), kvPairs);
            if (httpResult.responseCode==200) {
                JSONObject json = new JSONObject(httpResult.content);
                JSONObject jdata = json.getJSONObject("data");
                JSONObject fritzos = jdata.getJSONObject("fritzos");
                version = fritzos.getString("nspver");
            }
        }
        catch(IOException | JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return version;
    }

    private String getUID(String sessionId) {
        String uid = null;
        HashMap<String, String> kvPairs = new HashMap<>();
        kvPairs.put("sid", sessionId);
        kvPairs.put("page", "netDev");
        kvPairs.put("xhrId", "all");
        try {
            HttpResult httpResult = doPost(String.format(url3, useSSL ? "https" : "http", boxIp), kvPairs);
            if (httpResult.responseCode==200) {
                JSONObject json = new JSONObject(httpResult.content);
                JSONObject jdata = json.getJSONObject("data");
                JSONArray passiveDevices = jdata.getJSONArray("passive");
                for(int i=0; i<passiveDevices.length(); i++) {
                    if(mac.equals(passiveDevices.getJSONObject(i).getString("mac"))) {
                        uid = passiveDevices.getJSONObject(i).getString("UID");
                        break;
                    }
                }
                if(uid==null) {
                    JSONArray activeDevices = jdata.getJSONArray("active");
                    for(int i=0; i<activeDevices.length(); i++) {
                        if(mac.equals(activeDevices.getJSONObject(i).getString("mac"))) {
                            uid = activeDevices.getJSONObject(i).getString("UID");
                            break;
                        }
                    }
                }
            }
        }
        catch(IOException | JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return uid;
    }

    private HttpResult doPost(String url, HashMap<String, String> kvPair) throws IOException {
        HttpResult httpResult = new HttpResult();
        String data = null;
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String,String> entry : kvPair.entrySet()) {
            if(sb.length()>0) sb.append("&");
            sb.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
        }
        String body = sb.toString();

        HttpURLConnection urlConn;
        URL mUrl = new URL(url);
        urlConn = (HttpURLConnection) mUrl.openConnection();

        urlConn.setRequestMethod("POST");
        urlConn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Content-Length", Integer.toString(body.length()));
        urlConn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        httpResult.responseCode = urlConn.getResponseCode();
        httpResult.contentType = urlConn.getContentType();
        if (httpResult.responseCode==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            httpResult.content = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                httpResult.content += inputLine;
            in.close();
        }
        else {
            Log.e(TAG, String.format("POST request failed with responseCode %d", httpResult.responseCode));
        }
        return httpResult;
    }

    public void save(SharedPreferences.Editor editor) {
        editor.putBoolean(SECTION + ":" + ENABLE, isEnabled);
        editor.putString(SECTION + ":" + BOXIP, boxIp);
        editor.putBoolean(SECTION + ":" + USE_SSL, useSSL);
        editor.putString(SECTION + ":" + USER, user);
        editor.putString(SECTION + ":" + PASSWORD, password);
        editor.putString(SECTION+":" + MAC, mac);
        editor.putString(SECTION + ":" + SATIP, receiverHost);
    }

    public void load(SharedPreferences sPrefs) {
        isEnabled = sPrefs.getBoolean(SECTION + ":" + ENABLE, false);
        boxIp = sPrefs.getString(SECTION + ":" + BOXIP, "");
        useSSL = sPrefs.getBoolean(SECTION +":" + USE_SSL, false);
        user = sPrefs.getString(SECTION+":"+ USER, "");
        password = sPrefs.getString(SECTION + ":" + PASSWORD, "");
        mac = sPrefs.getString(SECTION + ":" + MAC, "");
        receiverHost = sPrefs.getString(SECTION + ":" + SATIP, "");
    }

    public void exportToIni(StringBuilder sb) {
        Configuration.addComment("The App can try to wake up the satellite receiver, in case it is not reachable", sb);
        Configuration.addComment("The wake up is sent after start of the application, in case the receiverHost is not reachable", sb);
        Configuration.addComment("The wakeup is not sent directly from this app, but from a (remote) Fritzbox", sb);
        Configuration.addComment("Default is no - do not use Wake on LAN", sb);
        Configuration.addKeyValue(ENABLE, isEnabled? "yes" : "no", sb);
        Configuration.addComment("IP address of the fritzbox", sb);
        Configuration.addKeyValue(BOXIP, boxIp, sb);
        Configuration.addComment("Fritzbox user.", sb);
        Configuration.addComment("SSL: https is used, otherwise http", sb );
        Configuration.addKeyValue(USE_SSL, useSSL? "yes" : "no", sb);
        Configuration.addKeyValue(USER, user, sb);
        Configuration.addComment("Fritzbox password (for the above user)", sb);
        Configuration.addKeyValue(PASSWORD, password, sb);
        Configuration.addComment("MAC Address of the receiver", sb);
        Configuration.addKeyValue(MAC, mac, sb);
        Configuration.addComment("Hostname of IP of the Sat-Receiver.", sb);
        Configuration.addKeyValue(SATIP, receiverHost, sb);
    }

    @Override
    public void importFromIni(Map<String, String> map) {
        isEnabled = Configuration.convertToBoolean(map.getOrDefault(FritzBox.ENABLE, "no"));
        boxIp = map.get(FritzBox.BOXIP);
        useSSL =  Configuration.convertToBoolean((map.getOrDefault(FritzBox.USE_SSL, "no")));
        user = map.get(FritzBox.USER);
        password = map.get(FritzBox.PASSWORD);
        mac = map.get(FritzBox.MAC);
        receiverHost = map.get(FritzBox.SATIP);
    }

    class HttpResult {
        public int responseCode;
        public String contentType;
        public String content;
    }

    static class WhitelistHostnameVerifier implements HostnameVerifier {

        private Set whitelist = new HashSet<>();
        private HostnameVerifier defaultHostnameVerifier =
                HttpsURLConnection.getDefaultHostnameVerifier();

        WhitelistHostnameVerifier(String... hostnames) {
            for (String hostname : hostnames) {
                whitelist.add(hostname);
            }
        }

        @Override
        public boolean verify(String host, SSLSession session) {
            if (whitelist.contains(host)) {
                return true;
            }
            // important: use default verifier for all other hosts
            return defaultHostnameVerifier.verify(host, session);
        }
    }

}
