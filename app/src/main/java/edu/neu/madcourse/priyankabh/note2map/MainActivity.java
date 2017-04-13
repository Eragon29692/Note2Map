package edu.neu.madcourse.priyankabh.note2map;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button evenTimePicker = (Button) findViewById(R.id.eventTimePicker);
        evenTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SelectEventTimeActivity.class);
                startActivity(intent);
            }
        });

        startService(new Intent(this,MyLocationService.class));
    }
}
