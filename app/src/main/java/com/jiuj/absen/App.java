package com.jiuj.absen;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;
import com.jiuj.absen.sample.SampleActivity;

import java.io.IOException;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        //initRxTrueTime();
        initTrueTime();
    }

    /**
     * init the TrueTime using a AsyncTask.
     */
    private void initTrueTime() {
        new InitTrueTimeAsyncTask().execute();
    }

    // a little part of me died, having to use this
    private class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                TrueTime.build()
                        .withSharedPreferences(App.this)
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(false)
                        .withConnectionTimeout(3_1428)
                        .initialize();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "something went wrong when trying to initialize TrueTime", e);
            }
            return null;
        }
    }

}
