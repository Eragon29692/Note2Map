package edu.neu.madcourse.priyankabh.note2map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapChooseNoteType extends AppCompatActivity {
    final static String NOTE_TYPE = "note_type";
    private Button eventTypeButton;
    private Button reminderTypeButton;
    private Button directionTypeButton;
    private User currentUser;
    private Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_choose_note_type_activity);
        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_choose_note_type);
        setSupportActionBar(myToolbar);

        b = getIntent().getExtras();
        if (b != null) {
            currentUser = (User) b.getSerializable("currentUser");
        }

        getSupportActionBar().setTitle("Pick Note's Type");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        eventTypeButton = (Button) findViewById(R.id.n2m_choose_event_type_button);
        reminderTypeButton = (Button) findViewById(R.id.n2m_choose_reminder_type_button);
        directionTypeButton = (Button) findViewById(R.id.n2m_choose_direction_type_button);

        eventTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Note2MapChooseNoteType.this, SelectEventTimeActivity.class);
                intent.putExtra(NOTE_TYPE,"EVENT");
                intent.putExtra("currentUser",currentUser);
                startActivity(intent);
            }
        });
        reminderTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Note2MapChooseNoteType.this, SelectEventTimeActivity.class);
                intent.putExtra(NOTE_TYPE,"REMINDER");
                intent.putExtra("currentUser",currentUser);
                startActivity(intent);
            }
        });
        directionTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Note2MapChooseNoteType.this, SelectEventTimeActivity.class);
                intent.putExtra(NOTE_TYPE,"DIRECTION");
                intent.putExtra("currentUser",currentUser);
                startActivity(intent);
            }
        });
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
