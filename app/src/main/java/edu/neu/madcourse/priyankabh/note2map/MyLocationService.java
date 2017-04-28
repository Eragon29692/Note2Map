package edu.neu.madcourse.priyankabh.note2map;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import edu.neu.madcourse.priyankabh.note2map.models.Note;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class MyLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MyLocationService";
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates = true;
    private DatabaseReference mDatabase;
    private ArrayList<Note> listOfNotes;

    private int timer = 3;

    public MyLocationService() {
        super("MyLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listOfNotes = new ArrayList<>();
        // Do the task here
        createLocationRequest();

        //getting the list of notes:
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                listOfNotes = user.notes;
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
        Log.d("note", listOfNotes.toString());
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


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}