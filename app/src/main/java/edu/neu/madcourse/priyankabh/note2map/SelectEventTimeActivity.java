package edu.neu.madcourse.priyankabh.note2map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static edu.neu.madcourse.priyankabh.note2map.Note2MapChooseNoteType.NOTE_TYPE;

/**
 * Created by priya on 4/11/2017.
 */

public class SelectEventTimeActivity extends AppCompatActivity {
    final static String NOTE_TIME = "note_time";
    public static TextView datePicker;
    public static TextView startTime;
    public static TextView endTime;
    private Button continueButton;

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
            startTime.setText(new StringBuilder().append(hourOfDay).append(" : ")
                    .append(minute));
        }
    }

    public void showStartTimePickerDialog(View v) {
        DialogFragment newFragment = new StartTimePickerFragment();
        newFragment.show(getFragmentManager(), "startTimePicker");
    }

    public void showEndTimePickerDialog(View v) {
        DialogFragment endFragment = new EndTimePickerFragment();
        endFragment.show(getFragmentManager(), "endTimePicker");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventtime_select);
        final String noteType = getIntent().getStringExtra(NOTE_TYPE);
        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_select_time);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Set Time");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startTime = (TextView) findViewById(R.id.startValue);
        endTime = (TextView) findViewById(R.id.endValue);

        Calendar datetime = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH : mm ");

        endTime.setText("0h 0m");
        startTime.setText(simpleDateFormat.format(datetime.getTime()));

        datePicker = (TextView) findViewById(R.id.dateValue);
        String date = new SimpleDateFormat("MM/dd/yy").format(new Date());
        datePicker.setText(date);

        continueButton = (Button) findViewById(R.id.n2m_select_time_continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectEventTimeActivity.this, Note2MapSearchLocationActivity.class);
                intent.putExtra(NOTE_TYPE,noteType);
                //Append all times pipe separated to the last screen to select location before create it
                intent.putExtra(NOTE_TIME,
                        datePicker.getText().toString() + "|" +
                                startTime.getText().toString() + "|" +
                                endTime.getText().toString());
                startActivity(intent);
            }
        });
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class EndTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, 0, 0,
                    DateFormat.is24HourFormat(getActivity()));
            return timePickerDialog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            datetime.set(Calendar.MINUTE, minute);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH : mm");
            String duration = simpleDateFormat.format(datetime.getTime());
            duration = (duration.charAt(0) == '0' ? duration.substring(1,2) : duration.substring(0,2)) + "h "
                    + (duration.charAt(5) == '0' ? duration.substring(6,7) : duration.substring(5,7)) + "m";

            endTime.setText(duration);
        }
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
}

