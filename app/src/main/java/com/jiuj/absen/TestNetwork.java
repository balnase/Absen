package com.jiuj.absen;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jiuj.absen.Receiver.ConnectivityReceiver;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;

public class TestNetwork extends AppCompatActivity {
    private FloatingActionButton fab;
    private Button btnCheck;
    NetworkChangeReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myReceiver= new NetworkChangeReceiver();
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        //showToast(isConnected);
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(myReceiver, filter);
    }

    /*
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showToast(isConnected);
    }

    public void showToast(boolean isConnected){
        String msg;
        if (isConnected) {
            msg = "Good! Connected to Internet";
        } else {
            msg = "Sorry! Not connected to internet";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    */

}
