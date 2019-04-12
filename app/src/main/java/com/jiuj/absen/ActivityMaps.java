package com.jiuj.absen;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
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
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.inforoeste.mocklocationdetector.MockLocationDetector;
import com.instacart.library.truetime.TrueTime;
import com.jiuj.absen.Database.DatabaseHelper;
import com.jiuj.absen.MenuPage.MenuActivity;
import com.jiuj.absen.Receiver.ConnectivityReceiver;
import com.jiuj.absen.Receiver.NetworkChangeReceiver;
import com.jiuj.absen.Utils.InitTrueTimeAsyncTask;
import com.jiuj.absen.Utils.PrefManager;
import com.jiuj.absen.sample.SampleActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActivityMaps extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = ActivityMaps.class.getName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    static final int REQUEST_LOCATION = 199;
    private GoogleMap map;
    public static Activity aMaps;
    double latti = 0;
    double longi = 0;
    int i1 = 0;
    int i2 = 0;
    boolean isMock;
    NetworkChangeReceiver myReceiver;
    TextClock hk_Time, hk_Day;
    RequestQueue queue;
    JsonObjectRequest req;
    LocationManager locationManager;
    RelativeLayout rlClock, rlInfo;
    Geocoder geocoder;
    ProgressBar progressBar;
    List<Address> addresses;
    TextView txtName, txtNik, txtLoc, txtAddr, ntpTime;
    Button btnAbsen;
    SQLiteDatabase db;
    DatabaseHelper dbx;
    CameraPosition cameraPosition;
    LatLng latLng2,latLng;
    String sAddress = "";
    String sName = "";
    String sNik = "";
    String sDistance = "";
    String sTime = "";
    String strDevice = "";
    String sFakeGPS = "";
    String url = "";
    String status = "";
    String msg = "";
    String pageState = "";
    String strName = "";
    String sNtpTime = "";
    String sToken ="";
    String sUserid ="";
    String sDevice_model ="";
    String statusAtt = "";
    String sTimeZone = "";
    ImageButton imgAbsen;
    PrefManager session;
    View mapView;
    //SweetAlertDialog pDialog;
    private ProgressDialog pDialog;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test3);
        aMaps = this;
        getSupportActionBar().setTitle("Attendance");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rlClock = (RelativeLayout) findViewById(R.id.rlClock);
        rlInfo = (RelativeLayout) findViewById(R.id.info2);
        txtName = (TextView) findViewById(R.id.txtTitle);
        txtNik = (TextView) findViewById(R.id.txtSub);
        txtLoc = (TextView) findViewById(R.id.txtLocation);
        txtAddr = (TextView) findViewById(R.id.txtAddress);
        hk_Day = (TextClock) findViewById(R.id.hk_day);
        hk_Time = (TextClock) findViewById(R.id.hk_time);
        //btnAbsen = (Button) findViewById(R.id.btnAbsen);
        imgAbsen = (ImageButton) findViewById(R.id.imgAbsen);
        ntpTime = (TextView) findViewById(R.id.ntpTime);
        progressBar = (ProgressBar) findViewById(R.id.progBar);
        progressBar.setVisibility(View.GONE);
        ntpTime.setVisibility(View.GONE);
        rlInfo.setVisibility(View.GONE);
        rlClock.setVisibility(View.GONE);
        imgAbsen.setVisibility(View.GONE);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        dbx = new DatabaseHelper(this);
        db = dbx.getWritableDatabase();
        session = new PrefManager(this);
        myReceiver= new NetworkChangeReceiver();
        Random r = new Random();
        i1 = r.nextInt(5);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        //locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        imgAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayPromptWarning("Anda ingin melakukan absen ?");
            }
        });
        getDeviceID();
        //checkConnection();
        init();
        initTrueTime(this);
        final LocationManager manager = (LocationManager) ActivityMaps.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(ActivityMaps.this)) {}
        if(!hasGPSDevice(ActivityMaps.this)){}
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(ActivityMaps.this)) {
            enableLoc();
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        /*
        SimpleDateFormat fmt = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ssZ");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        */

        session.createAcvtivity(TAG);
        sToken = session.getKEY_Token();
        checkTime();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    private void setupMap(double lat, double lng){
        latLng = new LatLng(Double.parseDouble(session.getKEY_LAT()), Double.parseDouble(session.getKEY_LONG()));
        drawCircle(latLng);
        latLng2 = new LatLng(lat, lng);
        cameraPosition = new CameraPosition.Builder()
                .target(latLng2)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(17)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng2));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }else {
            map.setMyLocationEnabled(true);
            setMapBtn();
        }
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.addMarker(new MarkerOptions().position(latLng2).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.locator)));
        map.addMarker(new MarkerOptions().position(latLng).title("OFFICE"));
        showButton();
    }

    private void setupMap2(double lat, double lng){
        latLng = new LatLng(Double.parseDouble(session.getKEY_LAT()), Double.parseDouble(session.getKEY_LONG()));
        drawCircle(latLng);
        latLng2 = new LatLng(lat, lng);
        cameraPosition = new CameraPosition.Builder()
                .target(latLng2)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(10)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng2));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }else {
            map.setMyLocationEnabled(true);
            setMapBtn();
        }
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.addMarker(new MarkerOptions().position(latLng2).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.locator)));
        map.addMarker(new MarkerOptions().position(latLng).title("OFFICE"));
        showButton();
    }

    private void calDistance(){
        getAddress();
        Location mylocation = new Location("");
        Location dest_location = new Location("");
        //String lat = "-6.165038";
        //String lon = "106.817275";
        String lat = session.getKEY_LAT();
        String lon = session.getKEY_LONG();
        dest_location.setLatitude(Double.parseDouble(lat));
        dest_location.setLongitude(Double.parseDouble(lon));
        mylocation.setLatitude(latti);
        mylocation.setLongitude(longi);
        Float distance = mylocation.distanceTo(dest_location);//in meters
        //Float init = 100.0f;
        Float init = Float.parseFloat(session.getKEY_Radius());
        sDistance = String.format("%.0f",distance);
        int retval = Float.compare(distance, init);
        if(retval > 0) {
            //System.out.println("f1 is greater than f2");
            setupMap2(latti,longi);
            imgAbsen.setEnabled(false);
            imgAbsen.setAlpha(.4f);
        }else{
            setupMap(latti,longi);
            showButton();
        }
        txtLoc.setText(sDistance+" meters from Office");
        txtAddr.setText(sAddress);
        getUserPref();
    }

    private void getUser(){
        String selectQuery = "select userid, nama from device_login";
        Cursor csr = db.rawQuery(selectQuery, null);
        if (csr != null && csr.moveToFirst()) {
            sName = csr.getString(1);
            sNik = csr.getString(0);
            txtNik.setText(csr.getString(0));
            txtName.setText(csr.getString(1)+" ( "+strDevice+" ) ");
        }
    }

    @Override
    public void onBackPressed(){
        db.close();
            /*
            if("Loading".equalsIgnoreCase(pageState)){
            }else{
            }
            */
        Intent i = new Intent(this,MenuActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            db.close();
            /*
            if("Loading".equalsIgnoreCase(pageState)){
            }else{
            }
            */
            Intent i = new Intent(this,MenuActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }
    */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                showSnackbar(R.string.textwarn, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(ActivityMaps.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.textwarn, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLocationPermissionRequest();
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            startLocationPermissionRequest();
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }else {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();
                                latti = mLastLocation.getLatitude();
                                longi = mLastLocation.getLongitude();
                                checkFakeGPS();
                            } else {
                                Log.w(TAG, "getLastLocation:exception", task.getException());
                                enableLoc();
                            }
                        }
                    });
        }
    }

    private void checkFakeGPS(){
        isMock = MockLocationDetector.isLocationFromMockProvider(this, mLastLocation);
        if(isMock==true){
            sFakeGPS = "FAKE";
            displayPrompt("Fake GPS Detected !!");
            //Toast.makeText(ActivityMaps.this, "Fake GPS Detected !!", Toast.LENGTH_SHORT).show();
            imgAbsen.setEnabled(false);
            imgAbsen.setAlpha(0.5f);
            calDistance();
        }else{
            sFakeGPS = "CLEAR";
            initMap();
            calDistance();
        }
    }

    private void displayPrompt(String msg){
        new AwesomeErrorDialog(this)
                .setMessage(msg)
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(false)
                .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                .setButtonText(getString(R.string.dialog_ok_button))
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        finish();
                    }
                })
                .show();

    }

    private void init(){
        txtName.setText("");
        txtNik.setText("");
        txtLoc.setText("");
        txtAddr.setText("");
    }

    private void showButton(){
        imgAbsen.setAlpha(1f);
        imgAbsen.setEnabled(true);
    }

    private void getAddress() {
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latti, longi, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        sAddress = address;
    }

    private void goToCamera(){
        session.createAcvtivity("ActivityAbsenPhoto");
        sTime = dbx.getDateTime();
        Intent i = new Intent(ActivityMaps.this, ActivityAbsenPhoto.class);
        i.putExtra("namex", sName);
        i.putExtra("nikx", sNik);
        i.putExtra("glat", Double.toString(latti));
        i.putExtra("glong", Double.toString(longi));
        i.putExtra("addrx", sAddress);
        i.putExtra("radiusx", session.getKEY_Radius());
        i.putExtra("distancex", sDistance);
        i.putExtra("timex", sTime);
        i.putExtra("fake", sFakeGPS);
        i.putExtra("status", statusAtt);
        i.putExtra("token", sToken);
        startActivity(i);
        //finish();
    }

    private void getDeviceID(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSIONS_REQUEST_CODE);
            }else{
                strDevice = Build.getSerial();
            }
        }else{
            strDevice = Build.SERIAL;
        }
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

    public static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService (Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    private void showNtpTime(){
        getTrueTime().toString();
        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();
        TimeZone tz = TimeZone.getDefault();
        sTimeZone = TimeZone.getTimeZone(tz.getDisplayName(false, TimeZone.SHORT)).toString();
        String zzz = _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone(tz.getDisplayName(false, TimeZone.SHORT)));
        //ntpTime.setText(_formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+07:00")));
        //ntpTime.setText(zzz);
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    private void UpdateTime(){
        if (!TrueTime.isInitialized()) {
            Toast.makeText(ActivityMaps.this, "Sorry TrueTime not yet initialized. Trying again.", Toast.LENGTH_SHORT).show();
            return;
        }
        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();
        ntpTime.setText(_formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+07:00")));
    }

    private void drawCircle(LatLng point){
        map.addCircle(new CircleOptions()
                .center(point)
                .radius(Double.parseDouble(session.getKEY_Radius()))
                .strokeWidth(0f)
                .fillColor(0x550000FF));
    }

    private void getUserPref(){
        sName = session.getKEY_Name();
        sNik = session.getKEY_NIK();
        sUserid = session.getKEY_Userid();
        sDevice_model = dbx.deviceModel();
        txtNik.setText(session.getKEY_NIK());
        txtName.setText(session.getKEY_Name()+" ( "+strDevice+" ) ");
    }

    /*
    public void onPause(){
        super.onPause();
        if("Loading".equalsIgnoreCase(pageState)){
        }else{
            queue.cancelAll(req);
        }
    }
    */


    public void GPSCheck(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showSettingsAlert();
        }
        else
        {
            checkAccuracy();
        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS OFF");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Aplikasi ini membutuhkan GPS ON");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent localIntent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
                        startActivity(localIntent);
                        finish();
                    }
                }
        );
        alertDialog.show();
    }

    public static int getLocationMode(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(locationProviders)) {
                locationMode = Settings.Secure.LOCATION_MODE_OFF;
            } else if (locationProviders.contains(LocationManager.GPS_PROVIDER) && locationProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                locationMode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
            } else if (locationProviders.contains(LocationManager.GPS_PROVIDER)) {
                locationMode = Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
            } else if (locationProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                locationMode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
            }
        }
        return locationMode;
    }

    private void checkAccuracy(){
        if(getLocationMode(this) == 3)
        {
            getLastLocation();
        }else {
            displayPromptGPS("GPS tidak dalam mode High Accuracy !");
        }
    }

    private void displayPromptGPS(String msg){
        new AwesomeErrorDialog(this)
                .setMessage(msg)
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(false)
                .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                .setButtonText(getString(R.string.dialog_ok_button))
                .setErrorButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        finish();
                    }
                })
                .show();

    }

    private void initMap(){
        rlInfo.setVisibility(View.VISIBLE);
        rlClock.setVisibility(View.VISIBLE);
        imgAbsen.setVisibility(View.VISIBLE);
    }

    private void setMapBtn(){
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 150);
        }
    }

    private void doRandom(){
        Random r2 = new Random();
        i2 = r2.nextInt(5);
        Log.d("nilai",String.valueOf(i2)+"/"+String.valueOf(i1));
        if(i1==i2){
            goToCamera();
        }else{
            doCheckIn();
            //goToCamera();
        }
    }

    private void showProgBar(){
        progressBar.setVisibility(View.VISIBLE);
        imgAbsen.setVisibility(View.GONE);
    }

    private void hideProgBar(){
        progressBar.setVisibility(View.GONE);
        imgAbsen.setVisibility(View.VISIBLE);
    }

    private void doCheckIn(){
        showDialog();
        pageState = "Loading";
        //url = "http://belumjadi.com/test/test6.php";
        //url = "http://192.168.2.34:81/api/upload";
        url = dbx.getUploadURL()+"/upload";
        String device_model = dbx.deviceBrand()+" "+dbx.deviceModel();
        Log.d("debugtest2",url);
        getTrueTime().toString();
        Date trueTime = TrueTime.now();
        TimeZone tz = TimeZone.getDefault();
        sNtpTime = _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone(tz.getDisplayName(false, TimeZone.SHORT)));
        Map<String, String> jsonParams = new HashMap<String, String>();
        jsonParams.put("userid", sUserid);
        jsonParams.put("deviceid", strDevice);
        jsonParams.put("device_model", device_model);
        jsonParams.put("img", "");
        jsonParams.put("glat", Double.toString(latti));
        jsonParams.put("glong", Double.toString(longi));
        jsonParams.put("radius", sDistance);
        jsonParams.put("addr", sAddress);
        jsonParams.put("fake", sFakeGPS);
        jsonParams.put("status", statusAtt);
        jsonParams.put("token", sToken);

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
                        hideDialog();
                        //strName = response.getString("name");
                        //sNik = response.getString("nik");
                        displayPromptSuccess(msg);
                    }else{
                        hideDialog();
                        displayPrompt(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                Log.d("debugtest",status);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getClass().equals(NoConnectionError.class)){
                    hideDialog();
                    String errStatus = "No internet Access, Check your internet connection.";
                    dialogError(errStatus);
                }
                if (error.getClass().equals(TimeoutError.class)){
                    hideDialog();
                    String errStatus = "Connection Timeout !! Please Try Again.";
                    dialogError(errStatus);
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

    private void showDialog(){
        /*
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        */
        if (!pDialog.isShowing())
            pDialog.show();
    }

    public void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(ActivityMaps.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                        }
                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();
            showSettingLocation();
        }else{
            showSettingLocation();
        }
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                /*
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLastLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        enableLoc();//keep asking if imp or do whatever
                        break;
                }
                return;
                */
                if (resultCode == RESULT_OK) {
                    getLastLocation();
                } else {
                    enableLoc();
                }
        }
    }

    private void showSettingLocation(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(ActivityMaps.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void hideDialog(){
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void dialogError(String msg){
        new AwesomeInfoDialog(this)
                .setTitle("Internet Error")
                .setMessage(msg)
                .setColoredCircle(R.color.dialogErrorBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                .setCancelable(false)
                .setPositiveButtonText("RETRY")
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText(getString(R.string.dialog_no_button))
                .setNegativeButtonbackgroundColor(R.color.colorAccent)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        doCheckIn();
                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {
                    }
                })
                .show();
    }

    private void displayPromptSuccess(String msg){
        new AwesomeSuccessDialog(this)
                .setTitle("")
                .setMessage(msg)
                .setColoredCircle(R.color.dialogSuccessBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_success, R.color.white)
                .setCancelable(false)
                .setPositiveButtonText(getString(R.string.dialog_yes_button))
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        //click
                    }
                })
                .show();

    }

    private void displayPromptWarning(String msg){
        /*
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Check IN");
        alertDialog.setCancelable(false);
        alertDialog.setMessage(msg);
        alertDialog.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        alertDialog.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doRandom();
                    }
                }
        );
        alertDialog.show();
        */

        new AwesomeInfoDialog(this)
                .setTitle(statusAtt)
                .setMessage(msg)
                .setColoredCircle(R.color.dialogWarningBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_warning, R.color.white)
                .setCancelable(false)
                .setPositiveButtonText(getString(R.string.dialog_yes_button))
                .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText(getString(R.string.dialog_no_button))
                .setNegativeButtonbackgroundColor(R.color.colorAccent)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        doRandom();
                    }
                })
                .setNegativeButtonClick(new Closure() {
                    @Override
                    public void exec() {
                    }
                })
                .show();
    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        displayPromptInternet(isConnected);
    }

    private void displayPromptInternet(boolean isConnected){
        String msg;
        if (isConnected) {
            msg = "Good! Connected to Internet";
        } else {
            msg = "Sorry! Not connected to internet";
        }
        /*
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE).setTitleText("");
        sweetAlertDialog.setContentText(msg);
        sweetAlertDialog.setConfirmText("OK");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        */

        new AwesomeWarningDialog(this)
                .setTitle("")
                .setMessage(msg)
                .setColoredCircle(R.color.dialogWarningBackgroundColor)
                .setDialogIconAndColor(R.drawable.ic_dialog_warning, R.color.white)
                .setCancelable(false)
                .setButtonText(getString(R.string.dialog_ok_button))
                .setButtonBackgroundColor(R.color.dialogNoticeBackgroundColor)
                .setButtonText(getString(R.string.dialog_ok_button))
                .setWarningButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        // click
                    }
                })
                .show();
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


    @Override
    public void onUserLeaveHint() {
        String activ = session.getKEY_Activity();
        if("ActivityAbsenPhoto".equalsIgnoreCase(activ)){

        }else{
            //session.logoutUser();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                finishAndRemoveTask();
            }else{
                finish();
            }
        }
    }


    private void checkTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);
        String[] sDate = formattedDate.split(" ");
        String checkin = sDate[0]+" 08:00:00";
        String checkout = sDate[0]+" 17:00:00";
        Date date1 = new Date();
        Date date2 = new Date();
        Log.d("tanggal",checkin+" / "+checkout);
        try {
            date1 = input.parse(checkin);
            date2 = input.parse(checkout);  // parse input
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(c.after(date1)){
            if(c.before(date2)){
                statusAtt = "CHECK IN";
                ntpTime.setVisibility(View.VISIBLE);
                ntpTime.setText(statusAtt);
                ntpTime.setTextColor(getResources().getColor(R.color.green));
            }else{
                ntpTime.setVisibility(View.VISIBLE);
                statusAtt = "CHECK OUT";
                ntpTime.setText(statusAtt);
                ntpTime.setTextColor(getResources().getColor(R.color.red));
            }
        }else{
            statusAtt = "CHECK IN";
            ntpTime.setText(statusAtt);
            ntpTime.setVisibility(View.VISIBLE);
            //ntpTime.setVisibility(View.GONE);
        }
    }

    private void setupClock(){
        hk_Time.setFormat24Hour("HH:mm:ss");
        hk_Time.setFormat12Hour("HH:mm:ss");
        hk_Day.setFormat24Hour("EEEE,  dd MMMM yyyy");
        hk_Day.setFormat12Hour("EEEE,  dd MMMM yyyy");
    }
}
