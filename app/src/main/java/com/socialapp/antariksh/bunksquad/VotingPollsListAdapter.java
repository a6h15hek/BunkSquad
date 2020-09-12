package com.socialapp.antariksh.bunksquad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingPollsListAdapter extends FirestoreRecyclerAdapter<VotingPollData, VotingPollsListAdapter.PollsViewHolder> {
    private static final String TAG = "abhishek";
    private Context context;
    FirebaseFirestore fireStoreDB;
    FirebaseUser bunkSquadUser;
    public VotingPollsListAdapter(@NonNull FirestoreRecyclerOptions<VotingPollData> options, Context context, FirebaseUser bunkSquadUser) {
        super(options);
        this.context=context;
        this.fireStoreDB = FirebaseFirestore.getInstance();
        this.bunkSquadUser = bunkSquadUser;
    }
    private String optionColor[] = {"#e53935","#8d4de9","#ffb200","#5bb381","#4f5354"};
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onBindViewHolder(@NonNull PollsViewHolder holder, int position, @NonNull VotingPollData poll) {
        holder.titleOfPoll.setText(Html.fromHtml("<font color='#8d4de9'><b>Q. </b></font>" +poll.getTitle()));
        HashMap<String,Object> voteData = null;
        if(poll.getVote() != null){
            voteData = (HashMap<String, Object>) poll.getVote().get(bunkSquadUser.getUid());
        }
        setHeightOfOptionList(((List<Object>)poll.getOptionAnswer()).size(),holder.heightOfOptionList);
        if(poll.getIsOn()){
            if((poll.getLastDate().getSeconds()+3600)<=Timestamp.now().getSeconds()){
                //make getIsOn false and remove from list
                DocumentReference pollsDocument=fireStoreDB.collection("BunkSquadVoting").document(getSnapshots().getSnapshot(position).getId());
                pollsDocument.update("isOn",false).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(context, "update successful.", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else if(poll.getLastDate().getSeconds()<=Timestamp.now().getSeconds()){
                //show the results
                holder.optionList.setAdapter(new answerOptionList(context, poll.getOptionAnswer(),getSnapshots().getSnapshot(position).getId(),bunkSquadUser,voteData,poll.getNumberOfVotes(),true));
                holder.statusOfPoll.setVisibility(View.VISIBLE);
            }else{
                holder.optionList.setAdapter(new answerOptionList(context, poll.getOptionAnswer(),getSnapshots().getSnapshot(position).getId(),bunkSquadUser,voteData,poll.getNumberOfVotes(),false));
                holder.statusOfPoll.setVisibility(View.GONE);
            }
        }
        holder.groupName.setText(Html.fromHtml("<font color='#8d4de9'><b>\uD83D\uDCCE </b></font>"+poll.getGroupName()));
        holder.descriptionOfPoll.setText(poll.getDescription());
        String VotedBy = "No one has given vote yet";
        if(poll.getVotedBy()!=null){
            VotedBy = String.join(", ", poll.getVotedBy());
        }
        holder.secondDescriptionPoll.setText("Last date : "+DateFormat.format("d MMM, h:mm a",poll.getLastDate().toDate())+"\nCreated On : "+DateFormat.format("d MMM, h:mm a",poll.getCreatedOn().toDate())+"\nCreated By : "+poll.getCreatedBy()+"\nVoted : "+VotedBy);
        holder.showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VotingPollDetailReport.class);
//                intent.putExtra("classAttended",String.valueOf(ClassAttended));
//                intent.putExtra("classTotal",String.valueOf(ClassTotal));
                context.startActivity(intent);
            }
        });
        Log.d(TAG, "onBindViewHolder: last date "+poll.getLastDate());
        Log.d(TAG, "onBindViewHolder: current : "+Timestamp.now());
        holder.rankView.setText(Html.fromHtml(getRankString(poll.getNumberOfVotes())));


