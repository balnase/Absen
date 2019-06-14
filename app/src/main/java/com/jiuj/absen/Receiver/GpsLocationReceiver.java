package com.jiuj.absen.Receiver;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;

import com.jiuj.absen.R;

import java.util.List;

public class GpsLocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent)
    {

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
        {
            if (isAppForground(context)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                /*
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    context.startService(new Intent(context, LocationService.class));
                } else {
                    context.stopService(new Intent(context, LocationService.class));
                }
                */
            } else {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    KillNotif(context);
                    //context.startService(new Intent(context, LocationService.class));
                } else {
                    CreateNotif(context);
                    //context.stopService(new Intent(context, LocationService.class));
                }
            }
        }
    }

    public boolean isAppForground(Context mContext) {

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }

        return true;
    }

    public void CreateNotif(Context context){
        NotificationManager notif = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String sTitle = "GPS OFFLINE";
        String sMsg = "Onejek Driver gagal mendeteksi GPS";
        NotificationCompat.Builder localBuilder = new NotificationCompat.Builder(context).setContentTitle(sTitle.toString()).setContentText(sMsg.toString()).setSmallIcon(R.drawable.ic_location_white);
        notif.notify(27, new NotificationCompat.Builder(context).setContentTitle(sTitle.toString()).setContentText(sMsg.toString()).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).setTicker(sMsg.toString()).build());
        notif.cancel(27);
        localBuilder.setOngoing(true);
        notif.notify(27, localBuilder.build());
    }

    public void KillNotif(Context context){
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(27);
    }

}