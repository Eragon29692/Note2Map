package edu.neu.madcourse.priyankabh.note2map;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class MyLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MyLocationService";
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates = true;
    private DatabaseReference mDatabase;

    private int timer = 3;

    public MyLocationService() {
        super("MyLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Do the task here
        createLocationRequest();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();

        Log.i("MyTestService", "Service running");
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (timer > 0) {
            mCurrentLocation = location;
            Log.d("locationchangeService", "Long: " + String.valueOf(mCurrentLocation.getLatitude()) + ", Lat: " + String.valueOf(mCurrentLocation.getLongitude()));
            mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("coordinates").setValue(Integer.toString(timer) + ") Long: " + String.valueOf(mCurrentLocation.getLatitude()) + ", Lat: " + String.valueOf(mCurrentLocation.getLongitude()));
            timer--;
        } else {
            Log.d("StoppingService", "");
            stopLocationUpdates();
            stopSelf();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}