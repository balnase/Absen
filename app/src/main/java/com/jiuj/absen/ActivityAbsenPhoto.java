package com.jiuj.absen;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


import static com.jiuj.absen.ActivityMaps.isNetworkConnected;

public class ActivityAbsenPhoto extends AppCompatActivity {
    private static String TAG = ActivityAbsenPhoto.class.getName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
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
    CountDownTimer waitTimer;
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
    String sToken = "";
    String sUserid = "";
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
            }
        });
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInternetState();
            }
        });
        getDeviceID();
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
            //encodedImage = "iVBORw0KGgoAAAANSUhEUgAAAMAAAADACAYAAABS3GwHAAAgAElEQVR42u29d5hcxZnv/6mTOvf05CjNKOcsJEAEEQ3YJiw45/Wuubtre7l3966v7XXEOPz2/hzXG329ePc6LDbGhsUkgRBCCAmBMsppFEaTQ0+nE6ruHz0aaZhRlgBJ9XkePTDdp6ur67zfqvd9KxzBGVB9zZdmCJOrhWIGqMYjrwshrkajOc8opZYf/UvsU4KNKmB56wv3bzzdssSpXlh7/RcbDYz7UOLjCFL6NmjefsqgB6EelMjvtzz3wL5zIoDSGz9XEpHO9wXi47qFNReOFtSDOcO9r3vJd3rPWAB11335DlDfF0I06SbVXICu0l4Q9x1a+vXfn7YA6q7/0r/pXl9zsYwGh567/xMjvWeObPxf/opA3KebTnMxIBCzE2OuJb1n2bKTCqB+8Zc/JgTf182mubhEwOJk47V703uXrT+uC1R/3ZdmgXheZ3k0F6kv1ANq8cGl9w+KwDjyP6U3fq5EIR7Uxq+5iIeBlEI8WHrj50qGCSAiQ/cJwWzdSpqLWgOC2REZum+IC1R64+dKokFor+79NZeKK5Q1C03dS77TawCEg9BXtPFrLiVXKBw4Xx50gYRSH9etorm0NKA+CiCqrvrKWMuWO4UQQjeL5pLxgpTyPZUfZZimv0g3h+ZSxCQ02RDCmKmbQnOJOkJNhkJV64bQXJpxAClDQI1uCs0lKQBDpAzdDJpLGS0AjRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAajRaARqMFoNFoAWg0WgAazTnD0k3w9kMBvhRIQACGUFjiramHGwjcwCCQBoEyEChMQ2KZkrApMYUWgOYsEYAECoEg61kY0qLUkTimQgIZz6ArUNiWR9SSWKJonOerLr6CrG+ifIuaiKQmlackkicayhFIk0w+Qnsmwr6+EDnhE7MDDM5fnbQALmYfVEDWF7ieQ31E0VTby7hUOxOSGeK2IlDQmrPY3l3Gjo4KtneHyBkeCUeizoPF9XkGlnSYUOIyr3YfVzW0M6emh9HlvSRLusBzaO8p5fXWMpY2V/HEzka299qEbA9bXHgi0AJ4C3t9T0FnxqE8ZHDnpP3cPO4gt0w8SLJuO4gcBCYIBYaE/npe2zmBX24ay0PbGujJG8RDPufCA1FAPhBkCw6N8YB3jd/NPVObuXzKJoi0Q8GCwAJpgq2orD7EtQ0u1y6Mcsv6WXzr+YUsO5xAmT6Ooc5KBJ4UuIEgUAIQmEISGhj1zgdmoumajyDEWCGE0Gb55hi+AnpdE0c5zKjM8ul5O/jGbUuYNm01IaMfmXNQhSjKdVBuCFUII0SO2oZt3DzlANUixpr9VXQWTEKWPGMRCKAgBdm8Q6VjclV9F//ryi18+sanaWh4HQomMh9BBQ5KWihloJRZ/H8vhCrAqKbtXFGVZdfhUezsjSCMAEOcmQj7XIsINqMTAU1Jl4a4R8Iy6c3ZSKHOqNwTIBFiuR4B3mQ/P+Mb5AshJqQKfGDKDv504UYqGrZDTiE7K4s9/qBJHHO3pAXpMgwnzQevfpaSWIYvLZnFznSYZKgw8A2n5+d3521KLIt3j23h9kkHuGfWVszUPshZyELZMXU5rgOH7CmnqWkj377V4M9/dxMvt8eI2e4pG6uvIO8b+F6ImVUZ/mjiLubXdVMTz2MbAYf7ozy5axQ/3TCKfKAIm1K7QBeqn19wHWojgoWNh7j3ss1cNXMDqD5kOgEYJzc4oZBuFMMs8M6rl+Ao+NTjC+nyTJK2PKnrcUSEPa5JSDrMqczw3onb+YurVuFU7oOMjcyEi1eKU3RkhEL2pRg3fi1fWpzgr568it0Zi5AVnPSjac/AkQ4TUi7za3fz5/N3MGPyqyAK4FuAYJzps2hqFZKb+fcNo/GlwDSUFsCF5Od3ZB1Slsk9Ew9wx+Rm7py5EVLNqL4USpYMGNtpGJwMIXoVN81ew8eaq/m71eNxrSzOcYLQI4bf7xl4rsO4Upd3jd/Bn8zbytixm8HzkD0JztyXUqieUq6a+wqf6ajhb5ZNo99TxG153HgjU3CYXOpxx4Qt3DyujUXT1oKVRuWiKBUa2oGYvXzjHcswgpv58YZaLKsYa2gBvI0NPwB68jZxYXFFdZq7pzRz31UroHIP9JQge8qLhi/O5EYqlBdGJNr5+ILXWL2/jmdawlRE88NSpEJAxhP4XohRMcllYw7zp3N3sGjWKhBpVCaBwjnDehxTIwFkDd4z/zVWtZbzfzfV0ys9oo6PAUglKASCwHeoi0quruvkM/O3c8O85WBkIRtHurGRnfXAwXBa+evFL7Ivcz1P7S1FKokhzl4EOgg+LwGuQb4QYU5Flk/O2c43r3+FGy57CUQ/Ml2KUsYZ9baGkAjDRxgSISQUQqQqemhM5tlxqI7tPVECUXSFfCUoSIOenEOZY/FHEw/wlws389fXvcToxo2QN5GFWFEhp1sP00M4LsL2jv4zfYQysKIdXFmVIWGZdGWSdPbHyLs2YWHRGJfcOv4gn5y5jS8sXsPEseugYCELUZQ6cT2UtIjE27i2vp/97bVs6w2jOKvAWCLEclG3+G+fRogbtQDOnoxf7GlHxyTXjG7hLxe9yuSpGyEXIPOxop9/WklCgRABwnbB8cCPgRstdreGhFA/kIGwYP36BXzr+SvZ1JnE9Yu3MmJL6pP9vGdyM5+4YiWUHIT+KDJwzqiLNywXwi5kKmjpLqW/YKIQKBQRW1IeyxMr6YBQF/hlrN08k2f3VJL2BBXhgJlVWa6dthlihyFnI/0Qp9sTGKEch1omcu+j1/NaewLEmWXBlFI+QjygXaBzQEEK+vM25Y7Bu6Yc5L3Td3D91K0Qa0F2JwBzwPDVqRub4UM4B0Toamtkc3uUvT2VdOXKUKo4GtQm25hW0c3UhmZmzV/Bz6o6Wb59PB15C1CURQIWNB4iVb0DPIXsKT19V+eI4UdykKnklS11LN05hQ2H6mnPhJCq+KtSEZ/RpX1cNmon1zQepL5uF3NmLWPOLLM4yijAN6AQHgj6z2AEtDwwJXXlrdRGXAwgUGc0iOkY4Jz4+UrSX7CJGzbXN/Tx/ql7+MjCl6FsH/SVIPtSpxfgDkSJRqQfZIyWw+N4uXk0j22ezjO7qjiQAwx/IKoVGMriimqP981Zy6Kxu5g7Zic3Lt4KhdDRiNO1UdkQ6lSyTMNGHx8Ry0Kmku17GlmydQY/eHk62/sMwqECtnm09w06bXJ7q7DXNPDOMX18asEq3jHtdbAzSM+Es5iyE0IiwjlUfzUb95Xzq3ULeaK5DE9JrLNczqldoDN1IJXCDcJMLcvzoanb+aMZO6ms2wmuXwzmxOnmqxWGU4CwJNNRyaObZvOb9bN4el+cfgpURj0cY3hmJ+0J0pkY01Lwmctf48NXvkAskkHm7TO0uYHRJ5QDFWfXoSoe3TCPxzZP5uUOA8cpkDhOyvVI8N+Rs6g2Q/zFZdu577qlhMPdSN88M8N3CkCYHc0NPLxpHr/dNJaNvYKScAHbOPOlF9oFOpuMB5DzTGZVm/zglteZNf0xyAlkJgY4p2f8SmBYRcMPeit5aXs9D702h59taCRj5KiK9ZMyGLbuRw0YXNJWJFP97HUFf/HEQtrSJXz+nb/HivQhc7HT7PUVRjgDMsGhQ7Ws3j+Of1w5l+cORXGcPKlYMOjMHa9dDKA64pOTPn/7wmTCluS+Gx/HsPNIN3TK9TEsF0yLro4mXtzXxN+vmMeywxHCToGyqBxMOOg06FshAAVCCGKWSzzcDoGPzJWfnrujBIbpQawf8qWs3DyVX6+bwW9fH81+16ci1kfKoOhjn0KRpY4iY/bwvVfGUxtfzJ/c8CSGk0O6kZMY3UCg7eTBNulpa+Cx12fyyMYpPL8/TsF0qUpkBsN3dYodRNiAsmSa7708kbHlrdw+fwWG5SED+4SlGKYHoQL01/DU1sn8euMMHt9VTgaX8ljunG9g0WnQM/JJwTIl69tiZHMRbhx3CCfcg/JDp+Z2KDCi/aBi7DjQxMOvLuCvn7qGP+xOYkUzlIV8jDNYWRkyIYPP6ubRTIwZTKzfDyIAZR5fhKEcwjHo66pj1c7JfG/ZYr790iReT0MiUiB2FmuNLAG9UvJq8xjmledoqGwFJTnePiwjnIWghK3NTfzrqkV87fnZrGh1cJwC0bOox4nSoHoEOEMMBKlIP49uq+eKl67mE+94DMPPI/3wCXpchWEXIBqQ66rk56uv4N/XzOaVThM7lGVUmUQqUXQlBOQ8RcGXg7luqcC2DMKWGDGhKhWUOpJD2Tx/u3QhEyo7mDj5VUgbKGUd/cSRzE40h+qt4emtU3l403Se2lVJq+dREu0ndQ6WNisg6Uh25jz+dsm1/Lyyi6qa7YhcBKmKIjBEALYHYZfDzeP4z43zeGTDBF7usImFC1RGz+8Caz0CnAWOCR2uYntXKXPjJg11B0D5oKwRU4ki5iPTVbywfRI/WLqY76ycyj63QHm0QMQEdUwf5ymoisCYhCLlKCrCUBc3iBjgSgP/OOk/BSScgJ29Dt2ZJFfUZEmUtSFUgJJGcRIrkQa3lLW7x/MPK67h/mVzWXY4hBHKU+IE53yXV8xWbOuycb0SpiYDkhWtiHAW4bhghOnqaOClnY18e8mN/HBtE61uQGnEwz6/G3b1CHD2mSCojnps7w7z2ScX8hPHZ+bMlxB+NxzJeggg5EG2jJUbp/PrDdN5ZPNo9hV8quJpSo/4+W8ou6s3xwfeOZWPXj+BrFsMqsOOwdKV2/mnp7aRJUToeJ6NElQnszy0tRbfv5EvXpdg+pgdGIkc9JWzcftYnt02nX9/dRobeyERzVKXUOdlg82RmKk0VuCfX21kT2cZH5yToqGkF98XbO2q4eU9E/jDjiq6lUt5LHvCQPtcowVw1iIQVMTybO4O85knF/DZ3giTazpIhqxitsgVtOTybNjbyD+snsu2HihPZqgPFQ1OHudOe5ksYyaOZ87iRUNeb8tA8NgGpO1wooDDVILyRIZHdqc4kL6eu6Y1UVXSyd6OWp54vYlXOi1CoTzViYGMynm2OIEgGcvxh/1hntt3Ew2xADeAXf0GhuWSCmcoG1jg9mbuKtMCOEfxQFksz6p2h4/97mrmVVtUJou9dl8OdrQbNGdcktEso0oH/PyTLrU3yOcLw17uzymUEaK4Zd48of9tIKiIF1jfa7DiuSko30JYHsmwS2XCPa3MzrkRAZSHfXzVz0FfIIDqpOKt9L21AM7FED/g51eGA3yVZl0neG0DJzoYELKgJjkweaXO7nYrYYOwTt1oFCRtSdLODc4dDKn3W2F0Aizz7bF7WAvgHAvBFJBwzve3KM5kmldnOUYavTWaS5hzMgIoBYUgGNJJCQGOaQ6m6txAknd9fD8ApTAtE8c2CdvDh/NAKnKej+8FBFIihMC2TUK2hWOeXLOBUrh+gO9LAimRA9OphmFgWsYJyxFHPh/Iox3twO+xTQPTKPrvri/Jex6+L4tlmwYhxyJsWydd9HxkGj/vBxRcnyA4WoZjW4Ttk6+bKbpTCi9QKKWOFirAMQ1MIQbHCi+QBFIN3gulFLZpYBnFNigM1EMqRcixiNgWvpT4R9JTb2yDgbK1AI4ZRpLHZBKKs5gC3wBfKtLZAgkhmFFXRllpAsMU9PXl2H+4m4M9GUpTMUwGNo3nXWTeY0JtKRVlSaLREJ7n09HZx762HvqUIhWLIEeIIgt+QLo/jyUVtakY5eVJEvEwkUgIIQS5XIHe3izNbT20FTzKUzEsY2hAKgFbCKIDE0HFpccghUAaBgWl6O/PkTBNZtVVkErFME2D/v48zS1dHOzNEEtECJlm0TDf2FZCkA8k6XSWmmiYmU3VxGJhTNMgk8lz8HA3+3sz4AcYJ5iakYBtGIRVgCEFUhUNXBgCTwh8jpwqJ4gBliEIBlJOlmniCoEroKs7Q3U0xOSGCsIhm86+LB3ZAmFEMR0p1GAbMFB2cBG5U2ctAC+QVKZifPUjizkyZSmA3v483/3FMvZ0prl26mjef8scpo6ppqqqBMs06erqZ0dzG48s28JjyzcRjkfI5VzGlif46F0LmTuumtqqMhLJCG7B49DhLjbsOMS/PvoqG/e3UVUSGyICPwiImyY3XzWNBbNG01iRpLYiSUlJlHgsgmEIMpk8HZ19bD/QyZMrt/PkitcxYmGitjVorP05l/nja/mzd12GsI1BQbd3pPnpk2tZtWU/N80Zx0dun8/0pmrKK0qwLYOe3iw79rby+IotPPTcRlTUIWyZQ8RlCEFfzkUVPO5eNIU7Fk9n2pgaEiVRLNOgry/L7v3tPP7iFn78i+VkXO+47Z7zAppKYnz0ltmMqisvCgDo68vx0yfXsqG5Ddu2qArZ/PEdCxg3umJQALZp8Njy1/nlU+u47eqpvPf6GUwaXUE0EuKxp9fxdw+v5J2XT+KdV04G4+gg0NOd5Z/+sIbdrT1EHEsLAEAGkkQsxD13Xz7kddfz+clDL3HXlVP56n+7mfGT64e8Xze6nOmzG7n+qqnk/zbP717extxRFXzrL9/NdddPH/Y9o8dVcfmiyYwbVc1n//9HOJTOEXeKuXbXDyiNhviTW+Zy7wevJVkZP259J1DPFcBdN83hK9/9HQ8+vR43buAYxWE9l3eprSnlnvdeMeRz3R19PPz0Oq6b2cQPP383k6c1DHm/lnKmzBjFzYunE+Q8fvnCZpzy+GBPaQhBb66AowT33nU5//NTN1FSmRxWxqTpo7jh6qnsae6g0Jc/fsfj+SRsi3veOZ+6xsqjHUHG57HVO8nvPIhhGIQU3HXbPMaOqx7y+deb27liQj0//sI91NaXDr6+dWcL+VyBGVNHc/fdC4emYLty/Gr5ZrwDHReNAM46CBYCgkCSzbpDXm9vTzN3Yj0/+PwfDTP+Yykpi/FXn7yJ+aMr+O7n7hrR+I/lhhum8fVPvQPTCwgGelfPD6hIxrj9+pknNP5jSZbH+NYX7uHeOy6jP53lyAJmwxC4rk++f+jveX1HC2OqS/nnr39wmPEfSzgR5hufu4vFs8fS3pMtuiVAzg9Qrs+n77iMb3zurmHGfyxOPMz3v/we7rp+2nEzOUIIfKXo7MkMbfeuNN6A+yQExWu6+4eV09eT4c8+dt0Q4wfI5l0MQ9CXySO9oUebHG7rwfMDLqZVM+dMxm9sk7LSGJ+99ybKq0vI9eVpbe+ltqaUUGx4jnDm1Hr+4cvvZ/bcMQB0tfaRzRVoaKhgpDPxFl81mTmjKnittZuIbSGEwPMDerNHJ452bz9Ma0cvfdkCQRCQiIaZPbORREn0qLHGQvzZRxbz+LJNHMr7J+zV6upK+Yt7b2bChBoA9uw4TDweobK2ZNi1NXWlfPjOBSx9bRe+VBhCkE1n+cgNs/jre29mpG1M3e1pOrvS1NWUEi2JMGFC3TlJTggx8pqhay+byPiB3zLEIKxi7CJGvMcXXyL1vI1jkYjDxIm1rHhpGw8+tobW1l4aG8p5/21zWHT5xGHXHjH+3z22hoee2Ugmk2f21Ab++K4FNI4dOnw7lsmcaaN5eX87EdtCKkUk7BA2TNau3cPDSzaydnMzLa3d9KTz+EFAJOzwrmun8z8+cR31x7gM5ckoiy+bzL8+89qgAIqnUg692WMaq4rC2tfBP/zf59my/TCWY/GhOxbw3tvmDhtLx9eUMLmmlD3ZHCAYnUrwibuuIJYaevSHn3P5xWNrePzFLaR7s5SVJ3jvzXO4/eaZnOuzAIcI4NqphMM2BIrX1u6mqzdDQ20Zfs4lHLLP+9KIi14AKFi/cT+ffuA3rDvQQThkk39lG69t3sevvv9JRo0qf0MwoVixaief/fbDHMgUsEyDP6zeRndnmh9+84NDLo0lwjQ2VuH5cjCoy3sev3ziVZau282qbQeIxMI4joVlGgjTpqvg8qPfrCAcMnng83cPlhUvjTF12igKj70MyegJf1JnRx+fe+BhfrNkHeGKBPm+HFv3HGZcVYp5C8YOdbESUaoqEuzYnSGbc3nXDROZNnWo65TPuPzk5y/wub9/nKySRKNhsn1ZlqzcBlJx+22zz9vtCYdtDh/s5J9+uYLHnt9Eb1+GMQ2VxKMOHmCbl8a02XmbCMtmC/z8tyvZ2tZLQ2WS8kSEyvIEB3uzPPHU2mHXd/Vk+O5PnqHDl9RXJKgujRFNRHjh9f1sXr9vaKVNg7LyGFIWBRCyLQ519PGTJ9ewuaWL2uoUkbCDkop8wSOdyVPIeXhd/SxZt5dCdmh2pb42iXHMisxi/nx4F/joko08snwT1U2VlCci1NWXsedwD4+u2Dzs2lgsRCweoeAFOBLmTm0gXhIZcs26DXv54S+X44dtRlWXUhoPM6q+nI6CywP/8hTbthwcqV85J0hf8t2fPsfX/s8zbGnvpRvByt2HWbK5mZ6si20ZegQ4GzK5Apu2HSQVDw8Op6YwQEoOtfcOu373gU6eWbuLstL44PVhxyKbzfPSKzuYNqtxyPXRaAh1zFJKJQRGyEHmCnR3ZahPRamoLCGZiFBSEqW0NE4yEWbelFHDhveoYxOzzeKE2XFcoP7uDC++upsgZGEPzB0YgG8JdhzsIp91CUePxjeOYxMOOXiuz9jyBNPeOOIBr2w/yN62HiprUoMpXakU1aVxXtnSzLOv7GTSlPoRg+CzZcum/Ty3dg+RZJTyRPEeRQYm4AoFT7tAZ0sgFb05d1gAJhXkXX/Y9S1tfeSFIDlCObkRrj+2XCEEWdcjnylwxaR6br1+FpPqUlSVxilJRkmlYlSUJzDDI/9cAVjmiWc3D7f10tLSRSwSGjQOBYRti/6+HF0d/dSNLjtmaBUYBuD71NekqKtODe3Jfcnug914DD8uywCUZXKoq/+83fhXN+2lu6ufWNi5ZIz9TY8BpJLDLU0NbAsdltcOjr88YYTsw5GbJoQgm/cwgHvfvYBP3XM5k6ePPt2qntQIlFKDk00jSeiIOzasTF9SkoxRmhqans1lXHp6slgD+waGuTmWQeD5580F6koXyBZ8jLDJpcx5nc04XtpspJeNM8x4uEGABXzsptl862/uxIzYQ7MsWZdtO1to70rT3NZHPBnhtmunEY6c3pJNYYjj1lGIE9VfYZomljnU0AoD66JO8LEzbpNTwVOSQEkMtAAuWBRQyHlcN7OJL/zZO4YYv+f6/OqRVSxdu5e9e1tp6+xjS3MHC2Y2sXjOuNMWwNnVc3hIHQlZGKaBlMfpOFwfJ3z+6ijOZUChBfDWEEhJwrG4bu4YUlVHo4dctsB//Hol//3vHiErBPF4GNsyUdEQJWUJYvHIm1fJgTU+Pb391DQcnXUNx8PU1ZTgj7Dex5OSqG0xtqpEW+h55sLOdfmSVCJMY83QAPNwSw8//90qgliYUdUpSqIhwraFLRUNqRihmP3m1dG2ONjSzcGW7mEtP72xgphlkvODo0scDEFrex83LpzETYsmaQvVAjjJDzAMbGvoQJZMRGioK6eQLeArhScV7d1pptaX8am7r3hT62faFns6+3j9QNew9264cjLvvnwKXYe66Mjkybg+B1q6KbEs/tv7rqKmrkxbqHaBTuzIBoGkkB/qRpRXJfnzjy5my95W1u8+jG0ZLJo8ii995p0sWDDuzW1gA3KB4pVNzWT6csSSR92vmroyvvZXtzN+XDXPrNxKpj/HDdfO4GPvvoxbF0/V1qkFcAr+dSbPgc7hE2uLFo7np9/4EKu3NOOYJgunjWbK9OIkmOv5hN6s5bwKSuNRVr+2i3Ub9rHoqslD3p44sYb7/+YO7n51Jr3pDLOmN5GqiA9+VqHQZ5ZdAAJQb4n9G6QLPkvX7eFjHWmSFUMfvDB7ThOz5zQN/t3W3sdvHlnNPXctoOoEy5HPNZGQxYG+LD/6xTImT6qjfITvnj1vzJC/n3huE+l0jvfcPv/tYy0X4YSZcS7axDAMom9IK5aVxTGP03OFQ8OD0GQiOuIWQgHER0hZliSiKKlwwjYrNh/g/h8/iZdxj1tPWfD55g/+i98s3URVRXJYzHBkWYWUCsc2CcWH1rE0FcMyjy6XGCxXKWzTpOwNE13lpXEc20QqhQHEEhF+v3I7X/z/HqH9YNcJ23T3thbue+DXbNjTNqz3T8TDg/uBTSFIvWEB35F6KjWwlZHh1wDEws5xZ/8CqYhGQhhv2JtcVh7HMo0R79MlOwIYQpDLuTz/3EYM0xh8rbM3Q86XQ2a9BMXdIc2Hu1mxfEtxwzvFvPdrG5ux7aHVEaL4OKxte1p56cUt+EExaW4KgzWb9hEO2ziWSbbg8W/PrCPn+XzgHbNZdOVkjhxwqdyAZSu38dAz6/iPx9cwc+polj6zAStsDWwON1m1eT9WOFQ8xdCx6ehKs2zJBgyneI1pGBxq6aY/72JbQ43CcSx6+jI88+x6SisSSCkxDIO+7n46u9OEHBsFOIZBrCTKQ8s2c7gvxz03zeKWKyZTccyGlJa9HTy9ahs/e/QVtjd30N3bz8svbsGVxQ36jmWyduM+sIoHBOT9gGUvbmHswU78oPi93R399PRlsG0LyxR4Al5YsZW21p7B9nNsi227WzCPs/k+7Njs2nOY5Us3ocziwwkMYdDW1kt/tjCsDS7oMPJsnxCjFJgGVITswSFSDGzA7vH84skCb+x9LJOEbQ0uHxBCUJCSroI3ZCP4kVOSE5ZJzLaOCsAwyAU+3QV/YOeTIO/5ZPrzTKkpY/EVk5g6thIFbNnZxtMrt7L1UCfJVIzSkE2JbaEEKFkUQMbz6fH8gZ4VIpZByjZRQqBkUQC+kqT9gJw3dLO6BCKGoMSxMIRBMCAApSRpL6Dfl4OHzQoh8JfUsHUAABDzSURBVKWkvTtDmWNx04KJLJjVSCoR5nBnhlfW72XpazvplYqKkigp26LEsfCkRA0IoN/16PMDfKmImCZJ28Q2jUEBKCXpcX3yQXGTvGUIkpZF2DKOEYBJb96l35cjnjAklaLEsYia5sCSjuKJGlJKejwfV6oLfg7tyBNizskjkpRSZI5dsDawEyniWCOebOAFkoIXDGl5yzCIOOaIT0Jx/aC49l8cfdEyDSLO0c3sgw+Dznv4eZfIwJEfeaUwHYtExCkaYCDJef7gTu8jR4QUyyrW2w/UkGuOLEsIWWZx0ZwaOkr5gSLv+cXXjzlCJGRb2ObwYxCPCDabc7GlxDEM8oHENw3i0RAhy0QAeS+g4AdDjzOxTMJW8biZQCryXlB0y4753vBA78/A2aN5zy9uiD+mnBMeDSMEBc8vHg1z7D01BBHbvCiORTmnj0gSQhAPnfrkkm0a2CM0/kiupQBClklohGH3WF/0iO0lwjYyZA2egJA0xKAIi+7MyHUdXOGpOO41I9XxyPWxU7x+0AAtEycRIZDFc31ihhiMmZQqLp1wLAPnOOvyi0eVCKLHyWYdXSzIaW9gV0rhWCbOcVydiykWviiPRjSEwDQFgYLOvINbsElG86RCPoF6+wzegqKLohflaAGcczwJCQvunH6AaTWdPLW9kRcPJYjagb7rmotfAK4yiJrwoal7WTRvHen+Mp7Ym9IC0Az1Fi7WH1aMCQVZH8gHFKRxPg9Z0GgBvD2RSoAyL+ltf5pLWAAajRaARvNWBcG+Ak8WnW/LAFucuS8iFRQGyrINhS3e3Jy0AAKgEAgUAlsobOPMauBJgTdw7HjIUCd9poDmAhKApyDtWuRdm6RhURf1EAja8iYHXUk07BJ3fOzjBKUZz6QnGyIacimPeGR8g+5cCEvZjI97CCE4lDFplQGpSIG4LU/bxxdCkXYtejNhIiGPyliBYEBcxsB7PZkwsbBLRdQl4xt05mzswKEpJglbkpasxUE/oCSSJ26rE1qwEOBK6C1YFAohKh2DUVGPvoLJ/rwAq0BZ2CNkKpTS0foFK4BCIBB+iFvrMyxs2s6s+u3URAsIoDUTYWPrWF7aPZYX9ifI2lli5tEN48UHnQiurElz64QdrDxYxq+3VjM2bPHH8/axaPwmxpV0o5TBgXSS1c2TeGLbaDb0GlRGvFN+AJ1S4Pkmi2p7+cC0zTzXXMFvt9YSCxU31mRci8vrevjQtIM8taeaX2yqYXREcM+0ThZP3MjkioPYQtCSjfHK3in8asMktvQpKuI5DDV8mYApFF0Fk4gKcdfoLFdNWMf0mj1UhX36PNjWMYoXd8zgiT0l9HgupWF53Menat6mAlADPXeJYfC1m9Zxx7TXKK3tgnySno4UlqmYOaGZm6Lr+ei+UTy1ZRzfeP4qWgN3UAQKQJrMrOji07csoeaFa9jZHuPv73mcyxo7wLLoPFhJIpxn5oyN3LZgLTesWcgXn72GNV0W5eHglEYCCbh+iMvrWvnwXf9F6TOX87N1TYMC6C84zKjq5MN3Pkn/b6/m0c1j+Jfbl3DTnDVgheg9XI5AMHXyDm6Yu5kbJ87me8vn8bsdVZTG88UlrEeCLKFozVnUhm2+cuV67lm4nHhlFrrLaO0sY0p9BwvnL+ND89fwn6tv4asvzmB/fx+VEaFFcCEJQEqDpqjPV69fy52XraK3o5q/f/xqXjrYRFcmBkJRm+zjhsbdfHDOVj50/bMov4QvLp1FwcgVT0EX4EtB2leg0kyo6OKHt3Vy2aRmfvHsIh7f00RbTxm25TGpooOPzFnP1Ze9ypddmz9+bDEFmT2uWzVS/C+FByKLFPnBdfeDi+o8E7IB0yu7+Md3v8RN817j6XXTeXjzBHZ1ViEMxehkF59c8BpXXvYqXzdgV+c72JQ2STlysKwe16Tacfj2Vet4321/oOdwFfc/dC2rD9WTzYeJhvMsqD3Ep69+iQ/e8igZN8YXl00k53cSMnWO4oIQgK8gbCr+fM5+7py/mq17x/HZx69nXUuSHunjmBIJBPsT/GHraF4+MJYf3fkkH77qWfo8gy8um4oVdgeMT5DxLQgMpjVuoZCL89mffojfbB/F4YIkanv40uHpPaVsPNjA98Qz3DhvJX96qJGvvzyammjhFINJhVQGBBDINyz6EopC4EBvOVdM2Mrl2Q5+/PQNfHP5bDpcH9N2UUqQ39PEzrY6/jnkMXnmau7e0ciqZ2eScjKDsZAThPn0ZZt437seZfO2cfzVb+5mWUscTxSI2AFZL8XTu6pY3zyO//2eB/norb9gXfvH+Ne1tVTGc3qV0IWQBlXSoCmR4cNXvIiXU3xv6dU8fzCCCGWpirqkQj5lIZ/KWB7XyvHLLTV8Z8k1EPJ575w1zC518QbcBqnADQywfYTy+dXaufzj+kZcI0d9PE8qFFAZ8SiPZ1jdHuLfXpkKCZfbpr5GpWninQO3wRCqKAppYcYyrGhu4OvPz6FLZamM5ygLBVSEfapL+lnZ5vCLdTPAMrimcQdTSgKyfvG3pD2T2ZV5/mTuBrx0gh8uuZmnmqMkY2lqYy4lTkBdrEBJLM3DuxL87OVbCYV83j1lOzWxGAVf+0BvewFIBBFLcU1dD9FYBy/tnMvjzQmqot6I7kjMkigkv3i9ge07Z1IaTnPjuA5c/w1VMRUtXWX8ev1kYtEs4WODZcA2QAqfzd0J/M46qqLdTEkVcOW5cRuUEmB59LWN4pfrZtPhFygPBUNiHluAsAqsOlhP245JzKrtYn5dDxmvOLBm8yHmNDRTOnEHqzaO5w+7aonGcgC4UuBJgStFcYmG08+Klol075vGzOo0E8sL5APd/7/9BSAhYknm1/eQ7Y/z0NqZZDnxgjPHkvT7Fs9sG40ZLTCtvqXY67/BDenzBNt7bCKmHLEc05RkvRDt7VVEHUl5zCWQ5zCstz32dyRZs7+SRNgbMfCP2gGtfRF2tJQSTqWpTaXxAgNXQVVIMK92P5iSpfsmcqArTlxGEYU4wo0N/qMQJypjtHWVsK87SW3JYWpj7biBrS307R4DKAS2GdBY0UOfa7L8YIywKU/qYuR9wZbOMIRyjCpvxmL46QcBUDhBatMAAmmQdm2qogrLOPcuQ14KCoHAPM4EniEU+UCQ9k1wXEJ2AaUMCr7BpESBpqQLGZP3zF7D5U2bjrsaNZDgWDYTa/YiLJtUuI8gKAU8baVv6yBYgWlISiJ5enMpWgqCk20OK04KCdrzFhg+caefuMmwtN/As5mRJ6/CkP+e0yFSgGmok5athhm0QWnUoyzqgW8wuSLL5Or8iWupJPhJMp3VFDwbw9BLty+ILJAY6I0LfpQCEDo13QwukTBQOAbkz+L73yqObMUUb1BB8UnukpJIjkJfFQ88ewsrD8cpCZ24RzdRBNJge3eE0oju/d/+AhAKqQSub5EId5EyTt5jo4qnlsQsCRi4QYh+H+zQxdOwpoCMa9LvGpQnTF7vKGHJ9hSRqHfykUooErZP2FJ6MuztHgQLwAtMWnqTlIT6mZr08U+yJEEiCBuSMQkXvBCt6RpySl1UOW/bkLT1h2jujmHV7GdG/X6wBOVRl/LISf6FPRxTG/8FIQCEwg0MtrSmKInmed/sPQSecULBuIHAthXXNXWhslG2toy+6PzdkCnZmzFZ114F4QLTK1qosAWuTm1eXAIwBGQ9k+f3pzAjHrdOXcvcsoC0Zwzr0cWA3x8EFtfW9nHV7BXkXYOlO2oImfKialhTQAGftYdG4x9o5IYxLcyuydDW7yBOsCRcoZdFX1gCADyl2NiZZP3WOdRU7eXzi9cwIWLRmrUHA12AtG/Qmwtza0M/X7/tCexQmt+tX8SLbSEc8+K77cmQz8vNFby0dRKp8bv44jUvMzNlcKgvSk4e7RQkxX0OnTmbzlyEQJoYWgYXThbINiQdeYv7X5jLz2tbWDxvGf8S7eL/vHIlv99azcF8cbvHzFKfD8zbyScWPUN1bTtPvHgLX10+E0/4RDiF4PkCI2JJ2tyArzw/nx+Vt7L4qmf4z1QXP1l1OQ9vHsXebNHIQ4agMSb50PRDTKxI89ttlaxpjxC3pbbQC0EAxYWUkuWH4tz7yM38r6vXMHf+auY2NXNfWwktmRJMIZlQ2kFVdRryUR78w+38eO0kWvKSEueYPLsShK0AUgUqYxlOlORUA9mkqlieVKK/uFzimGf4GgLKIgVIponZwdD9AkoQtz0ohbJoDnnMgjh5pA4laSpjmeJuLSVGdE4UxROYyyIFKO0j7gQwmN6FiOOxqi3G5x55B1/LlzB/zlr+95gdfPpAFQczSfJ+mNJwL1XRHA01HSAr2NJxC88dSBK3C9pCzzPn5GzQI/iymA6dUZFjZu1+xibSVEZ9DOUggVzgsj8TZXNrHZtay2h3JTFbDjErPzAYnczy7sl7aO2J88iOJjCCEWXgSUF5SPKO0W2E7SxL9zWxO2PgGApfCeImXF3XQVN5Ny83N7C6PUrEKn6fH5jMrujhmgn72NpSyX/tbiBmF/PuOd9gUmmOW8fv53BvhKf2jKJfKqwRfHdfQUQYXNPQyoTqbl7cU8/LrUniAzO+guKqUN9zGFfiMrfhAGNLOqmOBNjCRkkTZbgUAklrwWZ/X4qVB2rY3xcuilBzXjinh+Mei1TFjTG+7xC3FI6pMEVxy6IrTfKBwFUBUae4/W+kMDCQJn4hDGaA4+SLx6qfYBTwfBspDRynMOzsH9e3kIGFbblYVnDMdkOFH1goN4SyfEJOYfA9IRRBYOK5DhiyWC4jHwhb9OEVnmdDYGPahTd8z9A9Br5nEzUFIVNiGZIjy7J9KSgEFjkVEHUKRC15yjvcNGcugHO+JdIQkHAClJMjUOAObBEUAoTlE7EUsRPeV4FhSKxwdmCGVZzU/TItD5ORH8BtmT7K9DEEb9hrKzDNgCCcxWToe0oJhCGxQvnBOqgTZG0EAsvykdZI33PMwb12gLIDpAJPCdzB3xCABY5VICwGBKON/8KJAThOz1jc5aXO6LPmadz/E117otPgBut4DupgiFNLqR0p1xQ6y3NRpEE1Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQKPRAtBotAA0Gi0AjUYLQHPJC0DBYd0MmksRJVWPIRCtuik0l6QAoMdQSm7QTaG5RCWw1wikWq8bQnNp4u8VAHWLv9QlDFGqG0RzyfT9SnYcWvqNymIWSIjf6SbRXGL8HgbSoEoEX9PtobmkRgCh7h8UQMtzD+xTqAd1s2guCeNHPdjy3AP7BgUAkDPc+1D06ObRXOTW35Mz3PuO/DkogO4l3+lV8HHdQpqL2/75ePeS7/Qe+ds89s303mXbEmOuRcBi3VSai9D4v3po6df/+djXzDdelN6zbFlizDVNAjFbN5nmYvL7Dz13/39/4+vieB+oX/zlj2GgA2PNhY/k4wef//rPRnrLPN5n0nuXrY81XfO8gMVCiJRuRc0F1+srtVfCnS3P3//7411jnqiA/r0v7LPGX/GgrcwwiMt1k2ouIPP/fs5039/x3De3negqcarFVV39hZmWaX5GCD6iG1fz9u31+Q8/CH7Utvybp7TIU5z2Nyz+ilXr5et82ywzFaXCNGzd7Jq3zOAD6QWCbssLulrs8CGe/5p/Op//f7Zcg2N/Q7d1AAAAAElFTkSuQmCC";

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
            setTimer();
            uploadPhoto();
        }else{
            Toast.makeText(this,"No internet Access, Check your internet connection.",Toast.LENGTH_LONG).show();
        }
    }

    private void uploadPhoto(){
        showpDialog();
        //url = "http://belumjadi.com/test/test7.php";
        url = dbx.getUploadURL()+"/upload-image";
        getNtpTime();
        String device_model = dbx.deviceBrand()+" "+dbx.deviceModel();
        sToken = session.getKEY_Token();
        sUserid = session.getKEY_Userid();
        sDeviceid = session.getKEY_DeviceID();
        String encodedImageData =getEncoded64ImageStringFromBitmap(bitmap3);
        encodedImageData = encodedImageData.replace(" ", "");

        List<Map<String,String>> listMap =  new ArrayList<Map<String, String>>();

        Map<String, String> jsonParams = new HashMap<String, String>();
            jsonParams.put("userid", sUserid);
            jsonParams.put("nik", sNik);
            jsonParams.put("name", sName);
            jsonParams.put("deviceid", sDeviceid);
            jsonParams.put("device_model", device_model);
            jsonParams.put("glat", gLat);
            jsonParams.put("glong", gLong);
            jsonParams.put("radius", sDistance);
            jsonParams.put("addr", sAddress);
            jsonParams.put("fake", sFakeGPS);
            jsonParams.put("time", sNtpTime);
            jsonParams.put("token", sToken);
            jsonParams.put("img", encodedImageData);

        listMap.add(jsonParams);

        Log.d("upload", listMap.toString());

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
                        cancelTimer();
                        displayPrompt(msg);
                    }else{
                        hidepDialog();
                        cancelTimer();
                        displayPromptFailed(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    hidepDialog();
                    cancelTimer();
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
                    cancelTimer();
                    Toast.makeText(getApplicationContext(),
                            errStatus.toString(), Toast.LENGTH_LONG).show();
                }

                if (error.getClass().equals(TimeoutError.class)){
                    String errStatus = "Connection Timeout !! Please Try Again.";
                    hidepDialog();
                    cancelTimer();
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
        /*
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE).setTitleText("").setContentText(msg).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                ActivityMaps.aMaps.finish();
                Intent i = new Intent(ActivityAbsenPhoto.this,MenuActivity.class);
                startActivity(i);
                finish();
            }
        }).show();
        */

        new AwesomeSuccessDialog(this)
                .setTitle("")
                .setMessage(msg)
                .setColoredCircle(R.color.dialogSuccessBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                .setCancelable(false)
                .setPositiveButtonText(getString(R.string.dialog_yes_button))
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        ActivityMaps.aMaps.finish();
                        Intent i = new Intent(ActivityAbsenPhoto.this,MenuActivity.class);
                        startActivity(i);
                        finish();
                    }
                })
                .show();
    }

    private void displayPromptFailed(String msg){
        /*
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("").setContentText(msg).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
        */

        new AwesomeErrorDialog(this)
                .setTitle("")
                .setMessage(msg)
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(true).setButtonText(getString(R.string.dialog_ok_button))
                .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                .setButtonText(getString(R.string.dialog_ok_button))
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        // click
                    }
                })
                .show();

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

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteFormat = stream.toByteArray();
        String imgString = Base64.encodeToString(byteFormat, Base64.DEFAULT);
        return imgString;
    }

    private void getDeviceID(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSIONS_REQUEST_CODE);
            }else{
                sDeviceid = Build.getSerial();
            }
        }else{
            sDeviceid = Build.SERIAL;
        }
    }

    private void setTimer(){
        waitTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                queue.cancelAll(req);
                hidepDialog();
                Toast.makeText(ActivityAbsenPhoto.this,"Koneksi Internet Bermasalah, Harap coba kembali !!",Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void cancelTimer(){
        if(waitTimer != null) {
            waitTimer.cancel();
            waitTimer = null;
        }
    }

    @Override
    public void onUserLeaveHint() {
        //session.logoutUser();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            finishAndRemoveTask();
        }else{
            finish();
        }
    }
}
