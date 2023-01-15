package io.liebrand.multistreamapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.liebrand.exoplayer2.ExDataSource;
import io.liebrand.multistreamapp.databinding.ActivityFullscreenBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MODE_VIEW_BUTTONS = 0;
    private static final int MODE_VIEW_ONLY = 1;
    private static final int MODE_FILES = 2;

    /* up, down, left, right */
    private Map<Integer, int[]> navMap;
    private int navFocus = R.id.station1;
    private static final int [] navst1 = { R.id.btnExit, R.id.station9, R.id.station8, R.id.station2 };
    private static final int [] navst2 = { R.id.btnExit, R.id.station10, R.id.station1, R.id.station3 };
    private static final int [] navst3 = { R.id.btnExit, R.id.station11, R.id.station2, R.id.station4 };
    private static final int [] navst4 = { R.id.btnExit, R.id.station12, R.id.station3, R.id.station5 };
    private static final int [] navst5 = { R.id.btnExit, R.id.station13, R.id.station4, R.id.station6 };
    private static final int [] navst6 = { R.id.btnExit, R.id.station14, R.id.station5, R.id.station7 };
    private static final int [] navst7 = { R.id.btnExit, R.id.station15, R.id.station6, R.id.station8 };
    private static final int [] navst8 = { R.id.btnExit, R.id.station16, R.id.station7, R.id.station1 };
    private static final int [] navst9 = { R.id.station1, R.id.btnExit, R.id.station16, R.id.station10 };
    private static final int [] navst10 = { R.id.station2, R.id.btnExit, R.id.station9, R.id.station11 };
    private static final int [] navst11 = { R.id.station3, R.id.btnExit, R.id.station10, R.id.station12 };
    private static final int [] navst12 = { R.id.station4, R.id.btnExit, R.id.station11, R.id.station13 };
    private static final int [] navst13 = { R.id.station5, R.id.btnExit, R.id.station12, R.id.station14 };
    private static final int [] navst14 = { R.id.station6, R.id.btnExit, R.id.station13, R.id.station15 };
    private static final int [] navst15 = { R.id.station7, R.id.btnExit, R.id.station14, R.id.station16 };
    private static final int [] navst16 = { R.id.station8, R.id.btnExit, R.id.station15, R.id.station9 };
    private static final int [] navExit = { R.id.station1, R.id.station8, R.id.station1, R.id.station8 };

    private Map<Integer, int[]> navFtpMap;
    private int navFtpFocus = R.id.btnCancel;
    private static final int [] navPrevFtp = { R.id.valNameLast, R.id.btnUp, R.id.btnCancel, R.id.btnNxtSvr};
    private static final int [] navNextFtp = { R.id.valNameLast, R.id.btnUp, R.id.btnPrevSvr, R.id.btnCancel};
    private static final int [] navUp = { R.id.btnPrevSvr, R.id.valName1, R.id.btnCancel, R.id.valName1};
    private static final int [] navDown = { R.id.valNameLast, R.id.btnPrevSvr, R.id.btnCancel, R.id.valNameLast };
    private static final int [] navCancel = { R.id.btnExit2, R.id.valNameLast, R.id.btnNxtSvr, R.id.btnPrevSvr };
    private static final int [] navExit2 = { R.id.btnNxtSvr, R.id.btnCancel, R.id.btnNxtSvr, R.id.btnPrevSvr};
    private static final int [] navFtp1 = { R.id.btnUp, R.id.valName2, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp2 = { R.id.valName1, R.id.valName3, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp3 = { R.id.valName2, R.id.valName4, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp4 = { R.id.valName3, R.id.valName5, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp5 = { R.id.valName4, R.id.valName6, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp6 = { R.id.valName5, R.id.valName7, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp7 = { R.id.valName6, R.id.valName8, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp8 = { R.id.valName7, R.id.valName9, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp9 = { R.id.valName8, R.id.valName10, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp10 = { R.id.valName9, R.id.valName11, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp11 = { R.id.valName10, R.id.valName12, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp12 = { R.id.valName11, R.id.valName13, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp13 = { R.id.valName12, R.id.valName14, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp14 = { R.id.valName13, R.id.valName15, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp15 = { R.id.valName14, R.id.valNameLast, R.id.btnDown, R.id.btnCancel };
    private static final int [] navFtp16 = { R.id.valName15, R.id.btnDown, R.id.btnDown, R.id.btnCancel };





    ExoPlayer mPlayer;
    public final static String INTENT_LOAD_M3U8 = "loadm3u8";
    public final static String XTRA_STATION_ID = "xtraStationId";
    public final static String XTRA_URL_AUDIO = "urlAudio";
    public final static String XTRA_URL_VIDEO = "urlVideo";
    public final static String XTRA_STATUS = "status";
    public final static String XTRA_OK = "ok";
    public final static String XTRA_FAIL = "fail";
    public final static String XTRA_MESSAGE = "message";
    private M3U8Receiver m3u8Reveiver;
    public final static String INTENT_ENV = "envData";
    public final static String XTRA_SUNRISE = "sunrise";
    public final static String XTRA_SUNSET = "sunset";
    public static final String XTRA_TEMPTODAY = "tempToday";
    public static final String XTRA_HUMIDITYTODAY = "humidityToday";
    public static final String XTRA_ICONTODAY = "iconToday";
    public static final String XTRA_TEMPTOMORROW = "tempTomorrow";
    public static final String XTRA_ICONTOMORROW = "iconTomorrow";
    public static final String INTENT_CFG_UPDATE ="configUpdate";
    public final static String INTENT_ENIGMA2 = "enigma2";
    public final static String INTENT_RCV_FILES = "ftpFilesReceived";

    private EnvDataReceiver envDataReceiver;
    private ConfigUpdateReceiver cfgUpdateReceiver;
    private Enigma2Receiver enigma2Receiver;
    private FtpReceiver ftpReceiver;

    private AppContext appContext;
    private Station curStation;

    /**
     * Some older devices need a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private View mContentView;

    private int curViewMode;
    private boolean hideWhenReady;
    private int curFtpServerIndex;
    private ActivityFullscreenBinding binding;
    private ConfigWebServer cWS;
    int [] arrResIds;
    int [] arrFtpResIds;
    int [] arrFtpExtraResIds;
    HashMap<String, String> ftpFiles;
    int ftpTopIndex;
    String ftpUrl;

    Intent createFileIntent;
    ActivityResultLauncher<Intent> createFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m3u8Reveiver = new M3U8Receiver();
        envDataReceiver = new EnvDataReceiver();
        cfgUpdateReceiver = new ConfigUpdateReceiver();
        enigma2Receiver = new Enigma2Receiver();
        ftpReceiver = new FtpReceiver();
        appContext = new AppContext();

        navMap = new HashMap<>();
        arrResIds = new int[]{R.id.station1, R.id.station2, R.id.station3, R.id.station4,
                R.id.station5, R.id.station6, R.id.station7, R.id.station8, R.id.station9,
                R.id.station10, R.id.station11, R.id.station12, R.id.station13, R.id.station14,
                R.id.station15, R.id.station16
        };
        int[][] tmpNav = {navst1, navst2, navst3, navst4, navst5, navst6, navst7,
                navst8, navst9, navst10, navst11, navst12, navst13, navst14, navst15, navst16};
        for (int i = 0; i < arrResIds.length; i++) {
            navMap.put(arrResIds[i], tmpNav[i]);
        }
        navMap.put(R.id.btnExit, navExit);
        navFtpMap = new HashMap<>();
        arrFtpResIds = new int[]{R.id.valName1, R.id.valName2, R.id.valName3, R.id.valName4,
                R.id.valName5, R.id.valName6, R.id.valName7, R.id.valName8, R.id.valName9, R.id.valName10,
                R.id.valName11, R.id.valName12, R.id.valName13, R.id.valName14,
                R.id.valName15,
                R.id.valNameLast, R.id.btnPrevSvr, R.id.btnNxtSvr, R.id.btnUp, R.id.btnDown,
                R.id.btnCancel, R.id.btnExit2
        };
        arrFtpExtraResIds = new int[]{R.id.lblName, R.id.lblSize, R.id.valSize1, R.id.valSize2,
                R.id.valSize3, R.id.valSize4, R.id.valSize5, R.id.valSize6,
                R.id.valSize7, R.id.valSize8, R.id.valSize9, R.id.valSize10,
                R.id.valSize11, R.id.valSize12,
                R.id.valSize13, R.id.valSize14, R.id.valSize15,
                R.id.valSizeLast, R.id.lblStatus,
                R.id.lblFtp, R.id.lblFtpServer, R.id.lineHoriz, R.id.lineHoriz2
        };
        int[][] tmpFtpNav = {navFtp1, navFtp2, navFtp3, navFtp4, navFtp5, navFtp6, navFtp7,
                navFtp8, navFtp9, navFtp10, navFtp11, navFtp12, navFtp13, navFtp14, navFtp15, navFtp16,
                navPrevFtp, navNextFtp, navUp, navDown, navCancel,
                navExit2};
        for (int i = 0; i < arrFtpResIds.length; i++) {
            navFtpMap.put(arrFtpResIds[i], tmpFtpNav[i]);
        }

        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (appContext.configuration.configExists(sPrefs)) {
            appContext.configuration.load(sPrefs);
        } else {
            loadDefaults();
            appContext.configuration.save(sPrefs);
        }


        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().setKeepScreenOn(true);
        binding.getRoot().setOnClickListener(this);

        updateStationTitles(arrResIds);
        /*
            Calculate the layout of the screen when buttons are visible
         */
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels; // / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels; // / displayMetrics.density;

        int videoWidth = (int) Math.round(dpWidth * 0.85);
        int videoHeight = videoWidth / 16 * 9;
        int widgetWidth = (int) Math.round(dpWidth) - videoWidth;
        int stationWidth = (int) Math.abs(dpWidth / 8);
        int stationHeight = (int) Math.abs((dpHeight - videoHeight) * 0.6);
        int controlsHeight = (int) Math.abs(dpHeight - videoHeight - stationHeight);
        binding.fullScreenVideo.getLayoutParams().width = videoWidth;
        binding.fullScreenVideo.getLayoutParams().height = videoHeight;
        binding.btnExit.getLayoutParams().width = stationWidth;
        binding.btnExit.getLayoutParams().height = controlsHeight;

        for (int viewId : arrResIds) {
            View view = findViewById(viewId);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (viewId != R.id.station8 && viewId != R.id.station16) {
                lp.width = stationWidth;
            } else {
                lp.width = (int) Math.abs(dpWidth) - (7 * stationWidth);
            }
            lp.height = stationHeight / 2;
        }
        binding.statusbar.getLayoutParams().height = controlsHeight;
        binding.statusbar.getLayoutParams().width = stationWidth * 7;

        binding.imgWeatherToday.getLayoutParams().width = widgetWidth;
        binding.txtWeatherToday.getLayoutParams().width = widgetWidth;
        binding.imgWeatherTomorrow.getLayoutParams().width = widgetWidth;
        binding.txtWeatherTomorrow.getLayoutParams().width = widgetWidth;
        int tmp = widgetWidth / 2;
        binding.imgSunrise.getLayoutParams().width = tmp;
        binding.txtSunrise.getLayoutParams().width = tmp;
        binding.imgSunset.getLayoutParams().width = widgetWidth - tmp;
        binding.txtSunset.getLayoutParams().width = widgetWidth - tmp;
        tmp = videoHeight / 30;
        binding.imgWeatherToday.getLayoutParams().height = 10 * tmp;
        binding.txtWeatherToday.getLayoutParams().height = 2 * tmp;
        binding.imgWeatherTomorrow.getLayoutParams().height = 10 * tmp;
        binding.txtWeatherTomorrow.getLayoutParams().height = 2 * tmp;
        binding.imgSunset.getLayoutParams().height = 2 * tmp;
        binding.txtSunset.getLayoutParams().height = tmp + 10;
        binding.imgSunrise.getLayoutParams().height = 2 * tmp;
        binding.txtSunrise.getLayoutParams().height = tmp + 10;

        updateStationTitles(arrResIds);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int levels = 100;
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), levels);
        int ip = wifi.getConnectionInfo().getIpAddress();
        String ipStrg = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
        binding.statusbar.setText(String.format("Press 'OK' to start streaming [Wifi Strength is %d %%, IP %s]", level, ipStrg));

        /*
            Calculate the file browser view elements
         */

        int w85 = (int) Math.round(dpWidth * 0.85);
        int wCol1 = 50;
        int wCol2 = (int) Math.round(dpWidth * 0.65);
        int wCol3 = w85 - wCol2 - wCol1 - 30;
        binding.lblFtp.getLayoutParams().width = w85;
        binding.lineHoriz.getLayoutParams().width = w85 - wCol1;
        binding.lblFtpServer.getLayoutParams().width = wCol2 + wCol3 - wCol1 - wCol1;
        binding.lineHoriz2.getLayoutParams().width = w85;
        String tagFilename = getResources().getString(R.string.tag_filename);
        String tagFilesize = getResources().getString(R.string.tag_filesize);
        for (int idx = 0; idx < binding.fullscreenContainer.getChildCount(); idx++) {
            View vw = binding.fullscreenContainer.getChildAt(idx);
            if(vw.getTag()!=null) {
                String tag = (String)vw.getTag();
                if(tag.equals(tagFilename)) {
                    ((ViewGroup.LayoutParams)vw.getLayoutParams()).width = wCol2;
                }
                if(tag.equals(tagFilesize)) {
                    ((ViewGroup.LayoutParams)vw.getLayoutParams()).width = wCol3;
                }
            }
        }


        binding.lblStatus.getLayoutParams().width = w85 - wCol1;

        binding.btnExit2.getLayoutParams().width = (int) Math.round(dpWidth * 0.15);
        binding.btnCancel.getLayoutParams().width = (int) Math.round(dpWidth * 0.15);

        /*
            Highlight the first button as starting point
         */
        binding.station1.setTypeface(binding.station1.getTypeface(), Typeface.BOLD);
        binding.station1.setBackgroundColor(getResources().getColor(R.color.teal_700, getTheme()));

        changeMode(MODE_VIEW_BUTTONS);
        curFtpServerIndex = 1;

        hideWhenReady = false;
        mContentView = binding.fullScreenVideo;

        cWS = new ConfigWebServer(this, appContext);
        cWS.start();
    }


    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;


    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


            }
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            // Hide UI first
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            // Schedule a runnable to remove the status and navigation bar after a delay
            mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

        }
    };
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        mHideHandler.postDelayed(mHideRunnable, 100);
    }


    private void updateStationTitles(int [] arrResIds) {
        appContext.stationMap = new HashMap<>();
        int id;
        for (Station s : appContext.configuration.stationMap.values()) {
            if(s.slot>0 && s.slot<17) {
                id = arrResIds[s.slot - 1];
                appContext.stationMap.put(id, s);
                Button btn = findViewById(id);
                btn.setText(s.text);
            }
        }
    }

    private void changeMode(int newMode) {
        boolean showFiles, showButtons, fullScreenVideo, smallVideo, miniVideo;

        showButtons = false;
        showFiles = false;
        fullScreenVideo = false;
        smallVideo = false;
        miniVideo = false;
        if(newMode == MODE_VIEW_ONLY) {
            fullScreenVideo = true;
        }
        else if(newMode == MODE_VIEW_BUTTONS) {
            showButtons = true;
            smallVideo = true;
        }
        else if(newMode == MODE_FILES) {
            showFiles = true;
            miniVideo = true;
        }

        /* hide everything, before selectively show what is requested */
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        /* Buttons */
        for(int viewId : arrResIds) {
            View view = findViewById(viewId);
            view.setVisibility(View.GONE);
        }
        binding.btnExit.setVisibility(View.GONE);
        binding.statusbar.setVisibility(View.GONE);
        binding.imgWeatherToday.setVisibility(View.GONE);
        binding.imgWeatherTomorrow.setVisibility(View.GONE);
        binding.txtWeatherToday.setVisibility(View.GONE);
        binding.txtWeatherTomorrow.setVisibility(View.GONE);
        binding.imgSunrise.setVisibility(View.GONE);
        binding.imgSunset.setVisibility(View.GONE);
        binding.txtSunrise.setVisibility(View.GONE);
        binding.txtSunset.setVisibility(View.GONE);
        binding.lineHoriz.setVisibility(View.GONE);
        for(int viewId : arrFtpResIds) {
            View vw = findViewById(viewId);
            vw.setVisibility(View.GONE);
        }
        for(int viewId : arrFtpExtraResIds) {
            View vw = findViewById(viewId);
            vw.setVisibility(View.GONE);
        }

        if(showButtons) {
            /* Buttons */
            for(int viewId : arrResIds) {
                View view = findViewById(viewId);
                view.setVisibility(View.VISIBLE);
            }
            binding.btnExit.setVisibility(View.VISIBLE);
            binding.statusbar.setVisibility(View.VISIBLE);
            binding.imgWeatherToday.setVisibility(View.VISIBLE);
            binding.imgWeatherTomorrow.setVisibility(View.VISIBLE);
            binding.txtWeatherToday.setVisibility(View.VISIBLE);
            binding.txtWeatherTomorrow.setVisibility(View.VISIBLE);
            binding.imgSunrise.setVisibility(View.VISIBLE);
            binding.imgSunset.setVisibility(View.VISIBLE);
            binding.txtSunrise.setVisibility(View.VISIBLE);
            binding.txtSunset.setVisibility(View.VISIBLE);
            /* update weather info */
            EnvHandler envHandler = new EnvHandler(this);
            envHandler.start();
        }

        if(showFiles) {
            for(int viewId : arrFtpResIds) {
                View vw = findViewById(viewId);
                vw.setVisibility(View.VISIBLE);
            }
            for(int viewId : arrFtpExtraResIds) {
                View vw = findViewById(viewId);
                vw.setVisibility(View.VISIBLE);
            }
            binding.lineHoriz.setVisibility(View.VISIBLE);
            toggleFtpNav(navFtpFocus);
        }

        if(fullScreenVideo) {
            ViewGroup.LayoutParams params = binding.fullScreenVideo.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.fullScreenVideo.setLayoutParams(params);
            binding.fullScreenVideo.setVisibility(View.VISIBLE);
            ((RelativeLayout.LayoutParams)binding.fullScreenVideo.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_END);
            ((RelativeLayout.LayoutParams)binding.fullScreenVideo.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        }

        if(smallVideo) {
            int videoWidth = (int)Math.round(displayMetrics.widthPixels * 0.85);
            binding.fullScreenVideo.getLayoutParams().height = videoWidth / 16 * 9;
            binding.fullScreenVideo.getLayoutParams().width = videoWidth;
            ((RelativeLayout.LayoutParams)binding.fullScreenVideo.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_END);
            ((RelativeLayout.LayoutParams)binding.fullScreenVideo.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        }

        if(miniVideo) {
            int videoWidth =(int)Math.round(displayMetrics.widthPixels * 0.15);
            binding.fullScreenVideo.getLayoutParams().height = videoWidth / 16 * 9;
            binding.fullScreenVideo.getLayoutParams().width = videoWidth;
            ((RelativeLayout.LayoutParams)binding.fullScreenVideo.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_START);
            ((RelativeLayout.LayoutParams)binding.fullScreenVideo.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        }
        curViewMode = newMode;
    }


    private void toggleNav(int id) {
        Button btn = mContentView.getRootView().findViewById(navFocus);
        btn.setTypeface(btn.getTypeface(), Typeface.NORMAL);
        btn.setBackgroundColor(getResources().getColor(R.color.dark_gray, getTheme()));
        navFocus = id;
        btn = mContentView.getRootView().findViewById(navFocus);
        btn.setTypeface(btn.getTypeface(), Typeface.BOLD);
        btn.setBackgroundColor(getResources().getColor(R.color.teal_700, getTheme()));
    }

    private void toggleFtpNav(int id) {
        Button btn = mContentView.getRootView().findViewById(navFtpFocus);
        btn.setTypeface(btn.getTypeface(), Typeface.NORMAL);
        btn.setBackgroundColor(getResources().getColor(R.color.dark_gray, getTheme()));
        navFtpFocus = id;
        btn = mContentView.getRootView().findViewById(navFtpFocus);
        btn.setTypeface(btn.getTypeface(), Typeface.BOLD);
        btn.setBackgroundColor(getResources().getColor(R.color.teal_700, getTheme()));
    }


    @Override
    public void onClick(View view) {
        int viewId=view.getId();

        /* some devices handle the remote control not as keyboard input but convert this directly into clicks */
        if(viewId == R.id.fullscreen_container) {
            if(curViewMode==MODE_VIEW_BUTTONS) {
                viewId = navFocus;
                view = findViewById(viewId);
            }
            else if (curViewMode==MODE_FILES) {
                viewId = navFtpFocus;
                view = findViewById(viewId);
            }
            else {
                changeMode(MODE_VIEW_BUTTONS);
                return;
            }
        }
        /* adjust visible focus in case the device has a touch screen */
        if((curViewMode == MODE_FILES) && (viewId != navFtpFocus)) {
            toggleFtpNav(viewId);
        }
        if((curViewMode == MODE_VIEW_BUTTONS) && (viewId != navFocus)) {
            toggleNav(viewId);
        }


        if(viewId == R.id.btnCancel) {
            changeMode(MODE_VIEW_BUTTONS);
        }
        if(viewId==R.id.btnExit || viewId == R.id.btnExit2) {
            if(mPlayer!=null) {
                mPlayer.stop();
            }
            finish();
        }
        if(viewId==R.id.station16) {
            if(mPlayer!=null) {
                mPlayer.stop();
            }
            /* FTP */
            if(appContext.configuration.ftpServerList.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("FTP Server")
                        .setMessage("No FTP Server configured.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                return;
            }
            else {
                changeMode(MODE_FILES);
                FTPConfiguration ftpConfiguration = appContext.configuration.getNextFtpServer(0);
                if(ftpConfiguration!= null) {
                    curFtpServerIndex = ftpConfiguration.index;
                    FtpHandler ftpHandler = new FtpHandler(this, appContext, ftpConfiguration);
                    ftpHandler.start();
                    binding.lblFtpServer.setText(ftpConfiguration.name);
                }
            }
        }
        if(view.getTag()!=null) {
            String tag = (String)view.getTag();
            if(tag.equals(getResources().getString(R.string.tag_filename))) {
                Button btn = (Button)view;
                if(btn.getText().length()>0) {
                    if(btn.getCurrentTextColor() == getResources().getColor(R.color.yellow, getTheme())) {
                        FTPConfiguration ftpConfiguration = appContext.configuration.getCurrentFtpServer(curFtpServerIndex);
                        ftpConfiguration.changeDir(btn.getText().toString());
                        FtpHandler ftpHandler = new FtpHandler(this, appContext, ftpConfiguration);
                        ftpHandler.start();
                    }
                    else {
                        Station s = new Station();
                        s.url = ftpUrl + ((Button) view).getText();
                        s.mediaType = Station.MTYPE_HLS;
                        s.text = (String) ((Button) view).getText();
                        if (mPlayer != null) {
                            mPlayer.stop();
                        }
                        hideWhenReady = true;
                        //changeMode(MODE_VIEW_BUTTONS);
                        startPlayer(s, false, null, null);
                    }
                }
            }
        }
        switch(viewId) {
            case R.id.station1:
            case R.id.station2:
            case R.id.station3:
            case R.id.station4:
            case R.id.station5:
            case R.id.station6:
            case R.id.station7:
            case R.id.station8:
            case R.id.station9:
            case R.id.station10:
            case R.id.station11:
            case R.id.station12:
            case R.id.station13:
            case R.id.station14:
            case R.id.station15:

                Station s = appContext.stationMap.get(viewId);
                switchStation(s);
                break;
        }

        switch(viewId) {
            case R.id.valName1:
            case R.id.valName2:
            case R.id.valName3:
            case R.id.valName4:
            case R.id.valName5:
            case R.id.valName6:
            case R.id.valName7:
            case R.id.valName8:
            case R.id.valName9:
            case R.id.valName10:
            case R.id.valName11:
            case R.id.valName12:
            case R.id.valName13:
            case R.id.valName14:
            case R.id.valName15:
            case R.id.valNameLast:
                /* start streaming via ftp */
                break;
            case R.id.btnNxtSvr: {
                    FTPConfiguration ftpConfiguration = appContext.configuration.getNextFtpServer(curFtpServerIndex);
                    if(ftpConfiguration!= null) {
                        curFtpServerIndex = ftpConfiguration.index;
                        FtpHandler ftpHandler = new FtpHandler(this, appContext, ftpConfiguration);
                        ftpHandler.start();
                        binding.lblFtpServer.setText(ftpConfiguration.name);
                    }
                }
                break;
            case R.id.btnPrevSvr: {
                    FTPConfiguration ftpConfiguration = appContext.configuration.getPrevFtpServer(curFtpServerIndex);
                    if (ftpConfiguration != null) {
                        curFtpServerIndex = ftpConfiguration.index;
                        FtpHandler ftpHandler = new FtpHandler(this, appContext, ftpConfiguration);
                        ftpHandler.start();
                        binding.lblFtpServer.setText(ftpConfiguration.name);
                    }
                }
                break;
            case R.id.btnUp:
                if(ftpTopIndex>1) {
                    showFtpFiles(ftpTopIndex-1);
                }
                break;
            case R.id.btnDown:
                if(ftpTopIndex<ftpFiles.keySet().size()-10) {
                    showFtpFiles(ftpTopIndex+1);
                }
                break;

        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (curViewMode == MODE_VIEW_ONLY) {
                    changeMode(MODE_VIEW_BUTTONS);
                    return true;
                }
                else {
                    View vw = binding.getRoot().findViewById(curViewMode == MODE_VIEW_BUTTONS? navFocus : navFtpFocus);
                    if (vw != null) {
                        onClick(vw);
                        return true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if(curViewMode == MODE_VIEW_ONLY) {
                    Enigma2Handler e2Handler = new Enigma2Handler(this, appContext, curStation);
                    e2Handler.start();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if(curViewMode == MODE_VIEW_BUTTONS) {
                    toggleNav(Objects.requireNonNull(navMap.get(navFocus))[0]);
                }
                if(curViewMode == MODE_FILES) {
                    toggleFtpNav(Objects.requireNonNull(navFtpMap.get(navFtpFocus))[0]);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(curViewMode == MODE_VIEW_BUTTONS) {
                    toggleNav(Objects.requireNonNull(navMap.get(navFocus))[1]);
                }
                if(curViewMode == MODE_FILES) {
                    toggleFtpNav(Objects.requireNonNull(navFtpMap.get(navFtpFocus))[1]);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(curViewMode == MODE_VIEW_BUTTONS) {
                    toggleNav(Objects.requireNonNull(navMap.get(navFocus))[2]);
                }
                if(curViewMode == MODE_FILES) {
                    toggleFtpNav(Objects.requireNonNull(navFtpMap.get(navFtpFocus))[2]);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(curViewMode == MODE_VIEW_BUTTONS) {
                    toggleNav(Objects.requireNonNull(navMap.get(navFocus))[3]);
                }
                if(curViewMode == MODE_FILES) {
                    toggleFtpNav(Objects.requireNonNull(navFtpMap.get(navFtpFocus))[3]);
                }
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void switchStation(Station s) {

            if(mPlayer!=null) {
                mPlayer.stop();
            }
            hideWhenReady = true;

            startPlayer(s,  false, null, null);
    }



    private void startPlayer(Station s, boolean resolvedm3u8, String urlAudio, String resolvedUrlVideo) {
        if (!resolvedm3u8 && (s.url.endsWith(".m3u8") || s.url.contains("m3u"))) {
            M3U8Handler m3u8hdl = new M3U8Handler(this, s);
            m3u8hdl.start();
            return;
        }
        curStation = s;
        String urlVideo;
        if(resolvedUrlVideo!=null) {
            urlVideo = resolvedUrlVideo;
        } else {
            urlVideo = s.url;
        }
        if (mPlayer == null) {
            mPlayer = new ExoPlayer.Builder(this).build();
            binding.fullScreenVideo.setPlayer(mPlayer);
            mPlayer.addListener(new Player.Listener() {

                @Override
                public void onPlaybackStateChanged(int playbackState) {

                    switch (playbackState) {
                        case ExoPlayer.STATE_BUFFERING:
                            if(curViewMode == MODE_FILES) {
                                binding.lblStatus.setText(String.format("Buffering %s ...", curStation.text));
                            }
                            else {
                                binding.statusbar.setText(String.format("Buffering %s ...", curStation.text));
                            }
                            break;
                        case ExoPlayer.STATE_ENDED:
                            //do what you want
                            break;
                        case ExoPlayer.STATE_READY:
                            binding.statusbar.setText(String.format("Buffering %s ...", curStation.text));
                            if ((curViewMode == MODE_VIEW_BUTTONS || curViewMode == MODE_FILES) && hideWhenReady) {
                                hideWhenReady = false;
                                changeMode(MODE_VIEW_ONLY);
                            }
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    Throwable cause = error.getCause();
                    TextView tv = findViewById(R.id.statusbar);
                    if (cause instanceof UnrecognizedInputFormatException) {
                        tv.setText("Input format could not be handled by the player (maybe wrong media type?)");
                    } else {
                        tv.setText(cause.getMessage());
                    }
                }
            });

        }

        String userAgent = "userAgent = \"exoplayer-codelab\"";

        ExDataSource.Factory ftpDataSourceFactory = new ExDataSource.Factory(this);
        DefaultHttpDataSource.Factory dataSourceFactory = new
                DefaultHttpDataSource.Factory();
        DefaultDataSource.Factory defaultDataSourceFactory = new DefaultDataSource.Factory(this);
        Map<String, String> mp = new HashMap<>();
        if (s.user!=null && s.user.length()>0) {
            // encrypt Authdata
            byte[] toEncrypt = (s.user + ":" + s.password).getBytes();
            String encoded = Base64.encodeToString(toEncrypt, Base64.DEFAULT);
            mp.put("Authorization", "Basic " + encoded);
        }
        mp.put("User-Agent", userAgent);
        dataSourceFactory.setDefaultRequestProperties(mp);
        MediaSource videoSource = null;
        MediaSource audioSource = null;
        if (s.mediaType == Station.MTYPE_HLS) {
            if(urlVideo.startsWith("ftp")) {
                videoSource = new ProgressiveMediaSource.Factory(ftpDataSourceFactory).createMediaSource(MediaItem.fromUri(urlVideo));
            }
            else if (!urlVideo.endsWith("m3u8")) {

                videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(urlVideo));
                if (urlAudio != null && urlAudio.length() > 0) {
                    audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(urlAudio));
                }
            } else {
                videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(urlVideo));
                if (urlAudio != null && urlAudio.length() > 0) {
                    audioSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(urlAudio));
                }
            }

            Log.i("PLAY HLS: ", urlVideo);
            if (audioSource != null) {
                MergingMediaSource mmS = new MergingMediaSource(videoSource, audioSource);
                mPlayer.setMediaSource(mmS);
            } else {
                mPlayer.setMediaSource(videoSource);
            }
        }
        if (s.mediaType == Station.MTYPE_RTSP) {
            Log.i("PLAY RTSP: ", urlVideo);
            mPlayer.setMediaItem(MediaItem.fromUri(urlVideo));
        }
        if(s.mediaType == Station.MTYPE_FTP) {
            Log.i("PLAYER",String.format(  "PLAY from FTP: %s", s.url));
            DataSpec ds = new DataSpec( Uri.parse(s.url));

        }
        mPlayer.prepare();
        mPlayer.setPlayWhenReady(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(m3u8Reveiver, new IntentFilter(INTENT_LOAD_M3U8));
        registerReceiver(envDataReceiver, new IntentFilter(INTENT_ENV));
        registerReceiver(cfgUpdateReceiver, new IntentFilter(INTENT_CFG_UPDATE));
        registerReceiver(enigma2Receiver, new IntentFilter(INTENT_ENIGMA2));
        registerReceiver(ftpReceiver, new IntentFilter(INTENT_RCV_FILES));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(m3u8Reveiver);
        unregisterReceiver(envDataReceiver);
        unregisterReceiver(cfgUpdateReceiver);
        unregisterReceiver(enigma2Receiver);
        unregisterReceiver(ftpReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mPlayer!=null) {
            mPlayer.stop();
            mPlayer.release();
            changeMode(MODE_VIEW_BUTTONS);
            mPlayer = null;
        }
        super.onPause();
    }

    private void showFtpFiles(int newTopIndex) {
        ftpTopIndex = newTopIndex;
        int [] idName = { R.id.valName1, R.id.valName2, R.id.valName3, R.id.valName4,
                R.id.valName5, R.id.valName6, R.id.valName7, R.id.valName8,
                R.id.valName9, R.id.valName10, R.id.valName11, R.id.valName12, R.id.valName13,
                R.id.valName14,
                R.id.valName15,
                R.id.valNameLast };
        int [] idSize = { R.id.valSize1, R.id.valSize2, R.id.valSize3, R.id.valSize4,
                R.id.valSize5, R.id.valSize6, R.id.valSize7, R.id.valSize8,
                R.id.valSize9, R.id.valSize10, R.id.valSize11, R.id.valSize12, R.id.valSize13,
                R.id.valSize14,
                R.id.valSize15,
                R.id.valSizeLast };

        TextView tv;
        Button btn;
        for(int idx=0; idx<16; idx++) {
            btn = findViewById(idName[idx]);
            tv = findViewById(idSize[idx]);
            String tag = String.format("%04d-", idx+ftpTopIndex);
            if(ftpFiles.containsKey(tag + "name")) {
                if (ftpFiles.get(tag + "isfile").equals("true")) {
                    btn.setText(ftpFiles.get(tag + "name"));
                    tv.setText(ftpFiles.get(tag + "size"));
                    btn.setTextColor(getResources().getColor(R.color.white, getTheme()));
                }
                else {
                    btn.setText("[" + ftpFiles.get(tag + "name") + "]");
                    tv.setText("");
                    btn.setTextColor(getResources().getColor(R.color.yellow, getTheme()));
                }
            }
            else {
                btn.setText("");
                tv.setText("");
            }
        }
    }

    private void loadDefaults() {
        Station s = new Station();
        s.slot = 1;
        s.text = "ARD";
        s.url = "https://mcdn.daserste.de/daserste/de/master.m3u8";
        s.mediaType = Station.MTYPE_HLS;
        appContext.configuration.stationMap.put(1, s);
        s = new Station();
        s.slot = 2;
        s.text = "ZDF";
        s.url="http://zdf-hls-15.akamaized.net/hls/live/2016498/de/high/master.m3u8";
        s.mediaType = Station.MTYPE_HLS;
        appContext.configuration.stationMap.put(2, s);
    }

    public class M3U8Receiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (INTENT_LOAD_M3U8.equals(intent.getAction())) {
                if (intent.getStringExtra(XTRA_STATUS).equals(XTRA_OK)) {
                    int internalId = intent.getIntExtra(XTRA_STATION_ID, 0);
                    Station station = null;
                    for(Map.Entry<Integer, Station> pair : appContext.stationMap.entrySet()) {
                        station = pair.getValue();
                        if (internalId == station.slot) {
                            break;
                        }
                    }
                    if (station!=null) {
                        String urlAudio = intent.getStringExtra(XTRA_URL_AUDIO);
                        String urlVideo = intent.getStringExtra(XTRA_URL_VIDEO);
                        startPlayer(station, true, urlAudio, urlVideo);
                    }
                }
                else {
                    TextView tv = findViewById(R.id.statusbar);
                    tv.setText(intent.getStringExtra(XTRA_MESSAGE));
                }
            }
        }
    }

    public class FtpReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(INTENT_RCV_FILES.equals(intent.getAction())) {
                if(intent.getBooleanExtra("status", false)) {
                    ftpFiles = new HashMap<>();
                    ftpTopIndex = 1;
                    int ftpCount = intent.getIntExtra("count", 0);
                    ftpUrl = intent.getStringExtra("url");
                    for(int idx=0; idx<ftpCount; idx++) {
                        String tag = String.format("%04d-", idx+1);
                        ftpFiles.put(tag + "name", intent.getStringExtra(tag + "name"));
                        ftpFiles.put(tag + "size", intent.getStringExtra(tag + "size"));
                        ftpFiles.put(tag + "isfile", String.valueOf(intent.getBooleanExtra(tag + "isFile", true)));
                    }
                    showFtpFiles(1);
                    binding.lblName.setText(String.format("Name [%s]", intent.getStringExtra("subdir")));
                }
                else {
                    TextView tv = findViewById(R.id.statusbar);
                    tv.setText(intent.getStringExtra("message"));
                }
            }
        }
    }

    public class ConfigUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(INTENT_CFG_UPDATE.equals(intent.getAction())) {
                appContext.configuration.save(PreferenceManager.getDefaultSharedPreferences(context));
                updateStationTitles(arrResIds);
            }
        }

    }

    public class EnvDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(INTENT_ENV.equals(intent.getAction())) {
                if(intent.hasExtra(XTRA_SUNSET)) {
                    String sunrise = intent.getStringExtra(XTRA_SUNRISE);
                    String sunset = intent.getStringExtra(XTRA_SUNSET);
                    TextView tv = findViewById(R.id.txtSunrise);
                    tv.setText(sunrise);
                    tv = findViewById(R.id.txtSunset);
                    tv.setText(sunset);
                }
                if(intent.hasExtra(XTRA_TEMPTODAY)) {
                    String tempToday = intent.getStringExtra(XTRA_TEMPTODAY);
                    String humidityToday = intent.getStringExtra(XTRA_HUMIDITYTODAY);
                    TextView tv = findViewById(R.id.txtWeatherToday);
                    tv.setText("Today " + tempToday +" \u2103 " + humidityToday + " %");
                    ImageView iv = findViewById(R.id.imgWeatherToday);
                    iv.setImageResource(mapIcon(intent.getStringExtra(XTRA_ICONTODAY)));
                }
                if(intent.hasExtra(XTRA_TEMPTOMORROW)) {
                    String tempTomorrow = intent.getStringExtra(XTRA_TEMPTOMORROW);
                    TextView tv = findViewById(R.id.txtWeatherTomorrow);
                    tv.setText("Tomorrow " + tempTomorrow +" \u2103 ");
                    ImageView iv = findViewById(R.id.imgWeatherTomorrow);
                    iv.setImageResource(mapIcon(intent.getStringExtra(XTRA_ICONTOMORROW)));
                }
            }
        }


        private int mapIcon(String name) {
            if(name.equals("clear-day")) {
                return R.drawable.clearday;
            }
            if(name.equals("clear-night")) {
                return R.drawable.clearnight;
            }
            if(name.equals("partly-cloudy-day")) {
                return R.drawable.partlycloudyday;
            }
            if(name.equals("partly-cloudy-night")) {
                return R.drawable.partlycloudynight;
            }
            if(name.equals("cloudy")) {
                return R.drawable.cloudy;
            }
            if(name.equals("fog")) {
                return R.drawable.fog;
            }
            if(name.equals("wind")) {
                return R.drawable.wind;
            }
            if(name.equals("rain")) {
                return R.drawable.rain;
            }
            if(name.equals("sleet")) {
                return R.drawable.sleet;
            }
            if(name.equals("snow")) {
                return R.drawable.snow;
            }
            if(name.equals("hail")) {
                return R.drawable.hail;
            }
            if(name.equals("thunderstorm")) {
                return R.drawable.thunderstorm;
            }
            return R.drawable.broken;
        }

    }

    public class Enigma2Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(INTENT_ENIGMA2.equals(intent.getAction())) {
                String title = intent.getStringExtra("now:title");
                String start = intent.getStringExtra("now:start");
                int unixtime = Integer.parseInt(intent.getStringExtra("now:unixtime"));
                int duration = Integer.parseInt(intent.getStringExtra("now:duration"));
                View view = LayoutInflater.from(FullscreenActivity.this)
                        .inflate(R.layout.prg_info, null);
                TextView tv1 = view.findViewById(R.id.title);
                tv1.setText(title);
                TextView tv2 = view.findViewById(R.id.startTime);
                tv2.setText(start);
                Date now = new Date();
                int nowTime = (int)(now.getTime() / 1000);
                int elapsed = nowTime - unixtime;
                ProgressBar p = view.findViewById(R.id.progressBar);
                p.setMax(100);
                p.setProgress((int)(elapsed * 100 / duration));
                view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.getLayoutParams().width = view.getMeasuredWidth();

                Toast toast = new Toast(FullscreenActivity.this);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
            }
        }

    }
}