package com.jiuj.absen;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jiuj.absen.CustomCalendar.CalendarActivity;
import com.karan.churi.PermissionManager.PermissionManager;
import com.nekolaboratory.EmulatorDetector;
import com.scottyab.rootbeer.RootBeer;

import java.util.Random;

public class ActivitySplash extends AppCompatActivity {
    int i1 = 0;
    int i2 = 0;
    String sRadio = "";
    Activity activ;
    PermissionManager permissionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        sRadio = Build.getRadioVersion();

        Random r = new Random();
        i1 = r.nextInt(5);

        this.activ = this;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //startActivity(new Intent(ActivitySplash.this, ActivityLogin.class));
                //finish();
                checkEmulator();
            }
        }, 2000);
    }

    private void checkEmulator(){
        if(EmulatorDetector.isEmulator(this)){
            Toast.makeText(ActivitySplash.this, "is Emulator", Toast.LENGTH_SHORT).show();
        }else{
            checkRoot();
            //Toast.makeText(ActivitySplash.this, "is Real", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRoot(){
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted()) {
            Toast.makeText(ActivitySplash.this, "Rooted"+" / "+sRadio, Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(ActivitySplash.this, "Not Rooted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ActivitySplash.this, ActivityLogin.class));
            finish();
        }
    }

    private void checkRandom(){
        Random r2 = new Random();
        i2 = r2.nextInt(5);
        Toast.makeText(ActivitySplash.this, String.valueOf(i2)+"/"+String.valueOf(i1), Toast.LENGTH_SHORT).show();
        Log.d("nilai",String.valueOf(i2)+"/"+String.valueOf(i1));
        if(i1==i2){
            Toast.makeText(ActivitySplash.this, "sama", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(ActivitySplash.this, "Beda", Toast.LENGTH_SHORT).show();
        }
    }
}
