package edu.neu.madcourse.priyankabh.note2map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.priyankabh.note2map.models.User;

public class Note2MapCustomAdaptorForAllUsers extends ArrayAdapter implements Filterable{
    Context context;
    ArrayList<String> userList;
    User currentUser;
    ArrayList<String> mOriginalValues; // Original Values


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
            imgButton.setBackgroundResource(R.drawable.n2m_added_friend_icon);
            imgButton.setClickable(false);
        } else {
            imgButton.setBackgroundResource(R.drawable.n2m_not_added_friend_icon);
            imgButton.setClickable(true);
        }

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .withBorder(0) /* thickness in px */
                .width(60)  // width in px
                .height(60) // height in px
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .fontSize(30) /* size in px */
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(userList.get(i).substring(0, 1), Color.BLACK);

        image.setImageDrawable(drawable);

        return viewRow;
    }


    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                userList = (ArrayList<String>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<String> FilteredArrList = new ArrayList<String>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<String>(userList); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        String data = mOriginalValues.get(i);
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(data);
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}