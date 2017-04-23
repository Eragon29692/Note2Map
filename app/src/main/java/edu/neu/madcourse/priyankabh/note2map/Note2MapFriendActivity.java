package edu.neu.madcourse.priyankabh.note2map;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.ArrayList;
import java.util.Map;

import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapFriendActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayList<String> drawerList;
    private ArrayList<String> usernames;
    private DatabaseReference mDatabase;
    private User currentUser;
    private Bundle b;
    private ListView listView;
    private Button quitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_friend_activity);

        b = getIntent().getExtras();

        if (b != null) {
            currentUser = (User) b.getSerializable("currentUser");
        }

        //navigation bar
        mDrawerLayout = (DrawerLayout) findViewById(R.id.n2m_drawer_layout_friend);
        mDrawerList = (ListView) findViewById(R.id.n2m_left_drawer_friend);
        drawerList = new ArrayList<>();
        drawerList.add("Notes");
        drawerList.add("Friends");
        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_friend);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Friends");

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.n2m_drawer_list_item, drawerList));

        //onclick action
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                if(position == 0) {
                    Intent intent = new Intent(Note2MapFriendActivity.this, Note2MapMainActivity.class);
                    startActivity(intent);
                    Note2MapFriendActivity.this.finish();
                }
            }
        });

        quitButton = (Button) findViewById(R.id.n2m_friend_quitButton);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note2MapFriendActivity.this.finish();
                System.exit(0);
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
                getSupportActionBar().setTitle("Friends");
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

        mDatabase = FirebaseDatabase.getInstance().getReference();
        usernames = new ArrayList<String>();

        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    //Get user map
                    Map singleUser = (Map) entry.getValue();
                    usernames.add(((String) singleUser.get("username")).toLowerCase());
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        listView = (ListView) findViewById(R.id.n2m_listviewlayout_friends);

        Note2MapCustomAdaptorForFriends customAdapter = new Note2MapCustomAdaptorForFriends(this, currentUser);
        listView.setAdapter(customAdapter);
    }

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
        if(item.getItemId() == R.id.n2m_friend_add_friend){
            Intent intent = new Intent(Note2MapFriendActivity.this, Note2MapAllUsersActivity.class);
            intent.putExtra("username", usernames);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuItem item = menu.findItem(R.id.add_friend);
        //item.setVisible(true);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friend_menu, menu);
        MenuItem item = menu.findItem(R.id.n2m_friend_add_friend);
        item.setVisible(true);

        MenuItem search = menu.findItem(R.id.n2m_action_search);
        search.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    public void onClickRemoveFriend(View view){
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //get the row the clicked button is in
        LinearLayout vwParentRow = (LinearLayout)view.getParent();

        TextView textView = (TextView)vwParentRow.getChildAt(1);
        String newFriend = textView.getText().toString();
        currentUser.friends.remove(newFriend.toLowerCase());

        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(currentUser);

        Note2MapCustomAdaptorForFriends customAdapter = new Note2MapCustomAdaptorForFriends(this, currentUser);
        listView.setAdapter(customAdapter);
    }
}
