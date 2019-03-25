package com.jiuj.absen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import com.github.chrisbanes.photoview.PhotoView;
import com.instacart.library.truetime.TrueTime;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.MenuPage.MenuActivity;
import com.jiuj.absen.Utils.InitTrueTimeAsyncTask;
import com.jiuj.absen.Utils.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.jiuj.absen.ActivityMaps.isNetworkConnected;

public class ActivityAbsenPhoto extends AppCompatActivity {
    private static String TAG = ActivityAbsenPhoto.class.getName();
    TextView txtName, txtTime, txtLoc, txtAddr;
    FloatingActionButton fabClose, fabUpload;
    PhotoView img;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    Boolean verifyInternet, reachable;
    RequestQueue queue;
    JsonObjectRequest req;
    String path = "";
    String encodedImage = "";
    private File file = null;
    private byte[] byteImg = null;
    private Bitmap SaveGambar = null;
    private static final int CAMERA_REQUEST = 1888;
    protected static final String PHOTO_TAKEN = "photo_taken";
    private ProgressDialog pDialog;
    String path2, sNoref, sTgl;
    File file2;
    Bitmap bitmap3;
    String sAddress = "";
    String sName = "";
    String sNik = "";
    String gLat = "";
    String gLong = "";
    String sDistance = "";
    String sDeviceid = "";
    String sTime = "";
    String sTgl2 = "";
    String url = "";
    String status = "";
    String msg = "";
    String sFakeGPS = "";
    String sNtpTime = "";
    PrefManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_absen_new_2);

        txtName = (TextView) findViewById(R.id.txtName);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtLoc = (TextView) findViewById(R.id.txtLoc);
        txtAddr = (TextView) findViewById(R.id.txtAddress);
        fabClose = (FloatingActionButton) findViewById(R.id.fabClose);
        fabUpload = (FloatingActionButton) findViewById(R.id.fabUpload);
        img = (PhotoView) findViewById(R.id.image);

        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();

        session = new PrefManager(this);

        sDeviceid = dbx.deviceid();

        if (android.os.Build.VERSION.SDK_INT > 22) {
            StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(newbuilder.build());
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Uploading Image ...");
        pDialog.setCancelable(false);

        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/absen_camera_rslt.jpg";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sName = getIntent().getStringExtra("namex");
            sNik = getIntent().getStringExtra("nikx");
            gLat = getIntent().getStringExtra("glat");
            gLong = getIntent().getStringExtra("glong");
            sAddress = getIntent().getStringExtra("addrx");
            sDistance = getIntent().getStringExtra("distancex");
            sTime = getIntent().getStringExtra("timex");
            sFakeGPS = getIntent().getStringExtra("fake");
        }

        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();
                /*
                Intent i = new Intent(ActivityAbsenPhoto.this, ActivityMaps.class);
                startActivity(i);
                finish();
                */
            }
        });
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInternetState();
            }
        });
        initTrueTime(this);
        startCameraActivity();
    }

    protected void startCameraActivity() {
        file = new File(path);
        if(file.exists()){file.delete();}
        Uri outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED){
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                onPhotoTaken();
            }
        }else{
            ActivityMaps.aMaps.finish();
            startActivity(new Intent(ActivityAbsenPhoto.this, ActivityMaps.class));
            finish();
        }
    }

    public void onPhotoTaken()  {
        boolean taken = true;
        boolean imgCapFlag = true;
        path2 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/absen_camera_rslt.jpg";
        file2 =  new File(path2);
        FileInputStream instream;
        try {
            //File f = new File(file);
            ExifInterface exif = new ExifInterface(file2.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            int angle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            String sTgl2 = dbx.getDateTime();
            sTgl = dbx.getDateTime();
            String strTgl = sTgl.replace("-","").replace(" ","").replace(":","");
            getNtpTime();
            sNoref = strTgl;
            Matrix mat = new Matrix();
            mat.postRotate(angle);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            SaveGambar = BitmapFactory.decodeFile(path2, options);
            bitmap3 = Bitmap.createBitmap(SaveGambar, 0, 0, SaveGambar.getWidth(),
                    SaveGambar.getHeight(), mat, true);
            Bitmap bitmap2 = mark(bitmap3, "Noref : "+strTgl, "Nama : " + sName + " / "+sNik, "Tgl : " + sNtpTime );
            FileOutputStream savebos=new FileOutputStream(file2);
            //FileOutputStream savebos = getActivity().getApplicationContext().openFileOutput("msurvey_camera_rslt.jpg", Context.MODE_PRIVATE);
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 50, savebos);
            //bitmap3.compress(Bitmap.CompressFormat.JPEG, 50, savebos);
            instream = new FileInputStream(file2);
            BufferedInputStream bif = new BufferedInputStream(instream);
            byteImg = new byte[bif.available()];
            bif.read(byteImg);

            encodedImage = Base64.encodeToString(byteImg, Base64.DEFAULT);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        img.setImageBitmap(bitmap3);

        txtName.setText(sName+" - "+sNik);
        txtTime.setText(sTime);
        txtAddr.setText(sAddress);
        txtLoc.setText(sDistance+" meters from Office");
        //if(file2.exists()){file2.delete();}
        //file2=null;
    }

    public Bitmap mark(Bitmap src, String watermark, String watermark2, String watermark3) {
        int w = src.getWidth();
        int h = src.getHeight();
        int margin = 5;
        Paint.FontMetrics fm = new Paint.FontMetrics();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        //paint.setColor(Color.parseColor("AA000000"));
        paint.setColor(Color.BLACK);
        paint.setTextSize(10);
        paint.setAntiAlias(true);
        paint.setUnderlineText(false);
        //canvas.drawRect(500 - margin, 500 + fm.top - margin,
        //		500 + paint.measureText(watermark) + margin, 500 + fm.bottom
        //               + margin, paint);
        float left = 10;
        float top = h-60;
        float right = 258;
        float bottom = 1200;
        //paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //paint.setStrokeWidth(10);
        paint.setAlpha(127);
        canvas.drawRect(left, top, right, bottom, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawText(watermark, 20, h-40, paint);
        canvas.drawText(watermark2, 20, h-25, paint);
        canvas.drawText(watermark3, 20, h-10, paint);
        return result;
    }

    private void saveToDB(){
        ContentValues values = new ContentValues();
        values.put("noref", sNoref);
        values.put("nama", sName);
        values.put("image", encodedImage);
        values.put("stsupload", "");
        values.put("uploadtime", "");
        values.put("createtime", sTgl2);
        db.insert("device_absen", null, values);
    }

    private void getInternetState(){
        verifyInternet = dbx.getInternetStatus();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if(verifyInternet == true)
        {
            uploadPhoto();
        }else{
            Toast.makeText(this,"No internet Access, Check your internet connection.",Toast.LENGTH_LONG).show();
        }
    }

    private void uploadPhoto(){
        showpDialog();
        url = "http://belumjadi.com/test/test7.php";
        getNtpTime();
        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("nik", sNik);
        jsonParams.put("name", sName);
        jsonParams.put("deviceid", sDeviceid);
        jsonParams.put("img", encodedImage);
        jsonParams.put("glat", gLat);
        jsonParams.put("glong", gLong);
        jsonParams.put("radius", sDistance);
        jsonParams.put("addr", sAddress);
        jsonParams.put("fake", sFakeGPS);
        jsonParams.put("time", sNtpTime);

        queue = Volley.newRequestQueue(this);
        req = new JsonObjectRequest(Request.Method.POST,
                url, new JSONObject(jsonParams), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                try {
                    status = response.getString("status");
                    msg = response.getString("msg");
                    if("1".equalsIgnoreCase(status)){
                        hidepDialog();
                        displayPrompt(msg);
                    }else{
                        hidepDialog();
                        displayPromptFailed(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    hidepDialog();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                Log.d("debugtest",status);
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
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE).setTitleText("").setContentText(msg).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                ActivityMaps.aMaps.finish();
                Intent i = new Intent(ActivityAbsenPhoto.this,MenuActivity.class);
                startActivity(i);
                finish();
            }
        }).show();
    }

    private void displayPromptFailed(String msg){
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("").setContentText(msg).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void onPause(){
        super.onPause();
        //session.logoutUser();
    }

    public static Date getTrueTime() {
        Date date = TrueTime.isInitialized() ? TrueTime.now() : new Date();
        return date;
    }

    public static void initTrueTime(Context ctx) {
        if (isNetworkConnected(ctx)) {
            if (!TrueTime.isInitialized()) {
                InitTrueTimeAsyncTask trueTime = new InitTrueTimeAsyncTask(ctx);
                trueTime.execute();
            }
        }
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    private void getNtpTime(){
        getTrueTime().toString();
        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();
        TimeZone tz = TimeZone.getDefault();
        sNtpTime = _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone(tz.getDisplayName(false, TimeZone.SHORT)));
        //ntpTime.setText(_formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+07:00")));
    }
}
