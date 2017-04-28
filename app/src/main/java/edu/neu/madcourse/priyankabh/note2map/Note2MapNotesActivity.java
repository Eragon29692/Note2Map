package edu.neu.madcourse.priyankabh.note2map;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import edu.neu.madcourse.priyankabh.note2map.models.Note;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Note2MapNotesAdaptor adapter;
    private List<Note> noteList;
    private User currentUser;
    Button quitButton;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ArrayList<String> drawerList;
    private TextView errorTextView;
    private Dialog dialog;
    private BroadcastReceiver mybroadcast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_note_main);

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
                        dialog = new Dialog(Note2MapNotesActivity.this);
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

        errorTextView = (TextView) findViewById(R.id.n2m_addNote_error);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.n2m_drawer_layout_note);
        mDrawerList = (ListView) findViewById(R.id.n2m_left_drawer_note);
        drawerList = new ArrayList<>();
        drawerList.add("Notes");
        drawerList.add("Friends");

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_note);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Notes");

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.n2m_drawer_list_item, drawerList));
        //onclick action
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                if(position == 1) {
                    Intent intent = new Intent(Note2MapNotesActivity.this, Note2MapFriendActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    startActivity(intent);
                    Note2MapNotesActivity.this.finish();
                }
            }
        });

        quitButton = (Button) findViewById(R.id.n2m_note_quitButton);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note2MapNotesActivity.this.finish();
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

        currentUser = (User) getIntent().getExtras().getSerializable("currentUser");

        recyclerView = (RecyclerView) findViewById(R.id.n2m_note_recycler_view);

        noteList = new ArrayList<>();

        if(currentUser.notes!=null) {
            for (int i = 0; i < currentUser.notes.size(); i++) {
                if (currentUser.notes.get(i).getOwner().equals(currentUser.username) || currentUser.notes.get(i).getNoteReceived().equals("received")) {
                    noteList.add(currentUser.notes.get(i));
                }
            }
        }

        //noteList = currentUser.notes;
        if(noteList == null || noteList.size() == 0){
            errorTextView.setVisibility(View.VISIBLE);
        }

        adapter = new Note2MapNotesAdaptor(this, noteList, currentUser);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Note2MapDetectNetworkActivity.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mybroadcast, intentFilter);
    }

    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mybroadcast);
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
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.n2m_note_action_menu_new:
                Intent intent = new Intent(Note2MapNotesActivity.this, Note2MapChooseNoteType.class);
                intent.putExtra("currentUser", currentUser);
                this.startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.n2m_note_action_menu, menu);
        return true;
    }
}
