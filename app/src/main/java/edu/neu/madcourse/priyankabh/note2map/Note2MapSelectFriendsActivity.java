package edu.neu.madcourse.priyankabh.note2map;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import edu.neu.madcourse.priyankabh.note2map.models.Friend;
import edu.neu.madcourse.priyankabh.note2map.models.User;

import static edu.neu.madcourse.priyankabh.note2map.Note2MapChooseNoteType.NOTE_TYPE;
import static edu.neu.madcourse.priyankabh.note2map.SelectEventTimeActivity.NOTE_TIME;

public class Note2MapSelectFriendsActivity extends AppCompatActivity {

    private ArrayList<String> usernames;
    private User currentUser;
    private ListView listView;
    private String noteTime;
    private String noteType;
    private Bundle b;
    private Note2MapCustomAdaptorToChooseFriends customAdapter;
    private DatabaseReference mDatabase;
    private ChildEventListener childEventListener;
    private ArrayList<User> listOfUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_select_friends_activity);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        noteType = getIntent().getStringExtra(NOTE_TYPE);
        noteTime = getIntent().getStringExtra(NOTE_TIME);

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_select_friends);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Choose Friend");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        b = getIntent().getExtras();
        if(b!=null){
            currentUser = (User) b.getSerializable("currentUser");
        }
        listView = (ListView) findViewById(R.id.n2m_listview_selectfriends);

        if (currentUser.friends == null) {
            currentUser.friends = new ArrayList<>();
        }

        customAdapter = new Note2MapCustomAdaptorToChooseFriends(this, currentUser);
        listView.setAdapter(customAdapter);

        listOfUsers = new ArrayList<>();

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                listOfUsers.add(user);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousID) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        mDatabase.child("users").addChildEventListener(childEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDatabase.child("users").removeEventListener(childEventListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onClickChooseFriend(View view){
        StringBuilder listOffriends = new StringBuilder();

        for (Friend p : customAdapter.getBox()) {
            if (p.isSelected()){
                for (int i = 0; i < listOfUsers.size(); i++) {
                    if (listOfUsers.get(i).username.toLowerCase().equals(p.getName().toLowerCase())) {
                        listOffriends.append(listOfUsers.get(i).userId);
                        listOffriends.append(";");
                    }
                }
            }
        }


        Intent intent = new Intent(Note2MapSelectFriendsActivity.this, Note2MapSearchLocationActivity.class);
        intent.putExtra(NOTE_TYPE,noteType);
        intent.putExtra(NOTE_TIME,noteTime);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("friends", listOffriends.toString());
        startActivity(intent);
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
