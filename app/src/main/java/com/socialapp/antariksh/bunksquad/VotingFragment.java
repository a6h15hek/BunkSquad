package com.socialapp.antariksh.bunksquad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.escape.CharEscaper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.Inflater;

import static androidx.core.content.ContextCompat.getCodeCacheDir;
import static androidx.core.content.ContextCompat.getSystemService;

public class VotingFragment extends Fragment {
    private View view;
    private SwipeRefreshLayout swipeRefreshMassBunkLayout;
    //view variable initialize
    private LinearLayout notLoginLayout,userLoggedInLayout;
    private ImageView UserProfileIcon;
    private LinearLayout loadingProgressBar,usernameNotFoundLayout,NoGroupPresent;
    private View dialogBoxCreateGroupFromView;
    private ProgressBar loadingProgressBarDialogBox;
    View dialogBoxJoinGroupPanelView;
    Map<String, Object> GroupDataForJoinLink;
    private VotingPollsListAdapter votingPollsListAdapter;

    //initialize Data View variable
    private FloatingActionsMenu mainFloatingActionMenu;
    private DocumentReference UserDocRef;
    private Map<String,Object> UserInfo;
    private RecyclerView groupListRecyclerView, pollsListRecyclerView;
    List<Object> groupArray;
    private String joinGroupToken=null;
    private List<String> groupIdList;
    private SharedPreferences sharedPref;

    private FirebaseFirestore fireStoreDB;
    private FirebaseUser BunkSquadUser;
    public VotingFragment(String joinGroupToken) {
        if(joinGroupToken!=null){
            this.joinGroupToken=joinGroupToken;
        }
    }
    public VotingFragment(){
        //formality
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_voting, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //initialize view variable
        UserProfileIcon=view.findViewById(R.id.UserProfileIcon);
        loadingProgressBar=view.findViewById(R.id.loadingProgressBar);
        mainFloatingActionMenu=view.findViewById(R.id.mainFloatingMenu);
        usernameNotFoundLayout=view.findViewById(R.id.usernameNotFoundErrorLayout);
        NoGroupPresent=view.findViewById(R.id.NoGroupPresent);
        notLoginLayout=view.findViewById(R.id.not_login_stat_layout);
        userLoggedInLayout = view.findViewById(R.id.userLoggedInLayout);
        //initialize data view variable;
        groupListRecyclerView=view.findViewById(R.id.groupList);
        pollsListRecyclerView = view.findViewById(R.id.pollsRecyclerView);
        groupIdList = new ArrayList<String>();
        sharedPref = getActivity().getSharedPreferences("environmentalVariable",getActivity().MODE_PRIVATE);
        //initialize firebase variable
        BunkSquadUser=FirebaseAuth.getInstance().getCurrentUser();
        setInitialFunctions();
        setWhenUserNotLoggedInFunctions();
        setWhenUserLoggedInFunctions();

        if(joinGroupToken!=null){
            setJoinGroupPanel();
        }
        //should be last in sequence
        //initialize Refresh view variable

    }

