package edu.neu.madcourse.priyankabh.note2map;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import edu.neu.madcourse.priyankabh.note2map.models.User;

import static edu.neu.madcourse.priyankabh.note2map.Note2MapChooseNoteType.NOTE_TYPE;
import static edu.neu.madcourse.priyankabh.note2map.Note2MapMainActivity.isNetworkAvailable;

/**
 * Created by priya on 4/11/2017.
 */

public class SelectEventTimeActivity extends AppCompatActivity {
    final static String NOTE_TIME = "note_time";
    public static TextView datePicker;
    public static TextView startTime;
    private Dialog dialog;
    private BroadcastReceiver mybroadcast;
    public static Spinner durationSpinner;
    private Button continueButton;
    private Bundle b;
    public String duration;
    private User currentUser;

    public static class StartTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hourS = "";
            String minuteS = "";
            if (hourOfDay < 10) {
                hourS = "0" + hourOfDay;
            }
            if (minute < 10) {
                minuteS = "0" + minute;
            }
            startTime.setText(new StringBuilder().append(hourS).append(" : ")
                    .append(minuteS));
        }
    }

    public void showStartTimePickerDialog(View v) {
        DialogFragment newFragment = new StartTimePickerFragment();
        newFragment.show(getFragmentManager(), "startTimePicker");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_eventtime_select_activity);

        mybroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(Note2MapDetectNetworkActivity.IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";
                Log.d("networkStatus",networkStatus);
                if(networkStatus.equals("connected")){
                    if(dialog!=null && dialog.isShowing()){
                        dialog.cancel();
                        dialog.dismiss();
                        dialog.hide();
                    }
                } else {
                    if(dialog == null){
                        dialog = new Dialog(SelectEventTimeActivity.this);
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

        b = getIntent().getExtras();
        if (b != null) {
            currentUser = (User) b.getSerializable("currentUser");
        }

        final String noteType = getIntent().getStringExtra(NOTE_TYPE);
        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_select_time);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Set Time");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startTime = (TextView) findViewById(R.id.startValue);
        ////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////Dropdown fo duration/////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////

        durationSpinner = (Spinner) findViewById(R.id.endValue);

        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.duration_array,
                        R.layout.n2m_duration_spinner_item);

        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(R.layout.n2m_duration_spinner_dropdown_item);

        // Apply the adapter to the spinner
        durationSpinner.setAdapter(staticAdapter);

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                duration = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        //////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        Calendar datetime = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH : mm ");

        startTime.setText(simpleDateFormat.format(datetime.getTime()));

        datePicker = (TextView) findViewById(R.id.dateValue);
        String date = new SimpleDateFormat("MM/dd/yy").format(new Date());
        datePicker.setText(date);

        continueButton = (Button) findViewById(R.id.n2m_select_time_continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectEventTimeActivity.this, Note2MapSelectFriendsActivity.class);
                intent.putExtra("currentUser", currentUser);
                intent.putExtra(NOTE_TYPE,noteType);
                //Append all times pipe separated to the last screen to select location before create it
                intent.putExtra(NOTE_TIME,
                        datePicker.getText().toString() + "|" +
                                startTime.getText().toString() + "|" +
                                duration);
                startActivity(intent);
            }
        });
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("America/Cambridge_Bay"));
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog =  new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            datePicker.setText(new StringBuilder().append(month+1).append("/")
                    .append(day).append("/").append(year));
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
                dialog = new Dialog(SelectEventTimeActivity.this);
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

