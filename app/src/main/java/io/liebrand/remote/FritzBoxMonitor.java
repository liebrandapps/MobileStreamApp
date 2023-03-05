package io.liebrand.remote;

/*
  Mark Liebrand 2023

  This file is part of MobileStreamApp which is released under the Apache 2.0 License
  See file LICENSE or go to for full license details https://github.com/liebrandapps/MobileStreamApp
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

import io.liebrand.multistreamapp.AppContext;
import io.liebrand.multistreamapp.FullscreenActivity;

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
        int trycount = 0;
        while(!terminate) {
            if(appContext.fritzBox.isNetworkAvailable()) {
                if (appContext.fritzBox.isReachable()) {
                    Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2_STATUS);
                    intent.putExtra("msg", "Receiver is available");
                    ctx.sendBroadcast(intent);
                    break;
                }
                Date currentTime = new Date();
                if ((trycount == 3) && appContext.powerPlug.isEnabled) {
                    String msg = "WoL seems to be not successful, trying power cycle";
                    Log.i(TAG, msg);
                    appContext.powerPlug.doPowerCycle();
                    Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2_STATUS);
                    intent.putExtra("msg", msg);
                    ctx.sendBroadcast(intent);
                }
                else {
                    appContext.fritzBox.wakeup();
                }
                trycount += 1;
                Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2_STATUS);
                intent.putExtra("msg", String.format(Locale.ENGLISH,  "Trying to wakeup receiver [%d]", trycount));
                ctx.sendBroadcast(intent);
            }
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