    private void setJoinGroupPanel() {
        if(BunkSquadUser==null){
            Snackbar.make(view,"You must Login to join group",Snackbar.LENGTH_LONG).show();
            return;
        }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            ViewGroup viewGroup = view.findViewById(android.R.id.content);
            dialogBoxJoinGroupPanelView = LayoutInflater.from(getActivity()).inflate(R.layout.voting_join_group_panel, viewGroup, false);
            builder.setView(dialogBoxJoinGroupPanelView);
            final AlertDialog alertJoinGroupBox = builder.create();
            alertJoinGroupBox.show();
            loadingProgressBarDialogBox=dialogBoxJoinGroupPanelView.findViewById(R.id.loadingProgressBarDialogBox);

            //initialize firestore

            final Button joinBtn=dialogBoxJoinGroupPanelView.findViewById(R.id.join_button_dialogBox);
            Button closeBtn=dialogBoxJoinGroupPanelView.findViewById(R.id.close_button_dialogBox);
            joinBtn.setEnabled(false);
            loadingProgressBarDialogBox.setVisibility(View.VISIBLE);
            final DocumentReference groupDoc=fireStoreDB.collection("BunkSquadGroups")
                    .document(joinGroupToken.substring(0,joinGroupToken.length()-5));
            groupDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    loadingProgressBarDialogBox.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                             GroupDataForJoinLink= document.getData();
                             joinBtn.setEnabled(true);//enable button for joining group
                            List<Object> memberList= (List<Object>) GroupDataForJoinLink.get("Member");
                            if(memberList.size()>0){
                                ((ImageView)dialogBoxJoinGroupPanelView.findViewById(R.id.GroupAvatar)).setImageDrawable(TextDrawableForImageView.builder()
                                        .buildRound(String.valueOf(((String) GroupDataForJoinLink.get("GroupName")).toUpperCase().charAt(0)),
                                                Color.parseColor("#8d4de9"),Color.BLACK));
                                ((TextView)dialogBoxJoinGroupPanelView.findViewById(R.id.GroupName)).setText(GroupDataForJoinLink.get("GroupName").toString());
                                for(int i=0;i<memberList.size();i++){
                                    Map<String,Object> memberInfo= (Map<String, Object>) memberList.get(i);
                                    if(memberInfo.get("role").equals("admin")){
                                        ((TextView)dialogBoxJoinGroupPanelView.findViewById(R.id.adminName)).setText(memberInfo.get("name").toString());
                                        break;
                                    }
                                }
                            }else{
                                alertJoinGroupBox.dismiss();
                                Snackbar.make(view,"Group not exist or Joining Link Expired. Request admin for new Link.\" ",Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            alertJoinGroupBox.dismiss();
                            Snackbar.make(view,"Joining Link Expired. Request admin for new Link.",Snackbar.LENGTH_LONG).show();
                        }
                    }else{
                        alertJoinGroupBox.dismiss();
                        Snackbar.make(view,"Network Error. Try after sometime",Snackbar.LENGTH_LONG).show();
                    }
                }
            });
            //set content
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertJoinGroupBox.dismiss();
                }
            });
            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Object> memberList= (List<Object>) GroupDataForJoinLink.get("Member");
                    if(memberList.size()>100){
                        Snackbar.make(view,"Group is already full.",Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    for(int i=0;i<memberList.size();i++){
                        Map<String,Object> memberInfo= (Map<String, Object>) memberList.get(i);
                        if(memberInfo.get("Id").equals(BunkSquadUser.getUid())){
                            alertJoinGroupBox.dismiss();
                            Snackbar.make(view,"You are already member of group",Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    }
                    loadingProgressBarDialogBox.setVisibility(View.VISIBLE);
                    //setting up updating database;
                    WriteBatch batch = fireStoreDB.batch();

                    final Map<String, Object> group = new HashMap<>();
                    group.put("GroupName",GroupDataForJoinLink.get("GroupName"));
                    group.put("Id",groupDoc.getId());
                    group.put("status","new Member added.");
                    groupArray.add(0,group);
                    batch.update(UserDocRef,"Groups",groupArray);

                    Map<String, Object> members = new HashMap<>();
                    members.put("name",BunkSquadUser.getDisplayName());
                    members.put("Id",BunkSquadUser.getUid());
                    members.put("role","member");
                    members.put("registerToken",sharedPref.getString("device_register_token",null));

                    Map<String, Object> tempGroupUpdate = new HashMap<>();
                    tempGroupUpdate.put("memberId",FieldValue.arrayUnion(BunkSquadUser.getUid()));//adding an array of membersId array
                    tempGroupUpdate.put("Member",FieldValue.arrayUnion(members));
                    batch.update(groupDoc,tempGroupUpdate);

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingProgressBarDialogBox.setVisibility(View.GONE);
                            alertJoinGroupBox.dismiss();
                            if(task.isSuccessful()){
                                Snackbar.make(view,"You are added in group.",Snackbar.LENGTH_SHORT).show();
                                FirebaseMessaging.getInstance().subscribeToTopic(groupDoc.getId())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Snackbar.make(view,"You are subscribed to receive notification.",Snackbar.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    private void setInitialFunctions(){
        if(BunkSquadUser!=null){
            BunkSquadUser.reload();
            if(BunkSquadUser.isEmailVerified()){

                UserProfileIcon.setVisibility(View.VISIBLE);
                notLoginLayout.setVisibility(View.GONE);
                userLoggedInLayout.setVisibility(View.VISIBLE);
                mainFloatingActionMenu.setVisibility(View.VISIBLE);
                //main code goes here
                fireStoreDB=FirebaseFirestore.getInstance();
                UserDocRef=fireStoreDB.collection("BunkSquadUserData").document(BunkSquadUser.getUid());
                setUserInfoView();
                setFloatingMenuButtonClickListener();

            }else {
                Intent intent = new Intent(getActivity(), VerifyEmailAccountActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }else{
            notLoginLayout.setVisibility(View.VISIBLE);
            userLoggedInLayout.setVisibility(View.GONE);
            UserProfileIcon.setVisibility(View.GONE);
            mainFloatingActionMenu.setVisibility(View.GONE);
        }
    }

    private void setWhenUserNotLoggedInFunctions(){
        ((Button)view.findViewById(R.id.goToSignUpPageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()){
                    Intent intent = new Intent(getActivity(), BunkSquadSignUpActivity.class);
                    startActivity(intent);
                }else {
                    Snackbar.make(view,"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        ((Button)view.findViewById(R.id.goToSignInPageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()){
                    Intent intent = new Intent(getActivity(), BunkSquadSignInActivity.class);
                    startActivity(intent);
                }else {
                    Snackbar.make(view,"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setWhenUserLoggedInFunctions(){UserProfileIcon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

                Intent intent = new Intent(getActivity(), BunkSquadUserProfileActivity.class);
                startActivity(intent);
           /* if(isNetworkConnected()){
            }else {
                Snackbar.make(view,"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
            }*/
        }
    });
    }
    private void setUserInfoView() {
        UserDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    UserInfo= snapshot.getData();
                    //checking if register token is present or not
                    String locallyStoredRegisterToken = sharedPref.getString("device_register_token","NO_TOKEN");
                    if(UserInfo.get("registerToken") == null){
                        UserDocRef.update("registerToken",locallyStoredRegisterToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            //Toast.makeText(getActivity(),"notification set.",Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        if(!(UserInfo.get("registerToken")).equals(locallyStoredRegisterToken)){
                            UserDocRef.update("registerToken",locallyStoredRegisterToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                //Toast.makeText(getActivity(),"notification set.",Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }

                    usernameNotFoundLayout.setVisibility(View.GONE);
                    if(UserInfo.get("Groups")!=null){
                        groupArray = (List<Object>) UserInfo.get("Groups");
                        if(groupArray.size()>0){
                            NoGroupPresent.setVisibility(View.GONE);
                            setGroupListOnView(groupArray);
                            for(int i=0;i<groupArray.size();i++){
                                groupIdList.add((String) ((Map<String,Object>)groupArray.get(i)).get("Id"));
                            }
                            //at this time group List view is set
                            initPollsListAdapter();
                        }else{
                            NoGroupPresent.setVisibility(View.VISIBLE);
                            setGroupListOnView(groupArray);
                            groupIdList.clear();
                            initPollsListAdapter();
                        }
                    }else{
                        groupArray=new ArrayList<Object>();
                        NoGroupPresent.setVisibility(View.VISIBLE);
                    }
                }else{
                    //if data not updated properly in database error
                    usernameNotFoundLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private class groupListSelectAdapter extends BaseAdapter{
        Context context;
        List<Object> groupList;
        groupListSelectAdapter(Context context, List<Object> groupList){
            this.context=context;
            this.groupList=groupList;
        }
        @Override
        public int getCount() {
            return groupList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=LayoutInflater.from(context);
            View listView=inflater.inflate(R.layout.group_list_adapter__select_one,parent,false);
            ImageView groupIcon=listView.findViewById(R.id.GroupAvatar);
            TextView groupName=listView.findViewById(R.id.groupName);
            Map<String,Object> groupInfo= (Map<String, Object>) groupList.get(position);
            groupIcon.setImageDrawable(TextDrawableForImageView.builder()
                    .buildRound(String.valueOf(((String) groupInfo.get("GroupName")).toUpperCase().charAt(0)),
                            Color.parseColor("#8d4de9"),Color.BLACK));
            groupName.setText((String) groupInfo.get("GroupName"));
            return listView;
        }
    }
    private void setFloatingMenuButtonClickListener() {
        FloatingActionButton CreatePoll=view.findViewById(R.id.floatingButtonCreatePoll);
        final FloatingActionButton CreateGroup=view.findViewById(R.id.floatingButtonCreateGroup);
        CreatePoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainFloatingActionMenu.collapse();
                MaterialAlertDialogBuilder selectGroup=new MaterialAlertDialogBuilder(getActivity())
                        .setTitle("Choose Group")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                if(groupArray.size()!=0){
                    selectGroup.setAdapter(new groupListSelectAdapter(getActivity(), groupArray), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getActivity(), VotingCreatePollActivity.class);
                            Map<String,Object> groupInfo=(Map<String, Object>)groupArray.get(which);
                            intent.putExtra("GroupId",String.valueOf(groupInfo.get("Id")));
                            intent.putExtra("GroupName",String.valueOf(groupInfo.get("GroupName")));
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                }else{
                    selectGroup.setMessage("You are not Member of any Group. Create your own group or Request Join Link to Admin of another group.");
                    selectGroup.setNegativeButton("Create Group", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            CreateGroup.performClick();
                        }
                    });
                }
                selectGroup.show();
            }
        });
        CreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                ViewGroup viewGroup = view.findViewById(android.R.id.content);
                dialogBoxCreateGroupFromView = LayoutInflater.from(v.getContext()).inflate(R.layout.voting_create_group, viewGroup, false);
                builder.setView(dialogBoxCreateGroupFromView);
                final AlertDialog alertCreateFormDialogBox = builder.create();
                alertCreateFormDialogBox.show();
                loadingProgressBarDialogBox=dialogBoxCreateGroupFromView.findViewById(R.id.loadingProgressBarDialogBox);
                ((ImageView)dialogBoxCreateGroupFromView.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertCreateFormDialogBox.dismiss();
                    }
                });
                ((Button)dialogBoxCreateGroupFromView.findViewById(R.id.createGroupButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextInputEditText inputGroupName=dialogBoxCreateGroupFromView.findViewById(R.id.inputGroupName);
                        final TextInputLayout inputGroupNameLayout=dialogBoxCreateGroupFromView.findViewById(R.id.inputGroupNameLayout);
                        String GroupName=inputGroupName.getText().toString();
                        inputGroupName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                inputGroupNameLayout.setErrorEnabled(false);
                            }
                        });
                        if(GroupName.isEmpty()){
                            inputGroupNameLayout.setError("Empty Input");
                            return;
                        }

                        loadingProgressBarDialogBox.setVisibility(View.VISIBLE);
                        //setting up updating database;
                        WriteBatch batch = fireStoreDB.batch();

                        final Map<String, Object> group = new HashMap<>();//group is set of values in document
                        group.put("GroupName",GroupName);
                        group.put("memberId",FieldValue.arrayUnion(BunkSquadUser.getUid()));//adding an array of membersId
                        group.put("adminId",FieldValue.arrayUnion(BunkSquadUser.getUid()));
                        group.put("status",BunkSquadUser.getDisplayName()+" Created Groups.");
                            Map<String, Object> members = new HashMap<>();
                            members.put("name",BunkSquadUser.getDisplayName());
                            members.put("Id",BunkSquadUser.getUid());
                            members.put("role","admin");
                            members.put("registerToken",sharedPref.getString("device_register_token","NO_TOKEN"));

                        group.put("Member",FieldValue.arrayUnion(members));//adding member objects in array of member present in group

                        final DocumentReference groupDoc = fireStoreDB.collection("BunkSquadGroups")
                                .document();
                        group.put("joinLink",groupDoc.getId()+String.valueOf((new Random()).nextInt(90000) + 10000));
                        batch.set(groupDoc,group);//now updating the document

                        //updating group information in user data
                        group.remove("joinLink");//remove join link and member array
                        group.remove("Member");
                        group.remove("memberId");
                        group.remove("adminId");
                        group.remove("status");

                        group.put("Id",groupDoc.getId());
                        group.put("status","You created group.");
                        groupArray.add(0,group);
                        batch.update(UserDocRef,"Groups",groupArray);
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Snackbar.make(view,"Group Created Successfully.",Snackbar.LENGTH_SHORT).show();
                                    FirebaseMessaging.getInstance().subscribeToTopic(groupDoc.getId())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Snackbar.make(view,"You are subscribed to receive notification.",Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }else{
                                    Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                alertCreateFormDialogBox.dismiss();
                                loadingProgressBarDialogBox.setVisibility(View.GONE);
                            }
                        });

                    }
                });
                mainFloatingActionMenu.collapse();
            }
        });
    }
    private void setGroupListOnView(List<Object> groups){
        VotingGroupListAdapter massBunkGroupListAdapter=
                new VotingGroupListAdapter(getContext(),groups);
        groupListRecyclerView.setAdapter(massBunkGroupListAdapter);
        groupListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
    private void initPollsListAdapter(){
        groupIdList.add("ThisTextIsToMakeListNonEmpty");
        Query query = fireStoreDB.collection("BunkSquadVoting")
                .whereIn("groupId",groupIdList)
                .whereEqualTo("isOn",true);
                //.orderBy("createdOn", Query.Direction.DESCENDING);
                //.whereGreaterThan("lastDate",Timestamp.now());

        query.orderBy("createdOn");
        FirestoreRecyclerOptions<VotingPollData> options = new FirestoreRecyclerOptions.Builder<VotingPollData>()
                .setQuery(query,VotingPollData.class)
                .build();
        if(getContext()!=null){
            votingPollsListAdapter = new VotingPollsListAdapter(options,getContext(),BunkSquadUser);
            pollsListRecyclerView.setAdapter(votingPollsListAdapter);
            votingPollsListAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //stop every listener
        //votingPollsListAdapter.stopListening();
    }
}
