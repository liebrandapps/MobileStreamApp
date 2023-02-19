package io.liebrand.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.liebrand.multistreamapp.AppContext;
import io.liebrand.multistreamapp.FullscreenActivity;

public class PowerplugMonitor extends Thread {
    private static final String TAG = "PowerplugMonitor";

    private final Context ctx;
    private final AppContext appContext;
    private static boolean isRunning = false;
    private boolean terminate = false;
    private boolean logReachbility;

    public PowerplugMonitor(Context context, AppContext appContext) {
        ctx = context;
        this.appContext = appContext;
        terminate = false;
        logReachbility = false;
    }

    @Override
    public void run() {
        if (isRunning) return;
        isRunning = true;
        try {
            sleep(2500);
        } catch (InterruptedException ignored) {
        }
        Log.i(TAG, String.format("Starting %s", TAG));
        terminate = false;
        String msg;
        while (!terminate) {
            Powerplug.PlugStatus plugStatus = appContext.powerPlug.getStatus();
            if(plugStatus.isReachable) {
                if(!plugStatus.powerOn) {
                    msg = "Powerplug is turned off, going to turn on";
                    Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
                    intent.putExtra("message", msg);
                    ctx.sendBroadcast(intent);
                    Log.i(TAG, msg);
                    appContext.powerPlug.switchPower(true);
                }
                else { /* Plug is ON */
                    if(!plugStatus.isRunning) {
                        msg = "Powerplug is on, receiver seems to be off, doing a power cycle.";
                        appContext.powerPlug.doPowerCycle();
                    }
                    else { /* Plug is ON and Receiver is consuming power */
                        msg = "Powerplug is on, Receiver is possibly sleeping, nothing to do.";
                        terminate = true;
                    }
                    Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
                    intent.putExtra("message", msg);
                    ctx.sendBroadcast(intent);
                    Log.i(TAG, msg);
                }
            }
            else {
                if(!appContext.wg.isEnabled) {
                    msg = "Powerplug is not reachable, VPN is not configured. Giving up.";
                    terminate = true;
                }
                else {
                    msg = "Powerplug is not reachable, waiting for VPN. Will try again in 30 seconds.";
                }
                if(!logReachbility) {
                    Intent intent = new Intent(FullscreenActivity.INTENT_ENIGMA2);
                    intent.putExtra("message", msg);
                    ctx.sendBroadcast(intent);
                    Log.w(TAG, msg);
                }
                logReachbility = true;
            }

            try {
                sleep(30000);
            } catch (InterruptedException ignored) {

            }
        }
        Log.i(TAG, String.format("Terminating %s", TAG));
        isRunning = false;
    }

    public void terminate() {
        terminate = true;
        interrupt();
    }
}