package com.jiuj.absen.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.instacart.library.truetime.TrueTime;
import com.jiuj.absen.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class SampleActivity extends AppCompatActivity {
    TextView timePST,timeGMT,timeDeviceTime;
    Button tt_btn_refresh;
    Handler mClockHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        timePST = (TextView) findViewById(R.id.tt_time_pst);
        timeGMT = (TextView) findViewById(R.id.tt_time_gmt);
        timeDeviceTime = (TextView) findViewById(R.id.tt_time_device);
        tt_btn_refresh = (Button) findViewById(R.id.tt_btn_refresh);

        timeGMT.setVisibility(View.GONE);
        timeDeviceTime.setVisibility(View.GONE);

        tt_btn_refresh.setEnabled(TrueTime.isInitialized());
        tt_btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateTime();
            }
        });

        UpdateTime();
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    private void UpdateTime(){
        if (!TrueTime.isInitialized()) {
            Toast.makeText(SampleActivity.this, "Sorry TrueTime not yet initialized. Trying again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();

        timeGMT.setText(getString(R.string.tt_time_gmt,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"))));
        timePST.setText(getString(R.string.tt_time_pst,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+07:00"))));
        timeDeviceTime.setText(getString(R.string.tt_time_device,
                _formatDate(deviceTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+07:00"))));
    }

    private void setTimer(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                UpdateTime();
            }
        },1000,1000);
    }


}
