package io.liebrand.multistreamapp;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;
import android.content.Intent;

import org.apache.commons.net.ftp.FTPFile;

import java.util.Locale;

public class FtpHandler extends Thread {

    FTPConfiguration ftpConfiguration;
    Context ctx;

    public FtpHandler(Context context, AppContext appCtx, FTPConfiguration ftpConfiguration) {
        this.ftpConfiguration = ftpConfiguration;
        ctx = context;
    }

    @Override
    public void run() {
        FtpFileManager ftp = new FtpFileManager(ftpConfiguration);
        FtpFileManager.Result result = ftp.getListOfFiles(".");
        Intent intent = new Intent(FullscreenActivity.INTENT_RCV_FILES);
        intent.putExtra("status", result.statusOk);
        if (!result.statusOk) {
            intent.putExtra("message", result.errMsg);
        }
        else {
            String url = String.format("ftp://%s:%s@%s/%s/", ftpConfiguration.user, ftpConfiguration.password, ftpConfiguration.ftpHost, ftpConfiguration.getStartDir());
            intent.putExtra("url", url);
            intent.putExtra("count", result.files.length);
            intent.putExtra("subdir", ftpConfiguration.subDir.length()==0? "/" : ftpConfiguration.subDir);
            int idx = 1;
            if(ftpConfiguration.subDir.length()>0) {
                String tag = String.format("%04d-", idx);
                intent.putExtra(tag + "name", "..");
                intent.putExtra(tag + "size", String.format(Locale.GERMANY, "%,15d", 0));
                intent.putExtra(tag + "isFile", false);
                idx += 1;
            }
            for (FTPFile fl : result.files) {
                String tag = String.format("%04d-", idx);
                intent.putExtra(tag + "name", fl.getName());
                intent.putExtra(tag + "size", String.format(Locale.GERMANY, "%,15d", fl.getSize()));
                intent.putExtra(tag + "isFile", fl.isFile());
                idx += 1;
                if (idx > 9999) break;
            }
        }
        ctx.sendBroadcast(intent);
    }
}
