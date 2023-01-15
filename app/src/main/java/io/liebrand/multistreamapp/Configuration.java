package io.liebrand.multistreamapp;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
    Central storage for all config parameters
    + ability to load and save in application properties
    + ability to import and export as ini file
 */
public class Configuration {
    private static final String TAG = "CFG";
    private static final String INIFILE = "streamer.ini";

    public static final String SEV_ERROR = "Error";
    public static final String SEV_WARN = "Warning";
    public static final String SEV_INFO = "Info";
    public static final String SEV_DEBUG = "Debug";

    private static final String SECTION_GENERAL = "general";
    private static final String FTP_COUNT = "ftpServerCount";
    private static final String STATION_COUNT = "stationCount";
    private static final String SECTION_STATION = "station_%d";


    private static final String SECTION_FTP = "ftp_%d";


    public HashMap<Integer, Station> stationMap;

    public HashMap<Integer, FTPConfiguration> ftpServerList;

    private AppContext appContext;

    public Configuration(AppContext appContext) {
        ftpServerList = new HashMap<>();
        stationMap = new HashMap<>();
        this.appContext = appContext;
    }

    public boolean configExists(SharedPreferences sPrefs) {
        return (sPrefs.contains(TAG));
    }

    public void load(SharedPreferences sPrefs) {
        stationMap.clear();
        int stationCount = sPrefs.getInt(STATION_COUNT,0);
        for(int idx = 0; idx < stationCount; idx++) {
            Station s = new Station();
            s.load(sPrefs, String.valueOf(idx));
            stationMap.put(idx, s);
        }

        ftpServerList.clear();
        int ftpServercount = sPrefs.getInt(FTP_COUNT, 0);
        for(int idx = 0; idx < ftpServercount; idx++) {
            FTPConfiguration f = new FTPConfiguration();
            f.load(sPrefs, idx+1);
            ftpServerList.put(idx+1, f);
        }
        appContext.enigma2.load(sPrefs);
    }

    public void save(SharedPreferences sPrefs) {
       SharedPreferences.Editor editor = sPrefs.edit();

       for(Station s : stationMap.values()) {
           s.save(editor);
       }
       for(FTPConfiguration ftp : ftpServerList.values()) {
           ftp.save(editor);
       }
       editor.putInt(STATION_COUNT, stationMap.size());
       editor.putInt(FTP_COUNT, ftpServerList.size());
       appContext.enigma2.save(editor);

       DateFormat df = DateFormat.getDateTimeInstance();
       String now = df.format(new Date());

       editor.putString(TAG, now);
       editor.commit();
    }

