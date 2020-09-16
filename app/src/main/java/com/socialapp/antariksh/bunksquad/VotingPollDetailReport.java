package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VotingPollDetailReport extends AppCompatActivity {
    private TextView titleOfReport,descriptionOfReport,secondDescriptionOfReport;
    private PieChart resultPieChart;
    private FirebaseFirestore fireStoreDB;
    private DocumentReference GroupDocRef;
    private LinearLayout groupDoesNotExistView;
    private  Intent intentData;
    private RecyclerView votedListRecyclerView;
    private int[] colorArray;
    private TextView optionMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_poll_detail_report);
        titleOfReport = findViewById(R.id.titleOfPoll);
        resultPieChart = findViewById(R.id.pollsResultChart);
        groupDoesNotExistView = findViewById(R.id.groupDoesNotExistView);
        fireStoreDB=FirebaseFirestore.getInstance();
        votedListRecyclerView = findViewById(R.id.votedListView);
        descriptionOfReport = findViewById(R.id.descriptionOfPoll);
        secondDescriptionOfReport = findViewById(R.id.secondDescriptionOfPoll);
        optionMenu = findViewById(R.id.OptionMenu);

        intentData = getIntent();
        String groupDocumentId = intentData.getStringExtra("groupDocumentId");
        GroupDocRef=fireStoreDB.collection("BunkSquadGroups").document(groupDocumentId);
        setGroupData();

        titleOfReport.setText(Html.fromHtml("<font color='#8d4de9'><b>Q. </b></font>")+intentData.getStringExtra("title"));
        descriptionOfReport.setText(intentData.getStringExtra("description"));
        secondDescriptionOfReport.setText(intentData.getStringExtra("secondDescription"));

        resultPieChart.setCenterText("Result");
        resultPieChart.setCenterTextSize(22);
        resultPieChart.setDrawEntryLabels(false);
        resultPieChart.getLegend().setTextSize(18);
        resultPieChart.getLegend().setWordWrapEnabled(true);
        resultPieChart.getDescription().setEnabled(false);
        colorArray = new int[]{Color.parseColor("#e53935"),Color.parseColor("#8d4de9"),Color.parseColor("#ffb200"),Color.parseColor("#5bb381"),Color.parseColor("#FF007F"),Color.parseColor("#4f5354")};


        //back Button click listener
        findViewById(R.id.back_btn_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(intentData.getBooleanExtra("isCreator",false)){
            optionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(VotingPollDetailReport.this,optionMenu);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.polls_option_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.deletePoll:
                                    //jump to polls history
                                    deletePollDialogBox();
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }else{
            optionMenu.setVisibility(View.GONE);
        }

    }

    private void deletePollDialogBox() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        deletePollConfirmed();
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VotingPollDetailReport.this);
        builder.setTitle("Delete Poll").setMessage("Are you sure? You want to delete this poll").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deletePollConfirmed() {
        fireStoreDB.collection("BunkSquadVoting").document(intentData.getStringExtra("documentId"))
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(VotingPollDetailReport.this,"Poll deleted successfully.",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(VotingPollDetailReport.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setGroupData() {
        GroupDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    groupDoesNotExistView.setVisibility(View.GONE);
                    Map<String, Object> groupData = snapshot.getData();

                    PieDataSet pieDataSet = new PieDataSet(setPieChartData(intentData.getIntegerArrayListExtra("numberOfVotesArray"),intentData.getStringArrayListExtra("optionName"),((List<Object>)groupData.get("Member")).size()),"");
                    int[] tempColorArray = Arrays.copyOfRange(colorArray, 0, ((List<Integer>) intentData.getIntegerArrayListExtra("numberOfVotesArray")).size()+1);
                    tempColorArray[((List<Integer>) intentData.getIntegerArrayListExtra("numberOfVotesArray")).size()]= Color.parseColor("#4f5354");

                    pieDataSet.setColors(tempColorArray);
                    //pieDataSet.setColors(colorArray);
                    PieData pieData = new PieData(pieDataSet);
                    pieData.setValueTextSize(17);
                    pieData.setValueTextColor(Color.parseColor("#ffffff"));
                    pieData.setValueTypeface(Typeface.DEFAULT_BOLD);
                    resultPieChart.setData(pieData);
                    resultPieChart.invalidate();

                    setVotedListAdapter((List<Object>) groupData.get("Member"), (Map<String, Object>) intentData.getSerializableExtra("allVotes"));
                } else {
                    //current data is null
                    groupDoesNotExistView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private ArrayList<PieEntry> setPieChartData(List<Integer> numberOfVotes, ArrayList<String> optionAnswer, int numberOfMember){
        ArrayList<PieEntry> dataVals = new ArrayList<PieEntry>();
        int totalNumberOfVotes =0;
        for(int i=0;i<numberOfVotes.size();i++){
            dataVals.add(new PieEntry(numberOfVotes.get(i),(String) optionAnswer.get(i)));
            totalNumberOfVotes+=numberOfVotes.get(i);
        }
        dataVals.add(new PieEntry(numberOfMember-totalNumberOfVotes,"Not Voted"));
        return dataVals;
    }
    private void setVotedListAdapter(List<Object> votersMembers,Map<String,Object> VotedMembers){
        VotedListMemberAdapter massBunkGroupListAdapter=
                new VotedListMemberAdapter(VotingPollDetailReport.this,votersMembers,VotedMembers);
        votedListRecyclerView.setAdapter(massBunkGroupListAdapter);
        votedListRecyclerView.setLayoutManager(new LinearLayoutManager(VotingPollDetailReport.this));
    }
}