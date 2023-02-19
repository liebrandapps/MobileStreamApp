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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import io.liebrand.multistreamapp.Configurable;
import io.liebrand.multistreamapp.Configuration;

public class WireGuard implements Configurable {

    private static final String TAG = "WIREGUARD";

    public static final String SECTION = "WireGuard";
    public static final String ENABLE = "enable";
    public static final String TUNNEL = "tunnel";
    public static final String LOCAL_NETWORKS ="localNetworks";
    public static final String REMOTE_HOST = "remoteHost";

    private static final String INTENT_UP = "com.wireguard.android.action.SET_TUNNEL_UP";
    private static final String INTENT_DOWN = "com.wireguard.android.action.SET_TUNNEL_DOWN";
    private static final String EXTRA = "tunnel";


    public boolean isEnabled;
    public String tunnel;
    public String localNetworks;
    private boolean vpnIsOn;
    public String remoteHost;

    public WireGuard() {
        isEnabled = false;
        vpnIsOn = false;
        remoteHost="";
    }

    public void connectVPN(Context ctx, int ip) {
        if(!isEnabled) return;

        boolean local = false;
        int o1 = (int)(ip & 0xff);
        int o2 = (int)(ip >> 8 & 0xff);
        int o3 = (int)(ip >> 16 & 0xff);
        int o4 = (int)(ip >> 24 & 0xff);
        String [] ips = localNetworks.split(",");
        for(String ipRange : ips) {
            String [] octets = ipRange.trim().split("\\.");
            if(octets.length==4) {
                int x1 = Integer.parseInt(octets[0]);
                int x2 = Integer.parseInt(octets[1]);
                int x3 = Integer.parseInt(octets[2]);
                int x4 = Integer.parseInt(octets[3]);
                if (x1==255 || x1==o1) {
                    if(x2==255 || x2==o2) {
                        if(x3==255 || x3==o3) {
                            if(x4==255 || x4==o4) {
                                local = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if(local && vpnIsOn) {
            Intent intent = new Intent(INTENT_DOWN);
            intent.putExtra(EXTRA, tunnel);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setPackage("com.wireguard.android");
            ctx.sendBroadcast(intent);
            vpnIsOn = false;
        }

        if(local || (!local && vpnIsOn)) return;

        Intent intent = new Intent(INTENT_UP);
        intent.putExtra(EXTRA, tunnel);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setPackage("com.wireguard.android");
        ctx.sendBroadcast(intent);
        vpnIsOn = true;
    }

    public void disconnectVPN(Context ctx) {
        if(vpnIsOn) {
            Intent intent = new Intent(INTENT_DOWN);
            intent.putExtra(EXTRA, tunnel);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setPackage("com.wireguard.android");
            ctx.sendBroadcast(intent);
        }
    }

    @Override
    public void save(SharedPreferences.Editor editor) {
        editor.putBoolean(SECTION + ":" + ENABLE, isEnabled);
        editor.putString(SECTION + ":" + TUNNEL, tunnel);
        editor.putString(SECTION + ":" + LOCAL_NETWORKS, localNetworks);
        editor.putString(SECTION+":" + REMOTE_HOST, remoteHost);
    }

    @Override
    public void load(SharedPreferences sPrefs) {
        isEnabled = sPrefs.getBoolean(SECTION + ":" + ENABLE, false);
        tunnel = sPrefs.getString(SECTION+":"+ TUNNEL, "");
        localNetworks = sPrefs.getString(SECTION + ":" + LOCAL_NETWORKS, "");
        remoteHost = sPrefs.getString(SECTION + ":" + REMOTE_HOST, "");
    }

    @Override
    public void exportToIni(StringBuilder sb) {
        Configuration.addComment("This can be configured to turn on Wireguard VPN in case the beamer is not in a local network", sb);
        Configuration.addComment("WireGuard App needs to be installed and configured on the device", sb);
        Configuration.addComment("Default is no - do not use WireGuard", sb);
        Configuration.addKeyValue(ENABLE, isEnabled? "yes" : "no", sb);
        Configuration.addComment("Name of the tunnel configured in WireGuard", sb);
        Configuration.addKeyValue(TUNNEL, tunnel, sb);
        Configuration.addComment("If WireGuard is enabled in this section, it will automatically be turned on in case, the device is not in a local network", sb);
        Configuration.addComment("Networks are a comma separated list, use '255' as any. For example: '192.168.0.255, 192.168.2.255'", sb);
        Configuration.addKeyValue(LOCAL_NETWORKS, localNetworks, sb);
        Configuration.addComment("Specify an IP address that can be pinged successfully, when VPN is active", sb);
        Configuration.addComment("For example specify the address of your router at home", sb);
        Configuration.addKeyValue(REMOTE_HOST, remoteHost, sb);
    }

    @Override
    public void importFromIni(Map<String, String> map) {
        isEnabled = Configuration.convertToBoolean(map.getOrDefault(WireGuard.ENABLE, "no"));
        tunnel = map.get(WireGuard.TUNNEL);
        localNetworks = map.get(WireGuard.LOCAL_NETWORKS);
        remoteHost = map.get(WireGuard.REMOTE_HOST);
    }

    public boolean vpnActive() {
        return vpnIsOn;
    }

    public boolean remoteHostReachable() {
        InetAddress inet;
        boolean isReachable = false;
        try {
            inet = InetAddress.getByName(remoteHost);
            isReachable = inet.isReachable(5000);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return isReachable;
    }

}
