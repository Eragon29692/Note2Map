package edu.neu.madcourse.priyankabh.note2map.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class NoteContent implements Serializable{

    public String noteCoordinates;
    public String noteText;
    public String noteReceived;

    public String getNoteCoordinates() {
        return noteCoordinates;
    }

    public void setNoteCoordinates(String noteCoordinates) {
        this.noteCoordinates = noteCoordinates;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public NoteContent(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public NoteContent(String noteCoordinates, String noteText){
        this.noteCoordinates = noteCoordinates;
        this.noteText = noteText;
        this.noteReceived = "notReceived";
    }

}
