package com.socialapp.antariksh.bunksquad;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

public class VotedListMemberAdapter extends RecyclerView.Adapter<VotedListMemberAdapter.ListViewHolder> {
    private static final String TAG = "abhishek";
    private Context context;
    private List<Object>  votersMembers;
    private Map<String, Object> votedMembers;
    private String optionColor[] = {"#e53935","#8d4de9","#ffb200","#5bb381","#FF007F"};
    VotedListMemberAdapter(Context context, List<Object> votersMembers, Map<String, Object> votedMembers){
        this.context = context;
        this.votersMembers =votersMembers;
        this.votedMembers = votedMembers;
    }
    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.voted_member_list_row,parent,false);
        return new VotedListMemberAdapter.ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Map<String,Object> vote = (Map<String, Object>) votersMembers.get(position);
        holder.nameOfVoter.setText((String)vote.get("name"));
        if(votedMembers!=null){
            if(votedMembers.get(vote.get("Id"))!=null){
                Map<String,Object> voted = (Map<String, Object>) votedMembers.get(vote.get("Id"));
                holder.dateTimeOfVote.setText(DateFormat.format("d MMM, h:mm a",((Timestamp)voted.get("t")).toDate()));
                holder.colorOfVotes.setBackgroundColor(Color.parseColor(optionColor[ ((Long) voted.get("v")).intValue()]));
            }else{
                holder.dateTimeOfVote.setText("Not voted yet");
                holder.colorOfVotes.setBackgroundColor(Color.parseColor("#4f5354"));
            }
        }else{
            holder.dateTimeOfVote.setText("Not voted yet");
            holder.colorOfVotes.setBackgroundColor(Color.parseColor("#4f5354"));
        }
    }

    @Override
    public int getItemCount() {
        return votersMembers.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView nameOfVoter,dateTimeOfVote;
        LinearLayout colorOfVotes;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            nameOfVoter = itemView.findViewById(R.id.nameOfVoter);
            dateTimeOfVote = itemView.findViewById(R.id.dateAndTimeOfVote);
            colorOfVotes = itemView.findViewById(R.id.colorOfVote);
        }
    }
}
