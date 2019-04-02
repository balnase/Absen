package com.jiuj.absen;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.dx.dxloadingbutton.lib.LoadingButton;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.Utils.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ActivityRegister extends AppCompatActivity {
    private static String TAG = ActivityRegister.class.getName();
    public static Activity aReg;
    LoadingButton lbRegister;
    TextView shows,hides,register,login;
    EditText edNik, edName, edPass, edEmail;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    ProgressBar progressBar;
    Boolean verifyInternet, reachable;
    NetworkChangeReceiver myReceiver;
    String status = "";
    String url = "";
    String sNik = "";
    String sName = "";
    String sPass = "";
    String sEmail = "";
    String jNik = "";
    String jName = "";
    String jEmail = "";
    String jPass = "";
    String msg = "";
    PrefManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        aReg = this;
        shows = (TextView) findViewById(R.id.show);
        hides = (TextView) findViewById(R.id.hide);
        register = (TextView) findViewById(R.id.register);
        login = (TextView) findViewById(R.id.txt_login);
        lbRegister = (LoadingButton) findViewById(R.id.loading_btn);
        edNik = (EditText) findViewById(R.id.nik);
        edName = (EditText) findViewById(R.id.name);
        edEmail = (EditText) findViewById(R.id.email);
        edPass = (EditText) findViewById(R.id.pass);
        progressBar = (ProgressBar) findViewById(R.id.progBar);
        progressBar.setVisibility(View.GONE);

        //edName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        session = new PrefManager(this);
        myReceiver= new NetworkChangeReceiver();

        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        shows.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                edPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                hides.setVisibility(View.VISIBLE);
                shows.setVisibility(View.GONE);
            }
        });

        hides.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                edPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                hides.setVisibility(View.GONE);
                shows.setVisibility(View.VISIBLE);
            }
        });

        login.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = new Intent(ActivityRegister.this,ActivityLogin.class);
                startActivity(i);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                validateReg();
            }
        });

        lbRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lbRegister.startLoading(); //start loading
                //getInternetState();
                regUser();
            }
        });

        session.createAcvtivity(TAG);

    }

    private void getInternetState(){
        showProgBar();
        verifyInternet = dbx.getInternetStatus();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if(verifyInternet == true)
        {
            //startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
            //finish();
            regUser();
        }else{
            hideProgBar();
            Toast.makeText(this,"No internet Access, Check your internet connection.",Toast.LENGTH_LONG).show();
        }
    }

    private void regUser(){
        showProgBar();
        //url = "http://belumjadi.com/test/test5.php";
        //url = "http://192.168.2.34:81/api/register";
        url = dbx.getUploadURL()+"/register";
        String device_model = dbx.deviceBrand()+" "+dbx.deviceModel();

        Log.d("debugtest2",url);

        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("nik", sNik);
        jsonParams.put("name", sName);
        jsonParams.put("email", sEmail);
        jsonParams.put("pass", sPass);
        jsonParams.put("device_model", device_model);
        jsonParams.put("deviceid", dbx.deviceid());

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                url, new JSONObject(jsonParams), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    status = response.getString("status");
                    //msg = response.getString("msg");

                    if("1".equalsIgnoreCase(status)){
                        //lbRegister.loadingSuccessful();
                        Intent i = new Intent(ActivityRegister.this, ActivityLogin.class);
                        startActivity(i);
                        finish();
                    }else{
                        //lbRegister.loadingFailed();
                        hideProgBar();
                        displayPrompt(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    //lbRegister.loadingFailed();
                    hideProgBar();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                Log.d("debugtest",status);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getClass().equals(NoConnectionError.class)){
                    String errStatus = "No internet Access, Check your internet connection.";
                    //lbRegister.loadingFailed();
                    hideProgBar();
                    Toast.makeText(getApplicationContext(),
                            errStatus.toString(), Toast.LENGTH_LONG).show();
                }

                if (error.getClass().equals(TimeoutError.class)){
                    String errStatus = "Connection Timeout !! Please Try Again.";
                    //lbRegister.loadingFailed();
                    hideProgBar();
                    Toast.makeText(getApplicationContext(),
                            errStatus.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", "My useragent");
                return headers;
            }
        };
        queue.add(req);
    }

    private void displayPrompt(String msg){
        new AwesomeErrorDialog(this)
                .setTitle("")
                .setMessage(msg)
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(false)
                .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                .setButtonText(getString(R.string.dialog_ok_button))
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        Intent i = new Intent(ActivityRegister.this,ActivityLogin.class);
                        startActivity(i);
                        finish();
                    }
                })
                .show();

    }

    private void showProgBar(){
        progressBar.setVisibility(View.VISIBLE);
        register.setVisibility(View.GONE);
    }

    private void hideProgBar(){
        progressBar.setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);
    }

    private void validateReg(){
        sNik = edNik.getText().toString();
        sName = edName.getText().toString();
        sEmail = edEmail.getText().toString();
        sPass = edPass.getText().toString();
        if (TextUtils.isEmpty(sNik)){
            edNik.setError("Please enter your nik");
            edNik.requestFocus();
            return;
        }else if (TextUtils.isEmpty(sName)){
            edName.setError("Please enter your name");
            edName.requestFocus();
            return;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            edEmail.setError("Enter valid email");
            edEmail.requestFocus();
            return;
        }else if (TextUtils.isEmpty(sPass)){
            edPass.setError("Please enter your password");
            edPass.requestFocus();
            return;
        }else{
            getInternetState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(myReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }
}
