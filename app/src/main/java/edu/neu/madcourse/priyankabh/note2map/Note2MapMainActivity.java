package edu.neu.madcourse.priyankabh.note2map;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapMainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;
    private DatabaseReference mDatabase;
    private User currentUser;
    private Dialog dialog;
    private BroadcastReceiver mybroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note2map_activity_main);

        mybroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(Note2MapDetectNetworkActivity.IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";
                Log.d("networkStatus:Notes",networkStatus);
                if(networkStatus.equals("connected")){
                    if(dialog!=null && dialog.isShowing()){
                        dialog.cancel();
                        dialog.dismiss();
                        dialog.hide();
                        Intent intentAgain = new Intent(Note2MapMainActivity.this, Note2MapMainActivity.class);
                        startActivity(intentAgain);
                        Note2MapMainActivity.this.finish();
                    }
                } else {
                    if(dialog == null){
                        dialog = new Dialog(Note2MapMainActivity.this);
                        dialog.setContentView(R.layout.internet_connectivity);
                        dialog.setCancelable(false);
                        TextView text = (TextView) dialog.findViewById(R.id.internet_connection);
                        text.setText("Internet Disconnected");
                        dialog.show();
                    } else if(dialog != null && !dialog.isShowing()){
                        dialog.show();
                    }
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            /////Creating/checking user
            mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        currentUser = snapshot.getValue(User.class);
                        startService(new Intent(Note2MapMainActivity.this,MyLocationService.class));

                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                        Intent alarmIntent = new Intent(Note2MapMainActivity.this, MyLocationService.class);
                        PendingIntent pending = PendingIntent.getService(Note2MapMainActivity.this, 0, alarmIntent, 0);
                        if (alarmManager!= null) {
                            alarmManager.cancel(pending);
                        }
                        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() +
                                        60 * 1000, 60 * 1000, pending);
                        Intent intent = new Intent(Note2MapMainActivity.this, Note2MapNotesActivity.class);
                        intent.putExtra("currentUser", currentUser);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Note2MapMainActivity.this, Note2MapChooseUsername.class);
                        startActivity(intent);
                        Note2MapMainActivity.this.finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Note2MapDetectNetworkActivity.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mybroadcast, intentFilter);
        if (!isNetworkAvailable(getApplicationContext())) {
            if(dialog == null){
                dialog = new Dialog(Note2MapMainActivity.this);
                dialog.setContentView(R.layout.internet_connectivity);
                dialog.setCancelable(false);
                TextView text = (TextView) dialog.findViewById(R.id.internet_connection);
                text.setText("Internet Disconnected");
                dialog.show();
            } else if(dialog != null && !dialog.isShowing()){
                dialog.show();
            }
        }
    }

    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mybroadcast);
    }

    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connec.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;

            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        } else {
            // not connected to the internet
            return false;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    /////Creating/checking user
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.d("Main Activity","User Exist");
                                currentUser = snapshot.getValue(User.class);
                                startService(new Intent(Note2MapMainActivity.this,MyLocationService.class));

                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                Intent alarmIntent = new Intent(Note2MapMainActivity.this, MyLocationService.class);
                                PendingIntent pending = PendingIntent.getService(Note2MapMainActivity.this, 0, alarmIntent, 0);
                                if (alarmManager!= null) {
                                    alarmManager.cancel(pending);
                                }
                                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                        SystemClock.elapsedRealtime() +
                                                60 * 1000, 60 * 1000, pending);
                                Intent intent = new Intent(Note2MapMainActivity.this, Note2MapNotesActivity.class);
                                intent.putExtra("currentUser", currentUser);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Note2MapMainActivity.this, Note2MapChooseUsername.class);
                                startActivity(intent);
                                Note2MapMainActivity.this.finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                        }
                    });

                } else {
                    finish();
                    System.exit(0);
                }
            }
        }
    }
}
