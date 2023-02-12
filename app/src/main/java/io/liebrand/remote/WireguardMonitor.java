package io.liebrand.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.liebrand.multistreamapp.AppContext;
import io.liebrand.multistreamapp.FullscreenActivity;

public class WireguardMonitor extends Thread {
    private static final String TAG = "WireguardMonitor";
    private static final int sleepTime = 30;
    private Context ctx;
    private AppContext appContext;
    private boolean terminate;
    private static boolean isRunning=false;

    public WireguardMonitor(Context context, AppContext appContext)
    {
        ctx = context;
        this.appContext = appContext;
    }

    @Override
    public void run() {
        if(isRunning) return;
        isRunning = true;
        try {
            sleep(5000);
        } catch (InterruptedException ignored) {
        }
        Log.i(TAG, "Starting WireguardMonitor");
        terminate = false;
        while(!terminate) {
            Intent intent = new Intent(FullscreenActivity.INTENT_WG_MONITOR);
            if(appContext.wg.vpnActive() && appContext.wg.remoteHostReachable()) {
                intent.putExtra("showVPN", true);
            }
            else {
                intent.putExtra("showVPN", false);

            }
            ctx.sendBroadcast(intent);

            try {
                sleep(20000);
            } catch (InterruptedException ignored) {

            }
        }
        Log.i(TAG, "Terminating WireguardMonitor");
        isRunning = false;
    }

    public void terminate() {
        terminate = true;
        interrupt();
    }

}
