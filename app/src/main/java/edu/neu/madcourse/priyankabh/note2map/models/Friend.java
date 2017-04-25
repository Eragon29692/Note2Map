package edu.neu.madcourse.priyankabh.note2map.models;

/**
 * Created by priya on 4/24/2017.
 */

public class Friend {
    private String friendName;
    private boolean selected = false;

    public Friend(String name, boolean selected){
        this.selected = selected;
        this.friendName = name;
    }

    public String getName() {
        return friendName;
    }
    public void setName(String name) {
        this.friendName = name;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
