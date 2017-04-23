package edu.neu.madcourse.priyankabh.note2map;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import edu.neu.madcourse.priyankabh.note2map.models.User;

/**
 * Created by priya on 4/22/2017.
 */

public class Note2MapAllUsersActivity extends AppCompatActivity {
    private Bundle b;
    private ArrayList<String> usernames;
    private User currentUser;
    private ListView listView;
    private Note2MapCustomAdaptorForAllUsers customAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.n2m_listview_allusers);

        //toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.n2m_my_toolbar_allusers);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Manage Friends");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.n2m_listviewlayout_allusers);

        b = getIntent().getExtras();

        if (b != null) {
            currentUser = (User) b.getSerializable("currentUser");
            usernames = b.getStringArrayList("username");
        }

        ArrayList<String> namesWithoutCurrentUser = usernames;
        namesWithoutCurrentUser.remove(currentUser.username.toLowerCase());
        customAdapter = new Note2MapCustomAdaptorForAllUsers(this, namesWithoutCurrentUser, currentUser);
        listView.setAdapter(customAdapter);

    }

    public void onClickAddFriend(View view){
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //get the row the clicked button is in
        LinearLayout vwParentRow = (LinearLayout)view.getParent();

        TextView textView = (TextView)vwParentRow.getChildAt(1);
        String newFriend = textView.getText().toString();
        if(!newFriend.toLowerCase().equals(currentUser.username.toLowerCase()) && !currentUser.friends.contains(newFriend.toLowerCase())) {
            currentUser.friends.add(newFriend.toLowerCase());
        }
        mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(currentUser);
        for(String str: currentUser.friends){
            Log.d("onClickAddFriend",str);
        }

        customAdapter = new Note2MapCustomAdaptorForAllUsers(this, usernames, currentUser);
        listView.setAdapter(customAdapter);

        // smart search
        listView.setTextFilterEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.friend_menu, menu);

        MenuItem item = menu.findItem(R.id.n2m_friend_add_friend);
        item.setVisible(false);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.n2m_action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setIconifiedByDefault(false);

        EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        searchPlate.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                Note2MapAllUsersActivity.this.customAdapter.getFilter().filter(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle your other action bar items...
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(Note2MapAllUsersActivity.this, Note2MapFriendActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
            Note2MapAllUsersActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Note2MapAllUsersActivity.this, Note2MapFriendActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
        Note2MapAllUsersActivity.this.finish();
    }

}
