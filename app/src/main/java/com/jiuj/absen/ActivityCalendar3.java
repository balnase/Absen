package com.jiuj.absen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.jiuj.absen.Adapter.AbsenClientAdapter;
import com.jiuj.absen.Adapter.AbsenClientList;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.MenuPage.MenuActivity;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.SampleCalendar.HomeCollection;
import com.jiuj.absen.SampleCalendar.HwAdapter;
import com.jiuj.absen.Utils.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityCalendar3 extends AppCompatActivity {
    private static String TAG = ActivityCalendar3.class.getName();
    public static Activity aCal;
    public GregorianCalendar cal_month, cal_month_copy;
    private HwAdapter hwAdapter;
    private TextView tv_month, txtToday;
    private ProgressDialog pDialog;
    List<String> iNik, iName, iAddr, iImage, iTime;
    NetworkChangeReceiver myReceiver;
    ListView dataList;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    RequestQueue queue;
    JsonArrayRequest req;
    PrefManager session;
    Boolean verifyInternet, reachable;
    String url = "";
    String status = "";
    String sTgl = "";
    String blnold = "";
    String sFrom = "";
    String sTo = "";
    GridView gridview;
    String formattedDate ="";
    String dateToday ="";
    String monthNow ="";
    String dateNow= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar4);
        aCal = this;
        getSupportActionBar().setTitle("Report Attendance");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageButton next = (ImageButton) findViewById(R.id.Ib_next);
        ImageButton previous = (ImageButton) findViewById(R.id.ib_prev);
        tv_month = (TextView) findViewById(R.id.tv_month);
        txtToday = (TextView) findViewById(R.id.txtToday);
        dataList = (ListView) this.findViewById(R.id.listview);
        txtToday.setVisibility(View.VISIBLE);
        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();
        session = new PrefManager(this);
        myReceiver= new NetworkChangeReceiver();
        if (android.os.Build.VERSION.SDK_INT > 22) {
            StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(newbuilder.build());
        }
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading ...");
        pDialog.setCancelable(false);
        cal_month = (GregorianCalendar) GregorianCalendar.getInstance();
        cal_month_copy = (GregorianCalendar) cal_month.clone();
        String b = "2019-04-01";
        Date c = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String[] separated = b.split("-");
        String a = getLastDay(separated[0],separated[1]);
        getToday();
        tv_month.setText(android.text.format.DateFormat.format("MMMM yyyy", cal_month));
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (cal_month.get(GregorianCalendar.MONTH) == 4&&cal_month.get(GregorianCalendar.YEAR)==2018) {
                    cal_month.set((cal_month.get(GregorianCalendar.YEAR) - 1), cal_month.getActualMaximum(GregorianCalendar.MONTH), 1);
                }
                else {}
                */
                dataList.setAdapter(null);
                setPreviousMonth();
                refreshCalendar();
                if(cal_month.get(GregorianCalendar.MONTH)>8){
                    blnold = cal_month.get(GregorianCalendar.YEAR)+"-"+(cal_month.get(GregorianCalendar.MONTH)+1)+"-01";
                }else{
                    blnold = cal_month.get(GregorianCalendar.YEAR)+"-0"+(cal_month.get(GregorianCalendar.MONTH)+1)+"-01";
                }
                String[] separated = blnold.split("-");
                String a = getLastDay(separated[0],separated[1]);
                sFrom = separated[0]+"-"+separated[1]+"-01";
                sTo = separated[0]+"-"+separated[1]+"-"+a;
                DownloadData();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (cal_month.get(GregorianCalendar.MONTH) == 5&&cal_month.get(GregorianCalendar.YEAR)==2018) {
                    cal_month.set((cal_month.get(GregorianCalendar.YEAR) + 1), cal_month.getActualMinimum(GregorianCalendar.MONTH), 1);
                }
                else {
                    Toast.makeText(ActivityCalendar3.this, blnold, Toast.LENGTH_SHORT).show();
                }
                */
                dataList.setAdapter(null);
                setNextMonth();
                refreshCalendar();
                if(cal_month.get(GregorianCalendar.MONTH)>8){
                    blnold = cal_month.get(GregorianCalendar.YEAR)+"-"+(cal_month.get(GregorianCalendar.MONTH)+1)+"-01";
                }else{
                    blnold = cal_month.get(GregorianCalendar.YEAR)+"-0"+(cal_month.get(GregorianCalendar.MONTH)+1)+"-01";
                }
                String[] separated = blnold.split("-");
                String a = getLastDay(separated[0],separated[1]);
                sFrom = separated[0]+"-"+separated[1]+"-01";
                sTo = separated[0]+"-"+separated[1]+"-"+a;
                DownloadData();
            }
        });
        session.createAcvtivity(TAG);
        DownloadData();
    }
    protected void setNextMonth() {
        if (cal_month.get(GregorianCalendar.MONTH) == cal_month.getActualMaximum(GregorianCalendar.MONTH)) {
            cal_month.set((cal_month.get(GregorianCalendar.YEAR) + 1), cal_month.getActualMinimum(GregorianCalendar.MONTH), 1);
        } else {
            cal_month.set(GregorianCalendar.MONTH, cal_month.get(GregorianCalendar.MONTH) + 1);
        }
        int month = cal_month.get(GregorianCalendar.MONTH) + 1;
        String bulan = String.valueOf(month);
        //Toast.makeText(ActivityCalendar3.this, bulan,Toast.LENGTH_LONG).show();
    }

    protected void setPreviousMonth() {
        if (cal_month.get(GregorianCalendar.MONTH) == cal_month.getActualMinimum(GregorianCalendar.MONTH)) {
            cal_month.set((cal_month.get(GregorianCalendar.YEAR) - 1), cal_month.getActualMaximum(GregorianCalendar.MONTH), 1);
        } else {
            cal_month.set(GregorianCalendar.MONTH, cal_month.get(GregorianCalendar.MONTH) - 1);
        }
        int month = cal_month.get(GregorianCalendar.MONTH)+1;
        String bulan = String.valueOf(month);
        //Toast.makeText(ActivityCalendar3.this, bulan,Toast.LENGTH_LONG).show();
    }

    public void refreshCalendar() {
        hwAdapter.refreshDays();
        hwAdapter.notifyDataSetChanged();
        tv_month.setText(android.text.format.DateFormat.format("MMMM yyyy", cal_month));
    }

    private void DownloadData(){
        showpDialog();
        List<Map<String,String>> listMap =  new ArrayList<Map<String, String>>();
        Map<String,String> map  = new HashMap<String,String>();
        try {
            map.put("nik", session.getKEY_NIK());
            map.put("deviceid", dbx.deviceid());
            map.put("from", sFrom);
            map.put("to", sTo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        listMap.add(map);
        //url = "http://belumjadi.com/test/test8.php?nik="+session.getKEY_NIK()+"&deviceid="+dbx.deviceid()+"&from="+sFrom+"&to="+sTo;
        url = "http://belumjadi.com/test/test8.php";
        Log.d("coba2", listMap.toString());

        queue = Volley.newRequestQueue(this);
        req = new JsonArrayRequest(Request.Method.POST, url, new JSONArray(listMap),
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
                                setupCalendar();
                                Toast.makeText(getApplicationContext(), "Data not found", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getClass().equals(NoConnectionError.class)){
                    String errStatus = "No internet Access, Check your internet connection.";
                    hidepDialog();
                    dialogError(errStatus);
                }

                if (error.getClass().equals(TimeoutError.class)){
                    String errStatus = "Connection Timeout !! Please Try Again.";
                    hidepDialog();
                    dialogError(errStatus);
                }
            }
        });
        queue.add(req);
    }

    private void saveToDB(){
        deleteLV();
        sTgl = dbx.getDateTime();
        for(int i=0; i<iNik.size(); i++) {
            String execstr="INSERT INTO device_absen VALUES ('"+iNik.get(i).toString()+"','"+iName.get(i).toString()+"','"+iImage.get(i).toString()+"'," +
                    "'"+iAddr.get(i).toString()+"','"+iTime.get(i).toString()+"','"+sTgl+"')";
            db.execSQL(execstr);
        }
        //cancelTimer();
        setupCalendar();
        displayLv(formattedDate);
    }

    private void deleteLV() {
        String execstr="delete from device_absen";
        db.execSQL(execstr);
    }

    private void displayLv(String strdate){
        final ArrayList<AbsenClientList> imageArry = new ArrayList<AbsenClientList>();
        AbsenClientAdapter adapter;
        String fromDT = strdate+" 00:00:00";
        String toDT = strdate+" 24:00:00";
        String selectQuery = "SELECT * FROM device_absen where uploadtime >='"+fromDT+"' and uploadtime<='"+toDT+"'";
        Log.d("listview",selectQuery);
        final Cursor csr = db.rawQuery(selectQuery, null);
        if (csr.moveToFirst()) {
            do
            {
                AbsenClientList KFL = new AbsenClientList(csr.getString(1)+" - "+csr.getString(0), csr.getString(4), csr.getString(2), csr.getString(3));
                imageArry.add(KFL);
            } while (csr.moveToNext());
        }
        adapter = new AbsenClientAdapter(this, R.layout.list_item, imageArry);
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

    private void getInternetState(){
        verifyInternet = dbx.getInternetStatus();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if(verifyInternet == true)
        {
            //regUser();
        }else{
            Toast.makeText(this,"No internet Access, Check your internet connection.",Toast.LENGTH_LONG).show();
        }
    }

    /*
    public void onPause(){
        super.onPause();
        session.logoutUser();
    }
    */

    @Override
    public void onBackPressed(){
        db.close();
        Intent i = new Intent(this, MenuActivity.class);
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

    public static String getLastDay(String year, String month) {
        GregorianCalendar calendar = new GregorianCalendar();
        int yearInt = Integer.parseInt(year);
        int monthInt = Integer.parseInt(month);
        monthInt = monthInt - 1;
        calendar.set(yearInt, monthInt, 1);
        int dayInt = calendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        return Integer.toString(dayInt);
    }

    private void getToday(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dt = new SimpleDateFormat("dd MMMM yyyy");
        formattedDate = df.format(c);
        dateToday = dt.format(c);
        String[] sDate = formattedDate.split("-");
        sFrom = sDate[0]+"-"+sDate[1]+"-01";
        sTo = formattedDate;
        txtToday.setText("Today : "+dateToday);
    }

    private void setupCalendar(){
        hwAdapter = new HwAdapter(this, cal_month, HomeCollection.date_collection_arr);
        //HomeCollection.date_collection_arr.clear();
        HomeCollection.date_collection_arr=new ArrayList<HomeCollection>();
        String selectQuery = "SELECT * FROM device_absen";
        final Cursor csr = db.rawQuery(selectQuery, null);
        if (csr.moveToFirst()) {
            do
            {
                String qqq = csr.getString(4);
                String asubstring = qqq.substring(0, 10);
                HomeCollection.date_collection_arr.add( new HomeCollection(asubstring ,csr.getString(1)));
            } while (csr.moveToNext());
        }
        /*
        HomeCollection.date_collection_arr.add( new HomeCollection("2018-07-08" ,"Diwali"));
        */
        gridview = (GridView) findViewById(R.id.gv_calendar);
        gridview.setAdapter(hwAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String selectedGridDate = HwAdapter.day_string.get(position);
                displayLv(selectedGridDate);
                //Toast.makeText(ActivityCalendar3.this,selectedGridDate,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void dialogError(String msg){
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this,SweetAlertDialog.ERROR_TYPE).setTitleText("Internet Error");
        sweetAlertDialog.setContentText(msg);
        sweetAlertDialog.setCancelText("Cancel");
        sweetAlertDialog.showCancelButton(true);
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismiss();
            }
        });
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                DownloadData();
            }
        });
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
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