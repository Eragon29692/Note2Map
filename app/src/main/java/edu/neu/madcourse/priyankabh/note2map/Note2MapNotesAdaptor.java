package edu.neu.madcourse.priyankabh.note2map;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import edu.neu.madcourse.priyankabh.note2map.models.Note;
import edu.neu.madcourse.priyankabh.note2map.models.User;

import static edu.neu.madcourse.priyankabh.note2map.Note2MapChooseNoteType.NOTE_TYPE;
import static edu.neu.madcourse.priyankabh.note2map.SelectEventTimeActivity.NOTE_TIME;

public class Note2MapNotesAdaptor extends RecyclerView.Adapter<Note2MapNotesAdaptor.MyViewHolder> {

    private Context mContext;
    private List<Note> noteList;
    private User currentUser;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView owner;
        public TextView noteContent;
        public RelativeLayout recyclerView;
        public TextView contents;
        public TextView ownerName;

        public MyViewHolder(View view) {
            super(view);
            owner = (TextView) view.findViewById(R.id.n2m_note_owner);
            noteContent = (TextView) view.findViewById(R.id.n2m_note_contents);

            contents = (TextView) view.findViewById(R.id.n2m_note_contents);
            ownerName = (TextView) view.findViewById(R.id.n2m_note_owner);

            contents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemPosition=getAdapterPosition();
                    Note item = noteList.get(itemPosition);

                    Intent intent = new Intent(mContext, Note2MapSearchLocationActivity.class);
                    intent.putExtra(NOTE_TIME,
                            item.getNoteDate().toString() + "|" +
                                    item.getStartTime().toString() + "|" +
                                    item.getDuration().toString());
                    intent.putExtra("currentUser",currentUser);
                    intent.putExtra("tapOnNote", "true");
                    intent.putExtra("viewLocation", item.getLocation());
                    intent.putExtra(NOTE_TYPE,item.getNoteType());
                    intent.putExtra("chosenNote", item);
                    mContext.startActivity(intent);
                }
            });
        }


    }


    public Note2MapNotesAdaptor(Context mContext, List<Note> noteList, User user) {
        this.mContext = mContext;
        this.noteList = noteList;
        this.currentUser = user;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.n2m_note_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.owner.setText("Owner: "+note.getOwner());
        holder.noteContent.setText("Type: "+ note.getNoteType()+"\n" + "Date: "+ note.getNoteDate() +"\n"+
                "Start Time: "+ note.getStartTime()+"\n"+ "Venue: "+ note.getLocation());
    }

    @Override
    public int getItemCount() {
        int size=0;
        if(noteList!=null){
            size = noteList.size();
        }
        return size;
    }

}