//        if(poll.getIsOn()&&poll.getLastDate().getSeconds()<=Timestamp.now().getSeconds()){
//            DocumentReference pollsDocument=fireStoreDB.collection("BunkSquadVoting").document(getSnapshots().getSnapshot(position).getId());
//            pollsDocument.update("isOn",false).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()){
//                        Toast.makeText(context, "update successful.", Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
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
    private void setHeightOfOptionList(int noOfOption, LinearLayout optionLayout){
        LinearLayout.LayoutParams param= (LinearLayout.LayoutParams) optionLayout.getLayoutParams();
        switch (noOfOption){
            case 2:
                param.height=175;
                break;
            case 3:
                param.height=255;
                break;
            case 4:
                param.height=325;
                break;
            case 5:
                param.height=405;
                break;
            default:
                param.height=0;
                break;
        }
    }
    @NonNull
    @Override
    public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.voting_poll_row,parent,false);
        return new PollsViewHolder(view);
    }

    class PollsViewHolder extends RecyclerView.ViewHolder {
        TextView titleOfPoll,descriptionOfPoll,groupName,secondDescriptionPoll,rankView,statusOfPoll;
        ListView optionList;
        LinearLayout heightOfOptionList,showMoreButton;
        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleOfPoll = itemView.findViewById(R.id.titleOfPoll);
            optionList = itemView.findViewById(R.id.answerOptionListView);
            descriptionOfPoll = itemView.findViewById(R.id.descriptionOfPoll);
            groupName = itemView.findViewById(R.id.groupName);
            heightOfOptionList = itemView.findViewById(R.id.heightOfOptionList);
            showMoreButton = itemView.findViewById(R.id.showMoreDetails);
            secondDescriptionPoll = itemView.findViewById(R.id.secondDescriptionOfPoll);
            rankView = itemView.findViewById(R.id.rankView);
            statusOfPoll = itemView.findViewById(R.id.statusOfPoll);
        }
    }

    private class answerOptionList extends BaseAdapter {
        private final List<Integer> numberOfVotes;
        private final FirebaseUser bunkSquadUser;
        Context context;
        List<Object> optionList;
        String documentId;
        HashMap<String, Object> voteData;
        Boolean showResult;
        answerOptionList(Context context, List<Object> optionList, String documentId, FirebaseUser bunkSquadUser, HashMap<String, Object> voteData, List<Integer> numberOfVotes,Boolean showResult){
            this.context=context;
            this.optionList=optionList;
            this.documentId = documentId;
            this.bunkSquadUser = bunkSquadUser;
            this.voteData = voteData;
            this.numberOfVotes = numberOfVotes;
            this.showResult = showResult;
        }
        @Override
        public int getCount() {
            return optionList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        private int previousVoteIndex = -1;
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            LayoutInflater inflater=LayoutInflater.from(context);
            final View listView = inflater.inflate(R.layout.voting_poll_option_row,parent,false);
            final TextView option = listView.findViewById(R.id.answerOption);
            final LinearLayout colorOfOption = listView.findViewById(R.id.colorOfOption);
            LinearLayout answerOptions =  listView.findViewById(R.id.colorOfOptionBorder);
            final TextView selectedOption = listView.findViewById(R.id.selectedOption);
            TextView percentageOfPoll = listView.findViewById(R.id.percentagePoll);

            colorOfOption.setBackgroundColor(Color.parseColor(optionColor[position]));
            option.setText((String) optionList.get(position));

            if(voteData!=null){
                previousVoteIndex = ((Long) voteData.get("v")).intValue();
                if(position == previousVoteIndex){
                    option.setTextColor(Color.parseColor(optionColor[position]));
                    option.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    colorOfOption.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,0.1f));
                    selectedOption.setText("✔");
                }
            }

            if(showResult == true){
                if(numberOfVotes!=null){
                    percentageOfPoll.setText(String.valueOf(numberOfVotes.get(position))+"🔒");
                }
                //showing result
                answerOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context,"Poll expired you cannot submit or change vote.",Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                if(numberOfVotes!=null){
                    percentageOfPoll.setText(String.valueOf(numberOfVotes.get(position)));
                }
                answerOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(previousVoteIndex == position){
                            Toast.makeText(context,"This response is already selected.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int noOfTimesVoted = 0;
                        if(voteData!=null){
                            noOfTimesVoted = ((Long) voteData.get("nc")).intValue();
                            if(noOfTimesVoted>=2){
                                int timeGap = (int) (Timestamp.now().getSeconds() - ((Timestamp)voteData.get("t")).getSeconds());
                                switch (noOfTimesVoted){
                                    case 2:
                                        if(timeGap<1800){
                                            Toast.makeText(context,"To change vote for "+Html.fromHtml((noOfTimesVoted+1)+"<sup>rd</sup>")+" time wait for "+(30-(timeGap/60)) +" min." ,Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        break;
                                    case 3:
                                        if(timeGap<3600){
                                            Toast.makeText(context,"To change vote for "+Html.fromHtml((noOfTimesVoted+1)+"<sup>th</sup>")+" time wait for "+(60-(timeGap/60)) +" min." ,Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        break;
                                    default:
                                        if(timeGap<5400){
                                            Toast.makeText(context,"To change vote for "+Html.fromHtml(noOfTimesVoted+"<sup>th</sup>")+" time wait for "+(90-(timeGap/60)) +" min." ,Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                }
                            }
                        }
                        DocumentReference pollsDocument=fireStoreDB.collection("BunkSquadVoting").document(documentId);
                        if(previousVoteIndex!=-1){
                            numberOfVotes.set(previousVoteIndex,numberOfVotes.get(previousVoteIndex)-1);
                        }
                        numberOfVotes.set(position,numberOfVotes.get(position)+1);
                        Map<String, Object> pollVote = new HashMap<>();
                        Map<String, Object> vote = new HashMap<>();
                        Map<String, Object> PollDataUpdate= new HashMap<>();
                        vote.put("v",position);
                        vote.put("t", Timestamp.now());
                        if(voteData!=null){
                            vote.put("nc",noOfTimesVoted+1);
                        }else{
                            vote.put("nc",1);
                        }
                        pollVote.put(bunkSquadUser.getUid(), vote);

                        PollDataUpdate.put("vote",pollVote);
                        PollDataUpdate.put("NumberOfVotes",numberOfVotes);
                        PollDataUpdate.put("VotedBy", FieldValue.arrayUnion(bunkSquadUser.getDisplayName()));
                        pollsDocument.update(PollDataUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,"Your Response has been submitted.",Toast.LENGTH_SHORT).show();
                                    //notifyDataSetChanged();
                                }else{
                                    Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
            return listView;
        }
        @SuppressLint("ResourceAsColor")
        private void unSelectAllOption(TextView option, LinearLayout colorOfOption, TextView selectedOption){
            option.setTextColor(R.color.colorPrimaryDark);
            option.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            colorOfOption.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,0.03f));
            selectedOption.setText("");
        }

    }
}
