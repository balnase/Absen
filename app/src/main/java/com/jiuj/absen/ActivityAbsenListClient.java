package com.jiuj.absen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.jiuj.absen.Adapter.AbsenAdapter;
import com.jiuj.absen.Adapter.AbsenClientAdapter;
import com.jiuj.absen.Adapter.AbsenClientList;
import com.jiuj.absen.Adapter.AbsenList;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.MenuPage.MenuActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ActivityAbsenListClient extends AppCompatActivity {
    private static String TAG = ActivityAbsenListClient.class.getSimpleName();
    List<String> iNik, iName, iAddr, iImage, iTime;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    Boolean verifyInternet, reachable;
    private ProgressDialog pDialog;
    String url = "";
    String status = "";
    String sTgl = "";
    RequestQueue queue;
    JsonArrayRequest req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absen_list);
        getSupportActionBar().setTitle("List Absen");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();

        if (android.os.Build.VERSION.SDK_INT > 22) {
            StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(newbuilder.build());
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading ...");
        pDialog.setCancelable(false);

        DownloadData();
    }

    @Override
    public void onBackPressed(){
        db.close();
        Intent i = new Intent(this,MenuActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            db.close();
            Intent i = new Intent(this,MenuActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void DownloadData(){
        showpDialog();

        /*
        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("nik", sNik);
        jsonParams.put("deviceid", dbx.deviceid());
        */

        url = "http://belumjadi.com/test/test8.php?nik="+dbx.getNIK()+"&deviceid="+dbx.deviceid();
        //url = "http://belumjadi.com/test/test8.php";
        Log.d("coba2", url.toString());

        queue = Volley.newRequestQueue(this);
        req = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        Log.d("coba", response.toString());
                        try {
                            HashMap<String, String> map;

                            iNik = new ArrayList<String>();
                            iName = new ArrayList<String>();
                            iAddr = new ArrayList<String>();
                            iImage = new ArrayList<String>();
                            iTime = new ArrayList<String>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject person = (JSONObject) response
                                        .get(i);
                                status = person.getString("status");
                                if(status.equals("1")){
                                    iNik.add(person.getString("nik"));
                                    iName.add(person.getString("name"));
                                    iAddr.add(person.getString("gaddr"));
                                    iImage.add(person.getString("image"));
                                    iTime.add(person.getString("taketime"));
                                }else{}
                            }
                            if(status.equals("1")) {
                                saveToDB();
                            }else{
                                hidepDialog();
                                Toast.makeText(getApplicationContext(),
                                        "Hospital list not found",
                                        Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getClass().equals(NoConnectionError.class)){
                    String errStatus = "No internet Access, Check your internet connection.";
                    hidepDialog();
                    Toast.makeText(getApplicationContext(),
                            errStatus.toString(), Toast.LENGTH_LONG).show();
                }

                if (error.getClass().equals(TimeoutError.class)){
                    String errStatus = "Connection Timeout !! Please Try Again.";
                    hidepDialog();
                    Toast.makeText(getApplicationContext(),
                            errStatus.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        queue.add(req);
    }

    private void saveToDB(){
        deleteLV();
        sTgl = dbx.getDateTime();
        for(int i=0; i<iNik.size(); i++) {
            String execstr="INSERT INTO device_absen VALUES ('"+iNik.get(i).toString()+"','"+iName.get(i).toString()+"','"+iImage.get(i).toString()+"','"+iAddr.get(i).toString()+"','"+iTime.get(i).toString()+"','"+sTgl+"')";
            db.execSQL(execstr);
        }
        //cancelTimer();
        displayLv();
    }

    private void deleteLV() {
        String execstr="delete from device_absen";
        db.execSQL(execstr);
    }

    private void displayLv(){
        final ArrayList<AbsenClientList> imageArry = new ArrayList<AbsenClientList>();
        AbsenClientAdapter adapter;
        String selectQuery = "SELECT * FROM device_absen";

        final Cursor csr = db.rawQuery(selectQuery, null);
        if (csr.moveToFirst()) {
            do
            {
                AbsenClientList KFL = new AbsenClientList(csr.getString(1)+" - "+csr.getString(0), csr.getString(4), csr.getString(2), csr.getString(3));
                imageArry.add(KFL);
            } while (csr.moveToNext());
        }
        adapter = new AbsenClientAdapter(this, R.layout.list_item, imageArry);
        ListView dataList = (ListView) this.findViewById(R.id.listview);
        dataList.setAdapter(adapter);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
