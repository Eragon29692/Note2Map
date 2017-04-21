package edu.neu.madcourse.priyankabh.note2map.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Note {
    public String noteType;
    public String noteDate;
    public String endTime;
    public String startTime;
    public Boolean noteReceived;
    public ArrayList<String> targetedUsers;
    public ArrayList<NoteContent> noteContents;



    public Note(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Note(String noteType, String noteDate, String startTime, String endTime, Boolean noteReceived, ArrayList<NoteContent> noteContent) {
        this.noteType = noteType;
        this.noteDate = noteDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.noteReceived = noteReceived;
        this.noteContents = noteContent;
    }

}

