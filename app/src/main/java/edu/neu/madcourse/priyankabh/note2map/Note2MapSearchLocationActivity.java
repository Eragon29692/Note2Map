package edu.neu.madcourse.priyankabh.note2map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.neu.madcourse.priyankabh.note2map.models.Note;
import edu.neu.madcourse.priyankabh.note2map.models.NoteContent;
import edu.neu.madcourse.priyankabh.note2map.models.User;

import static edu.neu.madcourse.priyankabh.note2map.Note2MapChooseNoteType.NOTE_TYPE;
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
    private String location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_search_location_activity);

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_search_location);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Set Up Location");

        noteType = getIntent().getStringExtra(NOTE_TYPE);
        noteTime = getIntent().getStringExtra(NOTE_TIME);
        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.n2m_show_map);

        // Getting a reference to the map
        supportMapFragment.getMapAsync(this);

        autoCompView = (AutoCompleteTextView) findViewById(R.id.n2m_autoCompleteTextView);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.n2m_list_item));
        autoCompView.setOnItemClickListener(this);

        switch (noteType) {
            case "EVENT":     preEditText = "Event on "+ noteTime.substring(0, 8) +" starting at "+noteTime.substring(9, 16).replace(" ","") + ": ";
                break;
            case "REMINDER":  preEditText = "Remind on "+ noteTime.substring(0, 8) +" at "+noteTime.substring(9, 16).replace(" ","") + ": ";
                break;
            case "DIRECTION": preEditText = "Direction:";
                break;
        }

        editTextView = (EditText) findViewById(R.id.n2m_edit_note);
        editTextView.setText(preEditText);

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

        listofLocationMarker = new ArrayList<>();
        listofNoteContents = new ArrayList<>();


        Button createNote = (Button) findViewById(R.id.n2m_create_note);
        createNote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDatabase = FirebaseDatabase.getInstance().getReference();

                //listofNoteContents.add(noteContent);
                // create a note and it the list of notes of the user
                Note newNote = new Note(noteType, noteTime.substring(0, 8),
                        noteTime.substring(9, 16), noteTime.substring(17), false, currentUser.username, listofNoteContents, location);
                currentUser.notes.add(newNote);
                mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(currentUser);
            }
        });
    }

    @Override
    public void onMapClick(LatLng point) {
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

                locationMarker = googleMap.addMarker(markerOptions);
                locationMarker.setDraggable(true);
                locationMarker.showInfoWindow();
                listofLocationMarker.add(locationMarker);

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
                preEditText = preEditText + " at "+ location+"...";
                editTextView.setText(preEditText);
                noteContent = new NoteContent(latLng.latitude+","+latLng.longitude, editTextView.getText().toString());
                listofNoteContents.add(noteContent);
            }
        }
        return;
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        if (googleMap != null) {
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        marker.showInfoWindow();
                        locationMarker = marker;
                        noteContent = listofNoteContents.get(listofLocationMarker.indexOf(marker));
                        editTextView.setText(noteContent.noteText);
                        return true;
                    }
                });
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
            googleMap.setOnMapClickListener(this);
            //googleMap.setOnMapLongClickListener(this);
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

}