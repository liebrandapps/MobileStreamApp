package io.liebrand.multistreamapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class AboutDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity a;
    public Dialog d;
    public Button ok;

    public AboutDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.a = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DisplayMetrics displayMetrics = this.getContext().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels; // / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels; // / displayMetrics.density;
        LayoutInflater inflater = (LayoutInflater)a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.about, null);
        layout.setMinimumWidth((int)(dpWidth * 0.9f));
        layout.setMinimumHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(layout);
        ok = (Button) findViewById(R.id.dismiss);
        ok.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dismiss:
                //a.finish();
                break;
            default:
                break;
        }
        dismiss();
    }
}