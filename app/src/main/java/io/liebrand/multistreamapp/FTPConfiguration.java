package io.liebrand.multistreamapp;

import android.content.SharedPreferences;

import java.util.Locale;

public class FTPConfiguration {

    private static final String TAG = "FTP";
    public static final String HOST = "host";
    public static final String NAME = "name";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String INITIALDIR = "initialDir";

    public int index;
    public String name;
    public String ftpHost;
    public String user;
    public String password;
    public String initialDir;
    public String subDir;

    public FTPConfiguration() {
        index = -1;
        initialDir = "/";
        subDir = "";
    }

        public boolean valid(StringBuilder sb) {
            boolean result = true;
            if (index==-1) {
                Configuration.printMessage(Configuration.SEV_WARN, TAG, "Class seems to be not initialized. Index is -1", sb);
            }
            if(name == null || name.length()==0) {
                Configuration.printMessage(Configuration.SEV_WARN, TAG, String.format(Locale.ENGLISH,
                        "Server Name not set for server %d. Using hostname instead", index), sb);
                name = ftpHost;
            }
            if(ftpHost == null || ftpHost.length()==0) {
                Configuration.printMessage(Configuration.SEV_ERROR, TAG, String.format(Locale.ENGLISH,
                        "FTP Host is not set for station %d", index), sb);
                result = false;
            }
            if(initialDir == null || initialDir.length()==0) {
                Configuration.printMessage(Configuration.SEV_INFO, TAG, String.format(Locale.ENGLISH,
                        "Initialdir is not set for station %d, using / instead", index), sb);
            }
            if(user == null) {
                Configuration.printMessage(Configuration.SEV_INFO, TAG, String.format(Locale.ENGLISH,
                        "No user/password set/required for server %d", index), sb);
                result = false;
            }
            else if(password == null || password.length()==0) {
                Configuration.printMessage(Configuration.SEV_INFO, TAG, String.format(Locale.ENGLISH,
                        "user but password is empty for server %d", index), sb);
                password = "";
            }
            return result;
        }

        public void save(SharedPreferences.Editor editor) {
            String strgIndex = "F" + String.valueOf(index);
            editor.putString(strgIndex + ":" + NAME, name);
            editor.putString(strgIndex + ":" + HOST, ftpHost);
            editor.putString(strgIndex + ":" + USER, user);
            editor.putString(strgIndex + ":" + PASSWORD, password);
            editor.putString(strgIndex + ":" + INITIALDIR, initialDir);
        }

        public void load(SharedPreferences sPrefs, int id) {
            name = sPrefs.getString("F" + id + ":" + NAME, "");
            ftpHost = sPrefs.getString("F" + id +":"+ HOST, "");
            user = sPrefs.getString("F" + id +":"+ USER, "");
            password = sPrefs.getString("F" + id +":"+ PASSWORD, "");
            initialDir = sPrefs.getString("F" + id + ":"+ INITIALDIR, "");
            index = id;
        }

        public void exportToIni(StringBuilder sb) {
            Configuration.addKeyValue(HOST, ftpHost, sb);
            Configuration.addKeyValue(USER, user, sb);
            Configuration.addKeyValue(PASSWORD, password, sb);
            Configuration.addKeyValue(INITIALDIR, initialDir, sb);
            Configuration.addKeyValue(NAME, name, sb);
        }

        public void changeDir(String newDir) {
            newDir = newDir.substring(1, newDir.length()-1);
            if(newDir.equals("..")) {
                int idx = subDir.lastIndexOf('/');
                if(idx>-1)
                    subDir = subDir.substring(0, idx);
            }
            else {
                subDir = subDir + "/" + newDir;
            }
        }

        public String getStartDir() {
            if(subDir.length()==0) {
                return initialDir;
            }
            else {
                return initialDir + subDir;
            }
        }
}
