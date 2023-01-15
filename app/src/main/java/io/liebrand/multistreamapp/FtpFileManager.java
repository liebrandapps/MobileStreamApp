package io.liebrand.multistreamapp;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;

public class FtpFileManager {
    private static final String FTP="FTP";

    public class Result {
        public boolean statusOk;
        public String errMsg;
        public FTPFile [] files;
    }

    private final String host;
    private final String user;
    private final String password;
    private final String initialDir;
    private final String name;

    public FtpFileManager(FTPConfiguration ftpConfiguration) {
        host = ftpConfiguration.ftpHost;
        user = ftpConfiguration.user;
        password = ftpConfiguration.password;
        initialDir = ftpConfiguration.getStartDir();
        name = ftpConfiguration.name;
    }

    public Result getListOfFiles(String dir) {
        Result res = new Result();
        try {
            FTPClient client = new FTPClient();
            client.connect(host);
            client.login(user, password);
            Log.i(FTP, client.getStatus());
            client.enterLocalPassiveMode();
            Log.i(FTP, client.printWorkingDirectory());
            String[] path = initialDir.split("/");
            for(String p : path) {
                if(client.changeWorkingDirectory(p)) {
                    Log.i(FTP, "Changed directory to " + p);
                }
                else {
                    Log.e(FTP, "Failed to change directory to " + p);
                }
            }
            Log.i(FTP, client.printWorkingDirectory());
            client.changeWorkingDirectory(dir);
            res.files = client.listFiles();
            client.logout();
            client.disconnect();
            res.statusOk = true;
            res.errMsg = "";
        } catch(IOException e) {
            res.statusOk = false;
            res.errMsg = e.getMessage();
        }
        return res;
    }

}