    public void exportToIni(String path) {
        String data = exportToIniString();
        File f = new File(path, INIFILE);
        try {
            FileOutputStream outStream = new FileOutputStream(f);
            PrintWriter pw = new PrintWriter(outStream);
            pw.print(data);
            pw.flush();
            pw.close();
            outStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not Exception on write -> missing WRITE_EXTERNAL_STORAGE in manifest?");
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    public String exportToIniString() {
        int index;
        StringBuilder sb = new StringBuilder();

        addSection(SECTION_GENERAL, sb);
        addKeyValue(FTP_COUNT, ftpServerList.size(), sb);
        addKeyValue(STATION_COUNT, stationMap.size(), sb);
        sb.append("\n");

        index = 1;
        for (Station s : stationMap.values()) {
            addSection(String.format(Locale.ENGLISH, SECTION_STATION, index), sb);
            index += 1;
            s.exportToIni(sb);
            sb.append("\n");
        }

        index = 1;
        for (FTPConfiguration f : ftpServerList.values()) {
            addSection(String.format(Locale.ENGLISH, SECTION_FTP, index), sb);
            index += 1;
            f.exportToIni(sb);
            sb.append("\n");
        }
        addSection(Enigma2.SECTION, sb);
        appContext.enigma2.exportToIni(sb);

        return sb.toString();
    }

    private void addSection(String section, StringBuilder sb) {
        sb.append('[');
        sb.append(section);
        sb.append("]\n");
    }

    public static void addKeyValue(String key, String value, StringBuilder sb) {
        sb.append(key);
        sb.append("=");
        sb.append(value);
        sb.append("\n");
    }

    public static void addKeyValue(String key, int value, StringBuilder sb) {
        sb.append(key);
        sb.append("=");
        sb.append(value);
        sb.append("\n");
    }

    public static void addComment(String comment, StringBuilder sb)  {
        sb.append("# ");
        sb.append(comment);
        sb.append("\n");
    }

    public void importFromIni(String [] lines) {
        String section = null;
        Pattern patternSection = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
        Pattern patternKV = Pattern.compile("\\s*([^=]*)=(.*)");
        Map<String, Map<String, String>> entries = new HashMap<>();
        for(String line : lines) {
            Matcher m = patternSection.matcher(line);
            if (m.matches()) {
                section = m.group(1);
                if (section!=null) section = section.trim();
            } else if (section != null) {
                m = patternKV.matcher(line);
                if (m.matches()) {
                    String key = m.group(1);
                    String value = m.group(2);
                    if(key!=null && value!=null) {
                        Map<String, String> kv = entries.computeIfAbsent(section, k -> new HashMap<>());
                        kv.put(key.trim(), value.trim());
                    }
                }
            }
        }

        /* now transfer the values into config */
        stationMap.clear();
        ftpServerList.clear();
        Map<String, String> mapGeneral = entries.get(SECTION_GENERAL);
        if (mapGeneral!=null) {
            if (mapGeneral.containsKey(STATION_COUNT)) {
                int stationCount = Integer.parseInt(mapGeneral.get(STATION_COUNT));
                for (int idx = 0; idx < stationCount; idx++) {
                    Map<String, String> mapStation = entries.get(String.format(SECTION_STATION, idx+1));
                    if (mapStation!=null) {
                        Station s = new Station();
                        s.index = idx;
                        s.text = mapStation.get(Station.NAME);
                        s.url = mapStation.get(Station.URL);
                        s.slot = Integer.parseInt(mapStation.get(Station.SLOT));
                        s.mediaType = mapStation.containsKey(Station.MEDIA_TYPE)?
                                Integer.parseInt(mapStation.get(Station.MEDIA_TYPE)) : -1;
                        if (mapStation.containsKey(Station.USER)) {
                            s.user = mapStation.get(Station.USER);
                            s.password = mapStation.get(Station.PASSWORD);
                        }
                        s.serviceId = mapStation.getOrDefault(Station.SERVICE_ID, "");
                        stationMap.put(idx, s);
                    }
                }
            }
            if(mapGeneral.containsKey(FTP_COUNT)) {
                int ftpCount = Integer.parseInt(mapGeneral.get(FTP_COUNT));
                for(int idx = 0; idx < ftpCount; idx ++) {
                    Map<String, String> mapFtp = entries.get(String.format(SECTION_FTP, idx+1));
                    if (mapFtp!=null) {
                        FTPConfiguration ftp = new FTPConfiguration();
                        ftp.ftpHost = mapFtp.get(FTPConfiguration.HOST);
                        ftp.user = mapFtp.get(FTPConfiguration.USER);
                        ftp.password = mapFtp.get(FTPConfiguration.PASSWORD);
                        ftp.initialDir = mapFtp.get(FTPConfiguration.INITIALDIR);
                        ftp.name = mapFtp.get(FTPConfiguration.NAME);
                        ftp.index = idx+1;
                        ftpServerList.put(idx+1, ftp);
                    }
                }
            }
        }
        Map<String, String> mapEnigma2 = entries.get(Enigma2.SECTION);
        if(mapEnigma2!=null) {
            appContext.enigma2.isEnabled = convertToBoolean(mapEnigma2.getOrDefault(Enigma2.ENABLE, "no"));
            appContext.enigma2.receiverIp = mapEnigma2.get(Enigma2.RECEIVERIP);
            appContext.enigma2.user = mapEnigma2.getOrDefault(Enigma2.USER, "");
            appContext.enigma2.password = mapEnigma2.getOrDefault(Enigma2.PASSWORD, "");
            appContext.enigma2.vpnbypass = mapEnigma2.getOrDefault(Enigma2.VPNBYPASS, "");
        }
    }

    public FTPConfiguration getNextFtpServer(int index) {
        int next = index+1;
        if (ftpServerList.containsKey(next)) {
            return ftpServerList.get(next);
        }
        return null;
    }

    public FTPConfiguration getPrevFtpServer(int index) {
        int prev = index-1;
        if (ftpServerList.containsKey(prev)) {
            return ftpServerList.get(prev);
        }
        return null;
    }

    public FTPConfiguration getCurrentFtpServer(int index) {
        return ftpServerList.get(index);
    }

    private static boolean convertToBoolean(String value) {
        return "1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) ||
                "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }
    public static void printMessage(String severity, String tag, String message, StringBuilder sb) {
        DateFormat df = DateFormat.getDateTimeInstance();
        String now = df.format(new Date());
        sb.append(now).append(", ");
        sb.append(severity).append(", ");
        sb.append(tag).append(", ");
        sb.append(message).append(('\n'));
    }
}