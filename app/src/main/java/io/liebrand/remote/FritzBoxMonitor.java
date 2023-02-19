package io.liebrand.remote;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;
import android.util.Log;

import java.util.Date;

import io.liebrand.multistreamapp.AppContext;

public class FritzBoxMonitor extends Thread {
    private static final String TAG = "FritzboxMonitor";
    private static final int sleepTime = 30;
    private Context ctx;
    private final AppContext appContext;
    private boolean terminate;
    private static boolean isRunning=false;

    public FritzBoxMonitor(Context context, AppContext appContext)
    {
        ctx = context;
        this.appContext = appContext;
    }

    @Override
    public void run() {
        if(isRunning) return;
        isRunning = true;
        try {
            sleep(3000);
        } catch (InterruptedException ignored) {
        }
        Log.i(TAG, "Starting FritzboxMonitor");
        terminate = false;
        Date startTime = new Date();
        while(!terminate) {
            if(appContext.fritzBox.isReachable()) {
                break;
            }
            Date currentTime = new Date();
            if (((currentTime.getTime() - startTime.getTime())/1000)>90) {
                if(appContext.powerPlug.isEnabled) {
                    Log.i(TAG, "WoL seems to be not successful, trying power cycle");
                    appContext.powerPlug.doPowerCycle();
                }
            }
            appContext.fritzBox.wakeup();

            try {
                sleep(30000);
            } catch (InterruptedException ignored) {

            }
        }
        Log.i(TAG, "Terminating FritzboxMonitor");
        isRunning = false;
    }

    public void terminate() {
        terminate = true;
        interrupt();
    }

}
