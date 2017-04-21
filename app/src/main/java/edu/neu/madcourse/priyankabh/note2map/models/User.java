package edu.neu.madcourse.priyankabh.note2map.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String userId;
    public String coordinates;


    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId){
        this.userId = userId;
        coordinates = "";
    }

}
