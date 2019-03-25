package com.jiuj.absen;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.Utils.NetworkUtil;
import com.jiuj.absen.Utils.PrefManager;

public class ActivityInternetAlert extends AppCompatActivity {
    private static final String TAG = ActivityInternetAlert.class.getSimpleName();
    public static Activity aInternet;
    PrefManager session;
    Button btnRetry;
    String activityState = "";
    String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_item_internet_image);
        aInternet = this;
        btnRetry = (Button) findViewById(R.id.bt_retry);
        session = new PrefManager(this);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityState = session.getKEY_Activity();
                status = NetworkUtil.getConnectivityStatusString(ActivityInternetAlert.this);
                if("Not connected to Internet".equalsIgnoreCase(status)){
                }else{
                    try {
                        Class<?> c = Class.forName(activityState);
                        Intent intent = new Intent(ActivityInternetAlert.this, c);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed(){

    }
}
