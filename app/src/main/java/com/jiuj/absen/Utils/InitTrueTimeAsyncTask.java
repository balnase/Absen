package com.jiuj.absen.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;
import com.jiuj.absen.ActivityLogin;

import java.io.IOException;

public class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context ctx;
    private static String TAG = InitTrueTimeAsyncTask.class.getSimpleName();

    public InitTrueTimeAsyncTask (Context context){
        ctx = context;
    }

    protected Void doInBackground(Void... params) {
        try {
            TrueTime.build()
                    .withSharedPreferences(ctx)
                    .withNtpHost("time.google.com")
                    .withLoggingEnabled(false)
                    .withConnectionTimeout(3_1428)
                    .initialize();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception when trying to get TrueTime", e);
        }
        return null;
    }
}
