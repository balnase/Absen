package com.jiuj.absen.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jiuj.absen.ActivityMenu;

import java.util.HashMap;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "AndroidHivePref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NIK = "nik";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LONG = "long";
    public static final String KEY_TOKEN = "email";
    public static final String KEY_ACTIVITY = "activity";
    public static final String KEY_SEND_ACTIVITY = "SendActivity";

    public PrefManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String email){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(_context, ActivityMenu.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        return user;
    }

    public void logoutUser(){
        editor.clear();
        editor.commit();
    }

    public String getKEY_Radius(){
        String radius = pref.getString(KEY_RADIUS,null);
        return radius;
    }

    public String getKEY_Token(){
        String token = pref.getString(KEY_TOKEN,null);
        return token;
    }

    public String getKEY_NIK(){
        String nik = pref.getString(KEY_NIK,null);
        return nik;
    }

    public String getKEY_LAT(){
        String lat = pref.getString(KEY_LAT,null);
        return lat;
    }

    public String getKEY_LONG(){
        String lng = pref.getString(KEY_LONG,null);
        return lng;
    }

    public String getKEY_Name(){
        String name = pref.getString(KEY_NAME,null);
        return name;
    }

    public String getKEY_Email(){
        String email = pref.getString(KEY_EMAIL,null);
        return email;
    }

    public String getKEY_Activity(){
        String activity = pref.getString(KEY_ACTIVITY,null);
        return activity;
    }

    public String getKEY_Send_Activity(){
        String activity = pref.getString(KEY_SEND_ACTIVITY,null);
        return activity;
    }

    public void createGPSLoc(String lat, String lng, String radius){
        editor.putString(KEY_LAT, lat);
        editor.putString(KEY_LONG, lng);
        editor.putString(KEY_RADIUS, radius);
        editor.commit();
    }

    public void createUserSession(String nik, String name, String email, String token){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NIK, nik);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void createAcvtivity(String activity){
        editor.putString(KEY_ACTIVITY, activity);
        editor.commit();
    }

    public void createSendAcvtivity(String activity){
        editor.putString(KEY_SEND_ACTIVITY, activity);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
