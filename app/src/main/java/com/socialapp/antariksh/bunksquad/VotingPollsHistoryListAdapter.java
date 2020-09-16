package com.socialapp.antariksh.bunksquad;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class VotingPollsHistoryListAdapter extends FirestoreRecyclerAdapter<VotingPollData, VotingPollsHistoryListAdapter.PollsViewHolder> {
    private String optionColor[] = {"#e53935","#8d4de9","#ffb200","#5bb381","#FF007F"};
    Context context;
    public VotingPollsHistoryListAdapter(@NonNull FirestoreRecyclerOptions<VotingPollData> options, Context context) {
        super(options);
        this.context =context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onBindViewHolder(@NonNull final PollsViewHolder holder, int position, @NonNull final VotingPollData poll) {
        holder.titleOfPoll.setText(Html.fromHtml("<font color='#8d4de9'><b>Q. </b></font>" +poll.getTitle()));
        holder.groupName.setText(Html.fromHtml("<font color='#8d4de9'><b>\uD83D\uDCCE </b></font>"+poll.getGroupName()));
        holder.descriptionOfPoll.setText(poll.getDescription());
        String VotedBy = "No one has given vote yet";
        if(poll.getVotedBy()!=null){
            VotedBy = String.join(", ", poll.getVotedBy());
        }
        holder.secondDescriptionPoll.setText("Last date : "+ DateFormat.format("d MMM, h:mm a",poll.getLastDate().toDate())+
                "\nCreated On : "+DateFormat.format("d MMM, h:mm a",poll.getCreatedOn().toDate())+
                "\nCreated By : "+poll.getCreatedBy()+"\nVoted : "+VotedBy+"\nOption : "+poll.getOptionAnswer());
        holder.showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VotingPollDetailReport.class);
                intent.putExtra("title",poll.getTitle());
                intent.putExtra("description",holder.descriptionOfPoll.getText());
                intent.putExtra("secondDescription",holder.secondDescriptionPoll.getText());
                intent.putExtra("numberOfVotesArray", (ArrayList<Integer>) poll.getNumberOfVotes());
                intent.putExtra("optionName", (ArrayList<Object>) poll.getOptionAnswer());
                intent.putExtra("allVotes", (Serializable)poll.getVote());
                intent.putExtra("groupDocumentId",poll.getGroupId());
                context.startActivity(intent);
            }
        });
        holder.rankView.setText(Html.fromHtml(getRankString(poll.getNumberOfVotes())));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getRankString(List<Integer> votes){
        String numFont[] = {"➊","➋","➌","➍","➎"};
        List<Integer> sorted = new ArrayList<>(votes);
        List<Integer> copy = new ArrayList<>(votes);
        sorted.sort(Collections.<Integer>reverseOrder());
        String rankString = "";
        int currentRank = 0;
        for(int i=0;i<sorted.size();i++){
            int colorIndex = copy.indexOf(sorted.get(i));
            copy.set(colorIndex,-1);
            if(i!=0&&sorted.get(i)!=sorted.get(i-1)){
                currentRank++;
            }
            rankString+=("<font  color='"+optionColor[colorIndex]+"'><b>"+numFont[currentRank]+"</b></font> ");
        }
        return rankString;
    }
    @NonNull
    @Override
    public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.voting_poll_history_row,parent,false);
        return new VotingPollsHistoryListAdapter.PollsViewHolder(view);
    }

    public class PollsViewHolder extends RecyclerView.ViewHolder {
        TextView titleOfPoll,descriptionOfPoll,groupName,secondDescriptionPoll,rankView,statusOfPoll;
        LinearLayout showMoreButton;
        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleOfPoll = itemView.findViewById(R.id.titleOfPoll);
            descriptionOfPoll = itemView.findViewById(R.id.descriptionOfPoll);
            groupName = itemView.findViewById(R.id.groupName);
            showMoreButton = itemView.findViewById(R.id.showMoreDetails);
            secondDescriptionPoll = itemView.findViewById(R.id.secondDescriptionOfPoll);
            rankView = itemView.findViewById(R.id.rankView);
            statusOfPoll = itemView.findViewById(R.id.statusOfPoll);
        }
    }
}
