package edu.neu.madcourse.priyankabh.note2map;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import edu.neu.madcourse.priyankabh.note2map.models.Note;
import edu.neu.madcourse.priyankabh.note2map.models.NoteContent;
import edu.neu.madcourse.priyankabh.note2map.models.User;

import static edu.neu.madcourse.priyankabh.note2map.Note2MapChooseNoteType.NOTE_TYPE;
import static edu.neu.madcourse.priyankabh.note2map.Note2MapMainActivity.isNetworkAvailable;
import static edu.neu.madcourse.priyankabh.note2map.SelectEventTimeActivity.NOTE_TIME;

public class Note2MapSearchLocationActivity extends AppCompatActivity implements OnItemClickListener,OnMapReadyCallback,GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    GoogleMap googleMap;
    private static final String LOG_TAG = "GoogleAutocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyBWHu2v4RKujUpKxPH53UvkIVdhDrnolCU";
    Marker locationMarker;
    private ArrayList<Marker> listofLocationMarker;
    private AutoCompleteTextView autoCompView;
    private String noteType;
    private String noteTime;
    private String preEditText;
    private EditText editTextView;
    private User currentUser;
    private NoteContent noteContent;
    private DatabaseReference mDatabase;
    private ArrayList<NoteContent> listofNoteContents;
    private ArrayList<String> listOftargetedUsers;
    private String location;
    private String targetedUsersExtra;
    private String tapNote;
    int counterI;
    private String viewLocationExtra;
    private Note receivedNote;
    private Dialog dialog;
    private BroadcastReceiver mybroadcast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_search_location_activity);

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
                    }
                } else {
                    if(dialog == null){
                        dialog = new Dialog(Note2MapSearchLocationActivity.this);
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

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_search_location);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tapNote = getIntent().getStringExtra("tapOnNote");
        targetedUsersExtra = getIntent().getStringExtra("friends");
        noteType = getIntent().getStringExtra(NOTE_TYPE);
        noteTime = getIntent().getStringExtra(NOTE_TIME);
        currentUser = (User) getIntent().getSerializableExtra("currentUser");
        viewLocationExtra = getIntent().getStringExtra("viewLocation");
        receivedNote = (Note) getIntent().getSerializableExtra("chosenNote");

        if(viewLocationExtra == null){
            viewLocationExtra ="";
        }

        if (currentUser.notes == null) {
            currentUser.notes = new ArrayList<>();
        }

        if(tapNote == null){
            tapNote = "false";
        }

        if (tapNote.equals("true")) {
            getSupportActionBar().setTitle("Note's Details");
        } else {
            getSupportActionBar().setTitle("Set Up Location");
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.n2m_show_map);
        autoCompView = (AutoCompleteTextView) findViewById(R.id.n2m_autoCompleteTextView);

        // Getting a reference to the map
        supportMapFragment.getMapAsync(this);

        editTextView = (EditText) findViewById(R.id.n2m_edit_note);
        Button createNote = (Button) findViewById(R.id.n2m_create_note);


        listofLocationMarker = new ArrayList<>();
        listofNoteContents = new ArrayList<>();
        listOftargetedUsers = new ArrayList<>();

        if (targetedUsersExtra == null) {
            targetedUsersExtra = "";
        }
        listOftargetedUsers.addAll(Arrays.asList(targetedUsersExtra.split(";")));

        // noteTime
        final String times[] = noteTime.split("[\\|]+");
        switch (noteType) {
            case "EVENT":
                preEditText = "Event: ";
                break;
            case "REMINDER":
                preEditText = "Remind:";
                break;
            case "DIRECTION":
                preEditText = "Direction:";
                break;
        }

        TextView noteExtraDetail = (TextView) findViewById(R.id.n2m_note_detail_location_activity);
        noteExtraDetail.setText("Note: on "+ times[0] + " at " + times[1]);

        editTextView.setText("Enter note's detail...");

        if(tapNote.equals("true")){

            editTextView.setFocusable(false);
            autoCompView.setVisibility(View.INVISIBLE);
            createNote.setText("Delete Note");

            createNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(receivedNote.getOwner().equals(currentUser.username)){

                        for(int n = 0; n< currentUser.notes.size();n++) {
                            if(currentUser.notes.get(n).getNoteId().equals(receivedNote.getNoteId())) {
                                counterI = 0;
                                //ArrayList<String> deleteTargetUsers  = new ArrayList<String>();
                                final ArrayList<String> deleteTargetUsers = currentUser.notes.get(n).getTargetedUsers();
                                for(; counterI <deleteTargetUsers.size();counterI++){
                                    final String deleteNotefromUser = deleteTargetUsers.get(counterI);
                                    //getting the list of notes:
                                    mDatabase.child("users").child(deleteNotefromUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            User user = snapshot.getValue(User.class);
                                            if (user.notes == null) {
                                                user.notes = new ArrayList<Note>();
                                            }
                                            for(int k = 0; k< user.notes.size();k++) {
                                                if(user.notes.get(k).getNoteId().equals(receivedNote.getNoteId())) {
                                                    user.notes.remove(k);
                                                }
                                            }
                                            mDatabase.child("users").child(deleteNotefromUser).setValue(user);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError firebaseError) {
                                        }
                                    });
                                }
                                currentUser.notes.remove(n);

                            }

                        }
                        counterI = 0;
                        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(currentUser);
                        Intent intent = new Intent(Note2MapSearchLocationActivity.this, Note2MapNotesActivity.class);
                        intent.putExtra("currentUser", currentUser);
                        startActivity(intent);

                    } else{
                        //just delete it from his notes list
                        for(int n = 0; n< currentUser.notes.size();n++) {
                            if(currentUser.notes.get(n).getNoteId().equals(receivedNote.getNoteId())) {
                                currentUser.notes.remove(n);
                            }
                        }
                        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(currentUser);
                    }

                    Intent intent = new Intent(Note2MapSearchLocationActivity.this, Note2MapNotesActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    startActivity(intent);

                }
            });

        } else {

            autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.n2m_list_item));
            autoCompView.setOnItemClickListener(this);

            editTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (noteContent != null) {
                        noteContent.noteText = s.toString();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


            counterI = 0;
            createNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // create a note and it the list of notes of the user
                    final Note newNote = new Note(noteType, times[0],
                            times[1], times[2], "notReceived", currentUser.username, listofNoteContents, listOftargetedUsers, location);
                    //if(listOftargetedUsers.contains(currentUser.userId)) {
                        if (currentUser.notes.size() == 0) {
                            currentUser.notes.add(newNote);
                        } else {
                            if (!currentUser.notes.contains(newNote)) {
                                currentUser.notes.add(newNote);
                            }
                    /*for (int n = 0; n < currentUser.notes.size(); n++) {
                        if (!currentUser.notes.get(n).getNoteId().equals(newNote.getNoteId())) {
                            currentUser.notes.add(newNote);
                        }
                    }*/
                        }
                    //}
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(currentUser);

                    for (; counterI < listOftargetedUsers.size(); counterI++) {
                        final String addNotestoTargetUsers = listOftargetedUsers.get(counterI);
                        //getting the list of notes:
                        if (addNotestoTargetUsers != "") {
                            mDatabase.child("users").child(addNotestoTargetUsers).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    if (user.userId.equals(addNotestoTargetUsers) && !currentUser.userId.equals(addNotestoTargetUsers)) {
                                        if (user.notes == null) {
                                            user.notes = new ArrayList<Note>();
                                        }
                                        user.notes.add(newNote);

                                        mDatabase.child("users").child(addNotestoTargetUsers).setValue(user);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {
                                }
                            });
                        }
                    }
                    counterI = 0;

                    Intent intent = new Intent(Note2MapSearchLocationActivity.this, Note2MapNotesActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    startActivity(intent);

                }
            });
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        MarkerOptions markerOptions;
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        String title="New Marker";

        if(tapNote.equals("true")) {
            return;
        }
        try {
            if (point != null) {
                addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                Log.d("ssssss", point.latitude + "," + point.longitude);
            }
        }catch (IOException ie){
            ie.printStackTrace();
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            String errorMessage = "Invalid Latitude and Longitude";
            Log.e("SearchLocationActivity", errorMessage + ". " +
                    "Latitude = " + point.latitude +
                    ", Longitude = " +
                    point.longitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            Log.d("SearchLocationActivity","No addresses found for given coordinates");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            title = TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
        }

        markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title(title);
        if((noteType.equals("EVENT") || noteType.equals("REMINDER")) && listofLocationMarker.size() > 0) {
            listofLocationMarker.remove(locationMarker);
            listofNoteContents.clear();
            locationMarker.remove();
            noteContent = null;
            locationMarker = null;
        }
        location = title.replace("\n", " ");
        locationMarker = googleMap.addMarker(markerOptions);
        locationMarker.setDraggable(true);
        locationMarker.showInfoWindow();
        listofLocationMarker.add(locationMarker);
        noteContent = new NoteContent(addresses.get(0).getLatitude() + "," + addresses.get(0).getLongitude(), preEditText);
        listofNoteContents.add(noteContent);
        editTextView.setText(noteContent.noteText);

        //Animating the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onMapLongClick(LatLng point) {
        MarkerOptions markerOptions;
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        String title="New Marker";

        if(tapNote.equals("true")) {
            return;
        }

        try {
            if (point != null) {
                addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            }
        }catch (IOException ie){
            ie.printStackTrace();
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            String errorMessage = "Invalid Latitude and Longitude";
            Log.e("SearchLocationActivity", errorMessage + ". " +
                    "Latitude = " + point.latitude +
                    ", Longitude = " +
                    point.longitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            Log.d("SearchLocationActivity","No addresses found for given coordinates");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            title = TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
        }

        markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title(title);

        locationMarker = googleMap.addMarker(markerOptions);
        locationMarker.showInfoWindow();

        //Animating the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        location = str;
        getCoordinatesOfLocation();
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;
        HttpURLConnection conn = null;

        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:us");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return (String) resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    public void getCoordinatesOfLocation() {
        MarkerOptions markerOptions;
        ArrayList coordinates = null;

        if (location != null || !location.equals("")) {
            try {
                coordinates = new Retrievedata().execute().get();
            }catch (InterruptedException ie){
                ie.printStackTrace();
            } catch (ExecutionException ee){ee.printStackTrace();}

            if(coordinates != null){
                double latitude = Double.parseDouble(coordinates.get(0).toString());
                double longitude = Double.parseDouble(coordinates.get(1).toString());
                LatLng latLng = new LatLng(latitude, longitude);
                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(location);

                if((noteType.equals("EVENT") || noteType.equals("REMINDER")) && listofLocationMarker.size() > 0) {
                    listofLocationMarker.remove(locationMarker);
                    listofNoteContents.clear();
                    locationMarker.remove();
                    noteContent = null;
                    locationMarker = null;
                }

                locationMarker = googleMap.addMarker(markerOptions);
                locationMarker.showInfoWindow();
                if(tapNote.equals("false")) {
                    locationMarker.setDraggable(true);
                    listofLocationMarker.add(locationMarker);
                }

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                //Animating the camera
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                autoCompView.setText("");

                // Check if no view has focus:
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                noteContent = new NoteContent(latLng.latitude+","+latLng.longitude, preEditText);
                listofNoteContents.add(noteContent);
                editTextView.setText(noteContent.noteText);
            }
        }
        return;
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        if (googleMap != null) {
            if(tapNote.equals("true")){
                loadNoteLocations();
            }

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Log.d("aaaaaa","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    marker.showInfoWindow();
                    locationMarker = marker;
                    noteContent = listofNoteContents.get(listofLocationMarker.indexOf(marker));
                    editTextView.setText(noteContent.noteText);
                    return true;
                }
            });
            if(!tapNote.equals("true")) {
                //Log.d("aaaaaa","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        // TODO Auto-generated method stub
                        listofNoteContents.remove(listofLocationMarker.indexOf(marker));
                        listofLocationMarker.remove(marker);
                        marker.remove();
                        noteContent = null;
                        locationMarker = null;
                        editTextView.setText("");
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                        // TODO Auto-generated method stub

                    }
                });
            }
            googleMap.setOnMapClickListener(this);
            //googleMap.setOnMapLongClickListener(this);
        }

    }

    public void loadNoteLocations() {
        if (receivedNote != null) {
            for (int k = 0; k < receivedNote.noteContents.size(); k++) {
                String[] latlongStrings = receivedNote.noteContents.get(k).getNoteCoordinates().split(",");
                LatLng point = new LatLng(Double.parseDouble(latlongStrings[0]), Double.parseDouble(latlongStrings[1]));
                MarkerOptions markerOptions;
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = null;
                String title="New Marker";

                try {
                    if (point != null) {
                        addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                    }
                }catch (IOException ie){
                    ie.printStackTrace();
                } catch (IllegalArgumentException illegalArgumentException) {
                    // Catch invalid latitude or longitude values.
                    String errorMessage = "Invalid Latitude and Longitude";
                    Log.e("SearchLocationActivity", errorMessage + ". " +
                            "Latitude = " + point.latitude +
                            ", Longitude = " +
                            point.longitude, illegalArgumentException);
                }

                // Handle case where no address was found.
                if (addresses == null || addresses.size()  == 0) {
                    Log.d("SearchLocationActivity","No addresses found for given coordinates");
                } else {
                    Address address = addresses.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();

                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.
                    for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    title = TextUtils.join(System.getProperty("line.separator"),
                            addressFragments);
                }

                markerOptions = new MarkerOptions();
                markerOptions.position(point);
                markerOptions.title(title);
                if((noteType.equals("EVENT") || noteType.equals("REMINDER")) && listofLocationMarker.size() > 0) {
                    listofLocationMarker.remove(locationMarker);
                    listofNoteContents.clear();
                    locationMarker.remove();
                    noteContent = null;
                    locationMarker = null;
                }
                location = title.replace("\n", " ");
                locationMarker = googleMap.addMarker(markerOptions);
                locationMarker.showInfoWindow();
                listofLocationMarker.add(locationMarker);
                noteContent = receivedNote.noteContents.get(k);
                listofNoteContents.add(noteContent);
                editTextView.setText(noteContent.noteText);

                googleMap.moveCamera(CameraUpdateFactory.newLatLng(point));

                //Animating the camera
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
    }

    class Retrievedata extends AsyncTask<String, Void, ArrayList> {
        @Override
        protected ArrayList doInBackground(String... params) {
            HttpURLConnection conn = null;
            ArrayList<Object> resList = new ArrayList<Object>();
            StringBuilder jsonResults = new StringBuilder();
            try{
                StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?address="+location.replaceAll(" ","+"));
                sb.append("&key=" + API_KEY);

                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("results");

                String lat = predsJsonArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat");
                String lng = predsJsonArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng");
                resList.add(0,lat);
                resList.add(1,lng);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot process JSON results", e);
            }

            return resList;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Note2MapDetectNetworkActivity.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mybroadcast, intentFilter);
        if (!isNetworkAvailable(getApplicationContext())) {
            if(dialog == null){
                dialog = new Dialog(Note2MapSearchLocationActivity.this);
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

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mybroadcast);
    }

}