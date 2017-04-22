package edu.neu.madcourse.priyankabh.note2map.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {

    public String username;
    public ArrayList<String> friends;
    public ArrayList<Note> notes;


    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username){
        this.username = username;
        this.friends = new ArrayList<>();
        this.notes = new ArrayList<>();
    }

}
