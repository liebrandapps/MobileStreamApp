package io.liebrand.remote;

import android.content.Context;
import android.content.Intent;

import io.liebrand.multistreamapp.AppContext;
import io.liebrand.multistreamapp.FullscreenActivity;

public class WireguardMonitor extends Thread {

    private static final int sleepTime = 30;
    private Context ctx;
    private AppContext appContext;
    private int width;

    public WireguardMonitor(Context context, AppContext appContext, int width)
    {
        ctx = context;
        this.appContext = appContext;
        this.width = width;
    }

    @Override
    public void run() {
        while(true) {
            Intent intent = new Intent(FullscreenActivity.INTENT_WG_MONITOR);
            if(appContext.wg.vpnActive() && appContext.wg.remoteHostReachable()) {
                intent.putExtra("showVPN", true);
            }
            else {
                intent.putExtra("showVPN", false);

            }
            intent.putExtra("stationWidth", width);
            ctx.sendBroadcast(intent);

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {

            }
        }
    }


}
