package com.jiuj.absen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.inforoeste.mocklocationdetector.MockLocationDetector;

import java.text.DateFormat;
import java.util.Date;

public class ActivityMock extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mLastUpdateTimeTextView;
    private TextView mIsMockTextView;
    private TextView mAreMockLocationAppsPresentTextView;
    private TextView mIsMockLocationsOnTextView;
    private TextView txtDistance;
    private TextView txtModel;
    private TextView txtSN;
    private TextView txtDevice;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock);

        mLatitudeTextView = (TextView) findViewById(R.id.txt_location_latitude);
        mLongitudeTextView = (TextView) findViewById(R.id.txt_location_longitude);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.txt_location_last_update_time);
        mIsMockTextView = (TextView) findViewById(R.id.txt_is_mock_text);
        mAreMockLocationAppsPresentTextView = (TextView) findViewById(R.id.txt_are_mock_location_apps_present);
        mIsMockLocationsOnTextView = (TextView) findViewById(R.id.txt_is_allow_mock_locations_on);
        txtDistance = (TextView) findViewById(R.id.txt_distance);
        txtDevice = (TextView) findViewById(R.id.txt_device);
        txtModel = (TextView) findViewById(R.id.txt_model);
        txtSN = (TextView) findViewById(R.id.txt_sn);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Log.i("TAG","MODEL: " + Build.MODEL);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }else{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        Location mylocation = new Location("");
        Location dest_location = new Location("");
        String lat = "-6.153993";
        String lon = "106.914143";
        dest_location.setLatitude(Double.parseDouble(lat));
        dest_location.setLongitude(Double.parseDouble(lon));

        mylocation.setLatitude(mCurrentLocation.getLatitude());
        mylocation.setLongitude(mCurrentLocation.getLongitude());

        Float distance = mylocation.distanceTo(dest_location);//in meters

        String sDistance = String.format("%.0f",distance);

        //txtDistance.setText(Double.toString(Math.round(distance)));
        txtDistance.setText(sDistance);

        txtDevice.setText(Build.BRAND);
        txtModel.setText(Build.MODEL);
        txtSN.setText(Build.SERIAL);

        mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));

        mLastUpdateTimeTextView.setText(mLastUpdateTime);
        boolean isMock = MockLocationDetector.isLocationFromMockProvider(this, mCurrentLocation);
        mIsMockTextView.setText(String.valueOf(isMock));
        if (isMock) {
            mIsMockTextView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            mIsMockTextView.setTextColor(ContextCompat.getColor(this, R.color.blue));
        }
        boolean mockLocationAppsPresent = MockLocationDetector.checkForAllowMockLocationsApps(this);
        mAreMockLocationAppsPresentTextView.setText(String.valueOf(mockLocationAppsPresent));
        if (mockLocationAppsPresent) {
            mAreMockLocationAppsPresentTextView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            mAreMockLocationAppsPresentTextView.setTextColor(ContextCompat.getColor(this, R.color.blue));
        }
        boolean isAllowMockLocationsON = MockLocationDetector.isAllowMockLocationsOn(this);
        mIsMockLocationsOnTextView.setText(String.valueOf(isAllowMockLocationsON));
        if (isAllowMockLocationsON) {
            mIsMockLocationsOnTextView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            mIsMockLocationsOnTextView.setTextColor(ContextCompat.getColor(this, R.color.blue));
        }
    }

    public static int math(float f) {
        int c = (int) ((f) + 0.5f);
        float n = f + 0.5f;
        return (n - c) % 2 == 0 ? (int) f : c;
    }
}
