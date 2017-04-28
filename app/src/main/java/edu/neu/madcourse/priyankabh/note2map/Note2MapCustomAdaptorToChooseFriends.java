package edu.neu.madcourse.priyankabh.note2map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import edu.neu.madcourse.priyankabh.note2map.models.Friend;
import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapCustomAdaptorToChooseFriends extends ArrayAdapter {
    Context context;
    ArrayList<String> userList;
    ArrayList<Friend> targetFriends = new ArrayList<Friend>();

    User currentUser;


    public Note2MapCustomAdaptorToChooseFriends(Activity applicationContext, User user) {
        super(applicationContext, R.layout.n2m_select_friends_activity, user.friends);
        this.currentUser = user;
        this.context = applicationContext;
        this.userList = user.friends;
        if(!this.userList.contains(currentUser.username)) {
            this.userList.add(currentUser.username);
        }
        for(String s: userList){
            Friend f = new Friend(s, false);
            this.targetFriends.add(f);
        }
    }

    @Override
    public Object getItem(int position) {
        return targetFriends.get(position);
    }

    Friend getFriend(int position) {
        return ((Friend) getItem(position));
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.n2m_select_friends_listview_element, null,
                    true);
        }

        Friend f = getFriend(i);

        TextView friendName = (TextView) view.findViewById(R.id.n2m_textview_selectFriends);
        friendName.setText(targetFriends.get(i).getName().toUpperCase());

        CheckBox chosen = (CheckBox) view.findViewById(R.id.n2m_checkbox_selectFriends);
        chosen.setOnCheckedChangeListener(checkedFriendsList);
        chosen.setTag(i);
        chosen.setChecked(f.isSelected());

        return view;
    }

    ArrayList<Friend> getBox() {
        ArrayList<Friend> box = new ArrayList<Friend>();
        for (Friend p : targetFriends) {
            if (p.isSelected())
                box.add(p);
        }
        return box;
    }

    CompoundButton.OnCheckedChangeListener checkedFriendsList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getFriend((Integer) buttonView.getTag()).setSelected(isChecked);
        }
    };
}