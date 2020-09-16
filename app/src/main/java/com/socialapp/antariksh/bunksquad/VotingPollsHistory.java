package com.socialapp.antariksh.bunksquad;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.List;

public class VotingPollsHistory extends AppCompatActivity {
    private RecyclerView historyOfPolls;
    private  FirebaseFirestore firestoreDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_polls_history);
        firestoreDB = FirebaseFirestore.getInstance();

        historyOfPolls = findViewById(R.id.HistoryPollsRecyclerView);

        setHistoryPollListAdapter();


        //back Button click listener
        findViewById(R.id.back_btn_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setHistoryPollListAdapter() {
        Query query = firestoreDB.collection("BunkSquadVoting")
                .whereEqualTo("groupId",getIntent().getStringExtra("groupId"))
                .whereEqualTo("isOn",false).limit(15);
        //.orderBy("createdOn", Query.Direction.DESCENDING);
        //.whereGreaterThan("lastDate",Timestamp.now());

        query.orderBy("createdOn");
        FirestoreRecyclerOptions<VotingPollData> options = new FirestoreRecyclerOptions.Builder<VotingPollData>()
                .setQuery(query,VotingPollData.class)
                .build();
            VotingPollsHistoryListAdapter votingPollsHistoryListAdapter = new VotingPollsHistoryListAdapter(options,VotingPollsHistory.this);
            historyOfPolls.setAdapter(votingPollsHistoryListAdapter);
           votingPollsHistoryListAdapter.startListening();
    }
}