package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.liebrand.remote.Enigma2;
import io.liebrand.remote.FritzBox;
import io.liebrand.remote.Powerplug;
import io.liebrand.remote.WireGuard;

public class AppContext {


    public Map<Integer, Station> stationMap;
    public Configuration configuration;
    public Enigma2 enigma2;
    public WireGuard wg;
    public EnvHandler envHandler;
    public ConfigWebServer configWebServer;
    public FritzBox fritzBox;
    public Powerplug powerPlug;

    public AppContext(Context context) {
        configuration = new Configuration(this);
        stationMap = new HashMap<>();
        enigma2 = new Enigma2();
        wg = new WireGuard();
        envHandler = new EnvHandler(context, this);
        configWebServer = new ConfigWebServer(context, this);
        fritzBox = new FritzBox(context);
        powerPlug = new Powerplug();
    }

}
