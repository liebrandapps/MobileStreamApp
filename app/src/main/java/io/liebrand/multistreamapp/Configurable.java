package io.liebrand.multistreamapp;

import android.content.SharedPreferences;

import java.util.Map;

public interface Configurable {

    public void save(SharedPreferences.Editor editor);

    public void load(SharedPreferences sPrefs);

    public void exportToIni(StringBuilder sb);

    public void importFromIni(Map<String, String> map);
}
