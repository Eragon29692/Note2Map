package edu.neu.madcourse.priyankabh.note2map;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimeZone;

import edu.neu.madcourse.priyankabh.note2map.models.Note;
import edu.neu.madcourse.priyankabh.note2map.models.NoteContent;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class MyLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MyLocationService";
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates = true;
    private DatabaseReference mDatabase;
    private ArrayList<Note> listOfNotes;
    NotificationManager notificationManager;

    private int timer = 3;

    public MyLocationService() {
        super("MyLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listOfNotes = new ArrayList<>();
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Do the task here
        createLocationRequest();

        //getting the list of notes:
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                listOfNotes = user.notes;
                if (listOfNotes == null) {
                    listOfNotes = new ArrayList<Note>();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

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
            //Log.d("locationchangeService", "Long: " + String.valueOf(mCurrentLocation.getLatitude()) + ", Lat: " + String.valueOf(mCurrentLocation.getLongitude()));
            //mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("coordinates").setValue(Integer.toString(timer) + ") Long: " + String.valueOf(mCurrentLocation.getLatitude()) + ", Lat: " + String.valueOf(mCurrentLocation.getLongitude()));
            //Log.d("note", Integer.toString(listOfNotes.size()));
            checkDistance();
            timer--;
            try {
                Thread.sleep(6000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } else {
            mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("notes").setValue(listOfNotes);
            //Log.d("StoppingService", "");
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

    private void checkDistance() {
        for (int i = 0; i < listOfNotes.size(); i++) {
            Note note = listOfNotes.get(i);
            if (listOfNotes.get(i).noteContents == null) {
                listOfNotes.get(i).noteContents = new ArrayList<>();
            }
            for (int k = 0; k < listOfNotes.get(i).noteContents.size(); k++) {
                NoteContent noteContent = listOfNotes.get(i).noteContents.get(k);
                if (noteContent.noteReceived == null) {
                    noteContent.noteReceived = "notReceived";
                }
                if (noteContent.noteReceived.equals("received")) {
                    Log.d("note", "note received");
                    continue;
                }
                if( !note.targetedUsers.contains(FirebaseInstanceId.getInstance().getToken())) {
                    Log.d("note", "you own this");
                    continue;
                }
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                df.setTimeZone(TimeZone.getTimeZone("EST"));
                Date startTime;
                try {
                    int duration = Integer.parseInt(note.getDuration().substring(0, note.duration.indexOf(" ")));
                    startTime = df.parse(note.getNoteDate() + " " + note.getStartTime().replaceAll(" ", ""));
                    Calendar cal;
                    cal = Calendar.getInstance();  // creates calendar
                    String month = new SimpleDateFormat("MM").format(cal.getTime()).toString();

                    String day = new SimpleDateFormat("dd").format(cal.getTime()).toString();

                    String year = new SimpleDateFormat("yy").format(cal.getTime()).toString();

                    String hour = new SimpleDateFormat("HH").format(cal.getTime()).toString();

                    String minute = new SimpleDateFormat("mm").format(cal.getTime()).toString();

                    Date currentTime = df.parse( month + "/" + day + "/" + year + " " + hour + ":" + minute);
                    cal.setTime(startTime); // sets calendar time/date
                    cal.add(Calendar.HOUR_OF_DAY, duration); // adds one hour
                    Date endTime = cal.getTime();
                    String newDateString1 = df.format(startTime);
                    String newDateString2 = df.format(endTime);
                    String newDateString3 = df.format(currentTime);
                    Log.d("startTime", newDateString1);
                    Log.d("endTime", newDateString2);
                    Log.d("currentTime", newDateString3);

                    if (currentTime.before(startTime) || currentTime.after(endTime)) {
                        Log.d("note", "too late");
                        continue;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String latLongString[] = noteContent.getNoteCoordinates().split(",");
                Double distance = distanceBetweenCoordinates(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), Double.parseDouble(latLongString[0]),Double.parseDouble(latLongString[1]));
                //Log.d("distance", Double.toString(distance));
                if (distance < 0.15) {
                    sendNotification(listOfNotes.get(i).owner, note.noteType + ": \nOn " + note.getNoteDate() + " at " + note.getStartTime() + "\n" + noteContent.getNoteText());
                    noteContent.noteReceived = "received";
                    listOfNotes.get(i).noteContents.get(k).noteReceived = "received";
                    listOfNotes.get(i).noteReceived = "received";
                }
            }
        }
        //mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).child("notes").setValue(listOfNotes);
    }

    private double distanceBetweenCoordinates(double lat1, double lon1, double lat2, double lon2) {
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

    public void sendNotification(String title, String message) {

        // prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(this, Note2MapMainActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    // build notification
    // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.add_icon)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setSound(soundUri).getNotification(); //This sets the sound to play


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(message.hashCode(), n);
        Log.d("notification", message);
    }
}