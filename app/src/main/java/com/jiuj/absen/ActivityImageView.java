package com.jiuj.absen;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.jiuj.absen.Adapter.AbsenList;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.Utils.ImageLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ActivityImageView extends AppCompatActivity {
    private static final String TAG = ActivityImageView.class.getSimpleName();
    private SQLiteDatabase db=null;
    private DatabaseHelper dbx=null;
    byte[] outImage;
    NetworkChangeReceiver myReceiver;
    FloatingActionButton fab;
    RequestQueue requestQueue;
    TextView txtName, txtTitle, txtAddr;
    PhotoView photoView, photoViewLoc;
    View vLoc;
    String imgxx, sNoref;
    Bitmap theImage;
    Button btn1;
    String urlMaps="";
    String gLat = "";
    String gLong = "";
    String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test2);
        photoView = (PhotoView) findViewById(R.id.image);
        photoViewLoc = (PhotoView) findViewById(R.id.image2);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtName = (TextView) findViewById(R.id.txtName);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtAddr = (TextView) findViewById(R.id.txtAddr);
        vLoc = (View) findViewById(R.id.vLoc);
        dbx = new DatabaseHelper(this);
        db=dbx.getWritableDatabase();
        myReceiver= new NetworkChangeReceiver();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        int loader = R.drawable.loader;

        key = getResources().getString(R.string.google_maps_key);
        //String url = "https://maps.googleapis.com/maps/api/staticmap?" +
         //       "markers=color:red|"+lat+","+lon+"&zoom=17&size=640x300" +
          //      "&key="+key;

        //urlMaps = "https://maps.googleapis.com/maps/api/staticmap?center="+lat+","+lon+"&markers=color:red%7Clabel:C%7C"+lat+","+lon+"&zoom=15&size=200x100&key="+key;
        ImageLoader imgLoader = new ImageLoader(getApplicationContext());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sNoref = getIntent().getStringExtra("key");
            gLat = getIntent().getStringExtra("glat");
            gLong = getIntent().getStringExtra("glong");
            if("".equalsIgnoreCase(getIntent().getStringExtra("key4"))){
                //photoView.setImageResource(R.drawable.noimage_new);
                photoView.setVisibility(View.GONE);
                vLoc.setVisibility(View.GONE);
                urlMaps = "https://maps.googleapis.com/maps/api/staticmap?" +
                        "markers=color:red|"+gLat+","+gLong+"&zoom=17&size=640x300" +
                        "&key="+key;
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round);
                Glide.with(this).load(urlMaps).apply(options).into(photoViewLoc);
            }else{
                photoView.setVisibility(View.VISIBLE);
                vLoc.setVisibility(View.VISIBLE);
                outImage= getIntent().getByteArrayExtra("key2");
                ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
                theImage = BitmapFactory.decodeStream(imageStream);
                photoView.setImageBitmap(theImage);
                //photoView.setImageResource(urlMaps);
                //imgLoader.DisplayImage(urlMaps, loader, photoView);
                urlMaps = "https://maps.googleapis.com/maps/api/staticmap?" +
                        "markers=color:red|"+gLat+","+gLong+"&zoom=17&size=640x300" +
                        "&key="+key;
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round);
                Glide.with(this).load(urlMaps).apply(options).into(photoViewLoc);
            }
            String strName = getIntent().getStringExtra("key");
            strName = strName.replace("-", "\n");
            txtName.setText(getIntent().getStringExtra("key3"));
            txtTitle.setText(sNoref);
            txtAddr.setText(getIntent().getStringExtra("addr"));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        //db.close();s
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //db.close();
            Intent i = new Intent(this,MainAbsen.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendImage(){
        String url = "http://belumjadi.com/test/test4.php";
        requestQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String encodedImageData =getEncoded64ImageStringFromBitmap(theImage);
                Log.d("imgstr", encodedImageData);
                Map<String,String> params = new Hashtable<String, String>();
                params.put("imgstr", encodedImageData);
                //params.put(KEY_NAME, name);
                //returning parameters
                return params;
            }
        };
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        requestQueue.add(postRequest);
    }

    private void getImg(){
        String selectQuery = "SELECT image from device_absen where noref='"+sNoref+"'";
        final Cursor csr = db.rawQuery(selectQuery, null);
        if (csr.moveToFirst()) {
            imgxx =  csr.getString(0);
        }
        byte[] decodedString = Base64.decode(imgxx, Base64.DEFAULT);
        String a = new String(decodedString);
        Log.d("imgstr", a);
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteFormat = stream.toByteArray();
        String imgString = Base64.encodeToString(byteFormat, Base64.DEFAULT);
        return imgString;
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
