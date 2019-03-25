package com.jiuj.absen;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.dx.dxloadingbutton.lib.LoadingButton;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.MenuPage.MenuActivity;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.Utils.PrefManager;
import com.karan.churi.PermissionManager.PermissionManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import java.util.HashMap;
import java.util.Map;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityLogin extends AppCompatActivity {
    private static String TAG = ActivityLogin.class.getName();
    public static Activity aLogin;
    PermissionManager permissionManager;
    LoadingButton lbLogin;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    ProgressBar progressBar;
    PrefManager session;
    Boolean verifyInternet, reachable;
    TextView shows,hides,login,txtSignup, txtForgot;
    NetworkChangeReceiver myReceiver;
    EditText edPass, edEmail;
    Activity activ;
    String status = "";
    String url = "";
    String msg = "";
    String sNik = "";
    String sPass = "";
    String sName = "";
    String sDeviceid = "";
    String sTgl = "";
    String sToken = "";
    String sRadius = "";
    String sLat = "";
    String sLong = "";
    String sEmail = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        aLogin = this;
        shows = (TextView) findViewById(R.id.show);
        hides = (TextView) findViewById(R.id.hide);
        login = (TextView) findViewById(R.id.login);
        lbLogin = (LoadingButton) findViewById(R.id.loading_btn);
        txtSignup = (TextView) findViewById(R.id.txt_signup);
        txtForgot = (TextView) findViewById(R.id.txt_forgot);
        edPass = (EditText) findViewById(R.id.pass);
        edEmail = (EditText) findViewById(R.id.email);
        progressBar = (ProgressBar) findViewById(R.id.progBar);
        progressBar.setVisibility(View.GONE);

        session = new PrefManager(this);
        session.createAcvtivity(TAG);
        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();
        //edNik.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        myReceiver= new NetworkChangeReceiver();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            permissionManager = new PermissionManager() {};
            permissionManager.checkAndRequestPermissions(this);
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
            public void onClick(View v) {
                validateLogin();
                //getInternetState();
            }
        });

        txtForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ActivityLogin.this,ActivityRegister.class);
                startActivity(i);
                finish();
            }
        });

        lbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lbLogin.startLoading(); //start loading
                getInternetState();
                //doLogin();
            }
        });
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
            doLogin();
            //createSession();
        }else{
            hideProgBar();
            Toast.makeText(this,"No internet Access, Check your internet connection.",Toast.LENGTH_LONG).show();
        }
    }

    private void doLogin(){
        //url = "http://belumjadi.com/test/test6.php";
        url = "http://192.168.2.34:81/api/login";

        String device_model = dbx.deviceBrand()+" "+dbx.deviceModel();
        Log.d("debugtest2",url);

        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("email", sEmail);
        jsonParams.put("password", sPass);
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
                    msg = response.getString("msg");

                    if("1".equalsIgnoreCase(status)){
                        sName = response.getString("name");
                        sNik = response.getString("nik");
                        sRadius = response.getString("radius");
                        sLat = response.getString("glat");
                        sLong = response.getString("glong");
                        sToken = response.getString("access_token");
                        //insertLogin();
                        createSession();
                    }else{
                        hideProgBar();
                        displayPrompt(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideProgBar();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                Log.d("debugtest",status);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getClass().equals(NoConnectionError.class)){
                    hideProgBar();
                    String errStatus = "No internet Access, Check your internet connection.";
                    Toast.makeText(getApplicationContext(), errStatus.toString(), Toast.LENGTH_LONG).show();
                }

                if (error.getClass().equals(TimeoutError.class)){
                    hideProgBar();
                    String errStatus = "Connection Timeout !! Please Try Again.";
                    Toast.makeText(getApplicationContext(), errStatus.toString(), Toast.LENGTH_LONG).show();
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
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("").setContentText(msg).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }

    private void insertLogin(){
        sDeviceid = dbx.deviceid();
        sTgl = dbx.getDateTime();
        String execstr="delete FROM device_login";
        db.execSQL(execstr);
        String insQuery = "insert into device_login values('"+sDeviceid+"', '"+sNik+"', '"+sName+"', '"+sEmail+"','"+sPass+"', '"+sToken+"','"+sTgl+"')";
        db.execSQL(insQuery);
        Intent i = new Intent(getBaseContext(),MenuActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
    }

    private void showProgBar(){
        progressBar.setVisibility(View.VISIBLE);
        login.setVisibility(View.GONE);
    }

    private void hideProgBar(){
        progressBar.setVisibility(View.GONE);
        login.setVisibility(View.VISIBLE);
    }

    private void validateLogin(){
        sEmail = edEmail.getText().toString();
        sPass = edPass.getText().toString();
        if(!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
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

    private void createSession(){
        //session.createUserSession(sNik,sName,sEmail,sToken);
        session.createGPSLoc("-6.165038","106.817275","300");
        //session.createGPSLoc(sLat,sLong,sRadius);
        session.createUserSession("000/0511/093","Ricky Balnase Lukas",sEmail,"q1w2e3r4t5y6");
        Intent i = new Intent(getBaseContext(),MenuActivity.class);
        startActivity(i);
        finish();
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
