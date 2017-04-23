package edu.neu.madcourse.priyankabh.note2map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Map;

import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapMainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;
    private EditText mLatitudeText;
    private EditText mLongitudeText;
    private String setLatitude = "";
    private String setLongitude = "";
    private TextView mCoordinatesText;
    private DatabaseReference mDatabase;
    private ValueEventListener valueEventListener;
    private Button setCoordinates;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayList<String> drawerList;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note2map_activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////Slide Menu///////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////
        mDrawerLayout = (DrawerLayout) findViewById(R.id.n2m_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.n2m_left_drawer);
        drawerList = new ArrayList<>();
        drawerList.add("Notes");
        drawerList.add("Friends");

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Notes");
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.n2m_drawer_list_item, drawerList));
        //onclick action
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                if(position == 1) {
                    Intent intent = new Intent(Note2MapMainActivity.this, Note2MapFriendActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    startActivity(intent);
                    Note2MapMainActivity.this.finish();
                }
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("Notes");
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getTitle());
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        //////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////


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
                    String token = FirebaseInstanceId.getInstance().getToken();
                    if (snapshot.exists()) {
                        currentUser = new User();
                        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                        for(Map.Entry<String,Object> entry: map.entrySet()){
                            if(entry.getKey().toString().equals("username")) {
                                currentUser.username = (String) entry.getValue();
                            } else{
                                currentUser.friends = (ArrayList<String>) entry.getValue();
                            }
                        }
                        startService(new Intent(Note2MapMainActivity.this,MyLocationService.class));

                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                        Intent alarmIntent = new Intent(Note2MapMainActivity.this, MyLocationService.class);
                        PendingIntent pending = PendingIntent.getService(Note2MapMainActivity.this, 0, alarmIntent, 0);
                        if (alarmManager!= null) {
                            alarmManager.cancel(pending);
                        }
                        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() +
                                        60 * 1000, 60 * 1000, pending);
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
                            String token = FirebaseInstanceId.getInstance().getToken();
                            if (snapshot.exists()) {
                                Log.d("Main Activity","User Exist");
                                currentUser = new User();
                                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                                for(Map.Entry<String,Object> entry: map.entrySet()){
                                    if(entry.getKey().toString().equals("username")) {
                                        currentUser.username = (String) entry.getValue();
                                    } else{
                                        currentUser.friends = (ArrayList<String>) entry.getValue();
                                    }
                                }
                                startService(new Intent(Note2MapMainActivity.this,MyLocationService.class));

                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                                Intent alarmIntent = new Intent(Note2MapMainActivity.this, MyLocationService.class);
                                PendingIntent pending = PendingIntent.getService(Note2MapMainActivity.this, 0, alarmIntent, 0);
                                if (alarmManager!= null) {
                                    alarmManager.cancel(pending);
                                }
                                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                        SystemClock.elapsedRealtime() +
                                                60 * 1000, 60 * 1000, pending);
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
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////Slide Menu///////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.add_friend);
//        item.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

}
