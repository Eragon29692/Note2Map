package edu.neu.madcourse.priyankabh.note2map.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;

@IgnoreExtraProperties
public class Note implements Serializable{
    public String noteId; //initialized with timestamp
    public String noteType;
    public String noteDate;
    public String duration;
    public String startTime;
    public Boolean noteReceived;
    public String owner; //Note's owner's username. Owner can delete/edit the note
    public ArrayList<String> targetedUsers;
    public ArrayList<NoteContent> noteContents;

    public Note(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Note(String noteType, String noteDate, String startTime, String duration, Boolean noteReceived, String owner, ArrayList<NoteContent> noteContent) {
        this.noteId = Long.toString(System.currentTimeMillis());
        this.noteType = noteType;
        this.noteDate = noteDate;
        this.startTime = startTime;
        this.duration = duration;
        this.noteReceived = noteReceived;
        this.owner = owner;
        this.noteContents = noteContent;
        this.targetedUsers = new ArrayList<>();
    }

}

