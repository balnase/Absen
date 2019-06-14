package com.jiuj.absen;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.jiuj.absen.Database.DatabaseHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ActivityAbsen extends AppCompatActivity {
    EditText edName;
    Button btnPhoto;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    ImageView img;
    String path;
    private File file = null;
    private byte[] byteImg = null;
    private Bitmap SaveGambar = null;
    private static final int CAMERA_REQUEST = 1888;
    protected static final String PHOTO_TAKEN = "photo_taken";
    String path2, sNoref, sTgl, sName;
    File file2;
    Bitmap bitmap3;
    TextView txtNik, txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_absen_new);
        getSupportActionBar().setTitle("Menu Absen");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //edName = (EditText) findViewById(R.id.edName);
        btnPhoto = (Button) findViewById(R.id.btnPhoto);
        img = (ImageView) findViewById(R.id.img);
        txtName = (TextView) findViewById(R.id.lbl_name);
        txtNik = (TextView) findViewById(R.id.lbl_nik);

        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();

        if (android.os.Build.VERSION.SDK_INT > 22) {
            StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(newbuilder.build());
        }

        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/absen_camera_rslt.jpg";
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();
            }
        });

        getUser();
    }

    @Override
    public void onBackPressed(){
        db.close();
        Intent i = new Intent(this,MainAbsen.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            db.close();
            Intent i = new Intent(this,MainAbsen.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
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

            //sName = edName.getText().toString();
            sNoref = strTgl;

            Matrix mat = new Matrix();
            mat.postRotate(angle);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            SaveGambar = BitmapFactory.decodeFile(path2, options);
            bitmap3 = Bitmap.createBitmap(SaveGambar, 0, 0, SaveGambar.getWidth(),
                    SaveGambar.getHeight(), mat, true);
            Bitmap bitmap2 = mark(bitmap3, "Noref : "+strTgl, "Nama : " + sName, "Tanggal : " + sTgl );
            FileOutputStream savebos=new FileOutputStream(file2);
            //FileOutputStream savebos = getActivity().getApplicationContext().openFileOutput("msurvey_camera_rslt.jpg", Context.MODE_PRIVATE);
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 50, savebos);
            //bitmap3.compress(Bitmap.CompressFormat.JPEG, 50, savebos);
            instream = new FileInputStream(file2);
            BufferedInputStream bif = new BufferedInputStream(instream);
            byteImg = new byte[bif.available()];
            bif.read(byteImg);

            String encodedImage = Base64.encodeToString(byteImg, Base64.DEFAULT);

            ContentValues values = new ContentValues();
            values.put("noref", sNoref);
            values.put("nama", sName);
            values.put("image", encodedImage);
            values.put("stsupload", "");
            values.put("uploadtime", "");
            values.put("createtime", sTgl2);
            db.insert("device_absen", null, values);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        img.setImageBitmap(bitmap3);
        Toast.makeText(this,"Photo Success !!", Toast.LENGTH_LONG).show();
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
        paint.setTextSize(13);
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

    private void getUser(){
        String selectQuery = "select userid, nama from device_login";
        Cursor csr = db.rawQuery(selectQuery, null);
        if (csr != null && csr.moveToFirst()) {
            txtNik.setText(csr.getString(0));
            txtName.setText(csr.getString(1));
        }
    }
}
