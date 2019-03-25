package com.jiuj.absen.Receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jiuj.absen.ActivityInternetAlert;
import com.jiuj.absen.ActivityLogin;
import com.jiuj.absen.ActivityMaps;
import com.jiuj.absen.Utils.NetworkUtil;
import com.jiuj.absen.Utils.PrefManager;

public class NetworkChangeReceiver extends BroadcastReceiver {
    PrefManager session;
    String activityState = "";
    String currActivity = "";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        session = new PrefManager(context);
        activityState = session.getKEY_Activity();
        currActivity = session.getKEY_Send_Activity();
        String status = NetworkUtil.getConnectivityStatusString(context);
        //Log.d("xxxx",activityState);
        if("Mobile data enabled".equalsIgnoreCase(status) || "Wifi enabled".equalsIgnoreCase(status)){
            //Toast.makeText(context, activityState, Toast.LENGTH_LONG).show();
        }else if("Not connected to Internet".equalsIgnoreCase(status)){
                Intent i = new Intent();
                i.setClassName("com.jiuj.absen", "com.jiuj.absen.ActivityInternetAlert");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(i);
        }
    }
}
