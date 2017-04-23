package edu.neu.madcourse.priyankabh.note2map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;

import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapCustomAdaptorForAllUsers extends ArrayAdapter {
    Context context;
    ArrayList<String> userList;
    User currentUser;


    public Note2MapCustomAdaptorForAllUsers(Activity applicationContext, ArrayList<String> userList, User user) {
        super(applicationContext, R.layout.n2m_listview_allusers, userList);
        this.currentUser = user;
        this.context = applicationContext;
        this.userList = userList;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = layoutInflater.inflate(R.layout.n2m_listview_element_allusers, null,
                true);

        TextView user = (TextView) viewRow.findViewById(R.id.n2m_element_all_users);
        ImageView image = (ImageView) viewRow.findViewById(R.id.n2m_user_image_view);
        user.setText(userList.get(i).toUpperCase());

        ImageButton imgButton = (ImageButton) viewRow.findViewById(R.id.n2m_addFriend_button);
        if(currentUser.friends.contains(user.getText().toString().toLowerCase())){
            imgButton.setBackgroundResource(R.drawable.tick_friend);
            imgButton.setClickable(false);
        } else {
            imgButton.setBackgroundResource(R.drawable.add_friend);
            imgButton.setClickable(true);
        }

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color1 = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(4) /* thickness in px */
                .width(60)  // width in px
                .height(60) // height in px
                .textColor(Color.BLACK)
                .useFont(Typeface.DEFAULT)
                .fontSize(30) /* size in px */
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(userList.get(i).substring(0, 1), color1);

        image.setImageDrawable(drawable);

        return viewRow;
    }
}