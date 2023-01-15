package io.liebrand.multistreamapp;

import android.content.SharedPreferences;

public class Enigma2 {

    public static final String SECTION = "enigma2";
    public static final String ENABLE = "enable";
    public static final String RECEIVERIP = "receiverIp";
    public static final String REFERENCE = "reference";
    private static final String DEFAULT_REF = "1:7:1:0:0:0:0:0:0:0:FROM BOUQUET \"userbouquet.favourites.tv\"";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String VPNBYPASS = "vpnByPass=";

    public final String epgNow = "http://%s/web/epgnow?bRef=%s";
    public final String epgNext = "http://%s/web/epgnownext?bRef=%s";

    public boolean isEnabled;
    public String receiverIp;
    public String user;
    public String password;
    public String vpnbypass;
    public String reference;

    public Enigma2() {
        isEnabled = false;
        receiverIp = "";
        user = "";
        reference = DEFAULT_REF;
    }

    public void save(SharedPreferences.Editor editor) {
        editor.putBoolean(SECTION + ":" + ENABLE, isEnabled);
        editor.putString(SECTION + ":" + RECEIVERIP, receiverIp);
        editor.putString(SECTION + ":" + USER, user);
        editor.putString(SECTION + ":" + PASSWORD, password);
        editor.putString(SECTION + ":" + REFERENCE, reference);
        editor.putString(SECTION + ":" + VPNBYPASS, vpnbypass);
    }

    public void load(SharedPreferences sPrefs) {
        isEnabled = sPrefs.getBoolean(SECTION + ":" + ENABLE, false);
        receiverIp = sPrefs.getString(SECTION+":"+ RECEIVERIP, "");
        user = sPrefs.getString(SECTION + ":" + USER, "");
        password = sPrefs.getString(SECTION + ":" + PASSWORD, "");
        reference = sPrefs.getString(SECTION + ":" + REFERENCE, DEFAULT_REF);
        vpnbypass = sPrefs.getString(SECTION + ":" + VPNBYPASS, "");
    }

    public void exportToIni(StringBuilder sb) {
        Configuration.addComment("Default is no, set to yes to query EPG info from receiver. ", sb);
        Configuration.addKeyValue(ENABLE, isEnabled? "yes" : "no", sb);
        Configuration.addKeyValue(RECEIVERIP, receiverIp, sb);
        Configuration.addComment("Query parameter for the receiver", sb);
        Configuration.addComment("the stations configured on this device must be also part of bouquet on the receiver, otherwise no station info can be retrieved.", sb);
        Configuration.addKeyValue(REFERENCE, reference, sb);
        Configuration.addComment("If the receiver requires login, set user and password: (uncomment & set the following 2 parameters", sb);
        if(user.length()==0) {
            Configuration.addComment(USER + "=", sb);
            Configuration.addComment(PASSWORD + "=", sb);
        }
        else {
            Configuration.addKeyValue(USER, user, sb);
            Configuration.addKeyValue(PASSWORD, password, sb);
        }
        Configuration.addComment("If the receiver can be reached through the internet in parallel to the VPN, you may add the host name here.", sb);
        Configuration.addComment("Purpose of this function is to avoid streaming through VPN (because the VPN is maybe too slow)", sb);
        if(vpnbypass==null || vpnbypass.length()==0) {
            Configuration.addComment(VPNBYPASS + "=", sb);
        }
        else {
            Configuration.addKeyValue(VPNBYPASS, vpnbypass, sb);
        }
    }




}
