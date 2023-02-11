package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import java.util.HashMap;
import java.util.Map;

import io.liebrand.remote.WireGuard;

public class AppContext {

    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";

    public Map<Integer, Station> stationMap;
    public Configuration configuration;
    public Enigma2 enigma2;
    public WireGuard wg;

    public AppContext() {
        configuration = new Configuration(this);
        stationMap = new HashMap<>();
        enigma2 = new Enigma2();
        wg = new WireGuard();
    }

}
