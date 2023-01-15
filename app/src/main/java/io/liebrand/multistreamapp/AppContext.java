package io.liebrand.multistreamapp;

import java.util.HashMap;
import java.util.Map;

public class AppContext {

    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";

    public Map<Integer, Station> stationMap;
    public Configuration configuration;
    public Enigma2 enigma2;

    public AppContext() {
        configuration = new Configuration(this);
        stationMap = new HashMap<>();
        enigma2 = new Enigma2();
    }

}
