package edu.neu.madcourse.priyankabh.note2map;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapChooseUsername extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ChildEventListener childEventListener;
    private ArrayList<String> usernames;
    private TextView errorTextView;
    private EditText usernameEditText;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter(Note2MapDetectNetworkActivity.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
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
                        dialog = new Dialog(Note2MapChooseUsername.this);
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
        }, intentFilter);

        setContentView(R.layout.n2m_choose_username_activity);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        usernames = new ArrayList<String>();
        errorTextView = (TextView) findViewById(R.id.n2m_choose_username_error);

        usernameEditText = (EditText) findViewById(R.id.n2m_choose_username_edit_text);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (errorTextView.getVisibility() == View.VISIBLE && !usernameEditText.getText().toString().equals("")) {
                    errorTextView.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                usernames.add(user.username.toLowerCase());
                Log.d("log users", usernames.toString().toLowerCase());
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


    public void onClickChooseUsername(View view) {
        Button chooseUsernameButton = (Button) findViewById(R.id.n2m_choose_username_button);
        chooseUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameEditText = (EditText) findViewById(R.id.n2m_choose_username_edit_text);
                String username = usernameEditText.getText().toString();
                if (usernames.contains(username.toLowerCase()) || username.equals("")) {
                    errorTextView = (TextView) findViewById(R.id.n2m_choose_username_error);
                    errorTextView.setVisibility(View.VISIBLE);
                    usernameEditText.setText("");
                } else {
                    User user = new User(username, FirebaseInstanceId.getInstance().getToken());
                    mDatabase.child("users").child(FirebaseInstanceId.getInstance().getToken()).setValue(user);
                    Intent intent = new Intent(Note2MapChooseUsername.this, Note2MapMainActivity.class);
                    startActivity(intent);
                    Note2MapChooseUsername.this.finish();
                }
            }
        });
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
}
