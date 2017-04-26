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
    public String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public Note(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Note(String noteType, String noteDate, String startTime, String duration, Boolean noteReceived, String owner, ArrayList<NoteContent> noteContent, ArrayList<String> targetedUsers, String location) {
        this.noteId = Long.toString(System.currentTimeMillis());
        this.noteType = noteType;
        this.noteDate = noteDate;
        this.startTime = startTime;
        this.duration = duration;
        this.noteReceived = noteReceived;
        this.location = location;
        this.owner = owner;
        this.noteContents = noteContent;
        this.targetedUsers = targetedUsers;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public String getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(String noteDate) {
        this.noteDate = noteDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Boolean getNoteReceived() {
        return noteReceived;
    }

    public void setNoteReceived(Boolean noteReceived) {
        this.noteReceived = noteReceived;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getTargetedUsers() {
        return targetedUsers;
    }

    public void setTargetedUsers(ArrayList<String> targetedUsers) {
        this.targetedUsers = targetedUsers;
    }

    public ArrayList<NoteContent> getNoteContents() {
        return noteContents;
    }

    public void setNoteContents(ArrayList<NoteContent> noteContents) {
        this.noteContents = noteContents;
    }
}

