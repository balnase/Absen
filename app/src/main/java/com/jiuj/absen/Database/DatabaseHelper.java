package com.jiuj.absen.Database;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    Context context;
    String RespJson="";
    String NETWORK_STATUS;
    String REPLY_STATUS = "OK";
    String androidId = "";
    String VRespVolley="";
    String VResp="";
    boolean reachable;
    String RtnState="";
    String RtnArr[]=null;
    int exefldct=0;
    String exestr="";
    String sDeviceid;
    String sID ="";

    private static final String DB_NAME = "ANDRO_KKM.DB";
    private static final int DB_VERSION = 1;
    private static final String DB_URL_ETHERNET = "http://172.16.0.156:8084/top_andro/AppForms/External/sync_adaptor.jsp?querystr=";
    private static final String DB_URL_INTERNET = "http://";

    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table device_owner('deviceid' text, " +
                "'nik' text, 'name' text, 'createtime' datetime DEFAULT (datetime('now','localtime')))");

        db.execSQL("create table device_login('deviceid' text, 'userid' text, " +
                "'nama' text,'email' text, 'pass' text,'token' text, 'createtime' datetime DEFAULT (datetime('now','localtime')))");

        db.execSQL("create table db_currLogin('deviceid' text,  'userid' text ,'createtime' datetime DEFAULT (datetime('now','localtime')))");

        db.execSQL("create table device_absen('noref' text, 'nama' text, " +
                "'image' text, 'stsupload' text, 'glat' text,'glong' text," +
                "'uploadtime' datetime DEFAULT (datetime('now','localtime')), " +
                "'createtime' datetime DEFAULT (datetime('now','localtime')))");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS device_owner");
        //db.execSQL("DROP TABLE IF EXISTS device_login");
        //onCreate(db);
    }

    public String getCoreURL(){
        String urlText="http://116.0.3.164:8080/top_andro/ProsesDest/incoming/Connector.jsp?Query=<DEVICE_ID>|<ACTION_ID>|<APP_ID>|<QUERYSTR>";
        sDeviceid = getMacAddr();
        sDeviceid = sDeviceid.replace(":","");
        urlText=urlText.replace("<DEVICE_ID>",sDeviceid);
        urlText=urlText.replace("<APP_ID>","MC");
        return urlText;
    }

    public String deviceid(){
        String strDevice = Build.SERIAL;
        return strDevice;
    }

    public String deviceBrand(){
        String strDeviceBrand = Build.BRAND;
        return strDeviceBrand;
    }

    public String deviceModel(){
        String strDeviceModel = Build.MODEL;
        return strDeviceModel;
    }

    public String getUploadURL(){
        //String urlText="http://192.168.2.34:81/api";
        String urlText="http://belumjadi.com/attendance/public/api";
        return urlText;
    }

    public String getImageURL(){
        //String urlText="http://192.168.2.34:81/images/android/";
        String urlText="http://belumjadi.com/attendance/public/images/android/";
        //sDeviceid = sDeviceid.replace(":","");
        //urlText=urlText.replace("<DEVICE_ID>",sDeviceid);
        return urlText;
    }

    public Boolean getInternetStatus()
    {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if(isNetworkAvailable()){
            try {
                reachable = InetAddress.getByName("45.64.1.247").isReachable(5000);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(reachable == true)
            {
                reachable= true;
            }else{
                reachable= false;
            }
        }else{
            reachable= false;
        }

        return reachable;
    }

    public static String getMacAddr2() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public String getMacAddr(){
        db=getWritableDatabase();
        String selectQuery = "select deviceid from device_owner ";
        Cursor csr = db.rawQuery(selectQuery, null);
        if(csr !=null && csr.moveToFirst()){
            sID = csr.getString(0);
        }else{
            sID = getMacAddr2();
        }
        return sID;
    }

    public String getNIK(){
        db=getWritableDatabase();
        String sNik ="";
        String selectQuery = "select userid from device_login ";
        Cursor csr = db.rawQuery(selectQuery, null);
        if(csr !=null && csr.moveToFirst()){
            sNik = csr.getString(0);
        }
        return sNik;
    }

    public String getToken(){
        db=getWritableDatabase();
        String sToken ="";
        String selectQuery = "select token from device_login ";
        Cursor csr = db.rawQuery(selectQuery, null);
        if(csr !=null && csr.moveToFirst()){
            sToken = csr.getString(0);
        }
        return sToken;
    }

    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public boolean get_internet_connected_state() throws InterruptedException, IOException
    {
        String command = "ping -c 3 116.0.3.164";
        return (Runtime.getRuntime().exec (command).waitFor() == 0);
    }

    public static boolean isNetworkAvailable () {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService (Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
