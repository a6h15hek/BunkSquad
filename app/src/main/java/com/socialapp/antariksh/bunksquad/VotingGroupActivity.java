package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class  VotingGroupActivity extends AppCompatActivity {
    private TextView optionMenu,groupName,noOfParticipant;
    private ImageView groupAvatar;
    private RecyclerView memberListRecyclerView;
    private LinearLayout mainContentGroupLayout,deletedGroupLayout;
    private FirebaseFirestore fireStoreDB;
    private DocumentReference GroupDocRef;
    private DocumentReference CurrentUserRef;
    private SharedPreferences sharedPref;

    private Map<String,Object> GroupInfo;
    private Map<String ,Object> currentUserInfo;
    private LinearLayout inviteLinkLayout;
    private View dialogBoxChangeGroupNameView;
    private View groupInviteLinkView;
    private List<Map<String, Object>> memberArrayInfoMap;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_group);
        mainContentGroupLayout=findViewById(R.id.mainContentGroupLayout);
        optionMenu=findViewById(R.id.OptionMenu);
        groupAvatar=findViewById(R.id.GroupAvatar);
        memberListRecyclerView=findViewById(R.id.memberList);
        groupName=findViewById(R.id.groupName);
        inviteLinkLayout=findViewById(R.id.inviteLink);
        deletedGroupLayout=findViewById(R.id.deletedGroupLayout);
        noOfParticipant = findViewById(R.id.noOfParticipant);

        sharedPref = getSharedPreferences("environmentalVariable",MODE_PRIVATE);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        fireStoreDB=FirebaseFirestore.getInstance();
        GroupDocRef=fireStoreDB.collection("BunkSquadGroups").document(getIntent().getStringExtra("groupId"));
        CurrentUserRef=fireStoreDB.collection("BunkSquadUserData").document(firebaseUser.getUid());
        setGroupData();
        ((ImageView)findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                    GroupInfo= snapshot.getData();
                    //function to change group name in users data
                    updateNameOfGroupIfChanged(GroupInfo.get("GroupName").toString());
                    //******************************************
                    groupName.setText((String) GroupInfo.get("GroupName"));
                    groupAvatar.setImageDrawable(TextDrawableForImageView.builder()
                            .buildRound(String.valueOf(((String) GroupInfo.get("GroupName")).toUpperCase().charAt(0)),
                                    Color.parseColor("#8d4de9"),Color.BLACK));
                    if(GroupInfo.get("Member")!=null){
                        memberArrayInfoMap = (List<Map<String, Object>>) GroupInfo.get("Member");
                        noOfParticipant.setText(String.valueOf(memberArrayInfoMap.size()));
                        if(memberArrayInfoMap.size()>0){
                            setGroupJoinLink();
                            for (int i=0;i<memberArrayInfoMap.size();i++){
                                if(memberArrayInfoMap.get(i).get("Id").equals(firebaseUser.getUid())){
                                    currentUserInfo=memberArrayInfoMap.get(i);
                                    if(!currentUserInfo.get("name").equals(firebaseUser.getDisplayName())){
                                        //change the name of user in group if not same
                                        memberArrayInfoMap.get(i).put("name",firebaseUser.getDisplayName());

                                        GroupDocRef.update("Member",memberArrayInfoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Snackbar.make(findViewById(android.R.id.content),"name updated in group.",Snackbar.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(VotingGroupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    String locallyStoredRegisterToken = sharedPref.getString("device_register_token","NO_TOKEN");
                                    if(!currentUserInfo.get("registerToken").equals(locallyStoredRegisterToken)){
                                        //change the name of user in group if not same
                                        memberArrayInfoMap.get(i).put("registerToken",locallyStoredRegisterToken);

                                        GroupDocRef.update("Member",memberArrayInfoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Snackbar.make(findViewById(android.R.id.content),"Notification Updated.",Snackbar.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(VotingGroupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            if(currentUserInfo==null){
                                //no of user in group is 0
                                mainContentGroupLayout.setVisibility(View.GONE);
                                deletedGroupLayout.setVisibility(View.VISIBLE);
                                setDeletedGroupExitPanel("Member Removed","Admin removed you from group");
                                return;
                            }
                            setMemberListView(memberArrayInfoMap);
                            if(currentUserInfo.get("role").equals("admin")){
                                setOptionMenuForAdmin();
                            }else{
                                setOptionMenuForMember();
                                inviteLinkLayout.setVisibility(View.GONE);
                            }
                        }else{
                            //no of user in group is 0
                            mainContentGroupLayout.setVisibility(View.GONE);
                            deletedGroupLayout.setVisibility(View.VISIBLE);
                            setDeletedGroupExitPanel("Group Not Exist","The group has been deleted.");
                        }
                    }else{
                        //no memeber is in the group
                        mainContentGroupLayout.setVisibility(View.GONE);
                        deletedGroupLayout.setVisibility(View.VISIBLE);
                        setDeletedGroupExitPanel("Group Not Exist","The group has been deleted.");
                    }
                }else{
                    //if document not present or group deleted !!!important
                    mainContentGroupLayout.setVisibility(View.GONE);
                    deletedGroupLayout.setVisibility(View.VISIBLE);
                    setDeletedGroupExitPanel("Group Not Exist","The group has been deleted.");
                }
            }
        });
    }

    private void setGroupJoinLink() {
        inviteLinkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VotingGroupActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                groupInviteLinkView = LayoutInflater.from(v.getContext()).inflate(R.layout.group_join_link_voting, viewGroup, false);
                builder.setView(groupInviteLinkView);
                final AlertDialog InviteLinkDialogBox = builder.create();
                InviteLinkDialogBox.show();
                ProgressBar loadingProgressBarDialogBox = groupInviteLinkView.findViewById(R.id.loadingProgressBarDialogBox);
                ((Button)groupInviteLinkView.findViewById(R.id.close_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InviteLinkDialogBox.dismiss();
                    }
                });
                final TextView joiningLink=groupInviteLinkView.findViewById(R.id.joinLink);
                joiningLink.setText("https://group.bunksquad.com/"+(String)GroupInfo.get("joinLink"));
                LinearLayout copyLinkLayout,shareLinkLayout,resetLinkLayout;
                copyLinkLayout=groupInviteLinkView.findViewById(R.id.copyLink);
                shareLinkLayout=groupInviteLinkView.findViewById(R.id.shareLink);
                resetLinkLayout=groupInviteLinkView.findViewById(R.id.resetLink);
                copyLinkLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(getApplication().CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("BunkSquad",joiningLink.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(VotingGroupActivity.this,"Link copied to Clipboard.",Toast.LENGTH_SHORT).show();
                    }
                });
                shareLinkLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent joiningLinkIntent = new Intent();
                        joiningLinkIntent.setAction(Intent.ACTION_SEND);
                        joiningLinkIntent.setType("text/plain");
                        joiningLinkIntent.putExtra(Intent.EXTRA_TEXT,joiningLink.getText().toString());
                        startActivity(Intent.createChooser(joiningLinkIntent, "Share via"));
                    }
                });
                resetLinkLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        final String joinLinkUniqueId=GroupDocRef.getId()+String.valueOf((new Random()).nextInt(90000) + 10000);
                                        GroupDocRef.update("joinLink",joinLinkUniqueId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    joiningLink.setText("https://group.bunksquad.com/"+joinLinkUniqueId);
                                                }else{
                                                    Toast.makeText(VotingGroupActivity.this,"Error in reset link.",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        dialog.dismiss();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        };
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(groupInviteLinkView.getContext());
                        builder.setTitle("Reset Link").setMessage("Are you sure you want to revoke the invite link for this group? If you revoke the link, no one will be able to use current link to join this group. New Link will be Created.").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }
                });
            }
        });
    }
    private void setDeletedGroupExitPanel(String Heading,String SubHeading) {
        ((TextView)findViewById(R.id.groupNotExistHeading)).setText(Heading);
        ((TextView)findViewById(R.id.groupNotExistSubHeading)).setText(SubHeading);
        ((Button)findViewById(R.id.removeGroupFromList)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final WriteBatch batch = fireStoreDB.batch();
                CurrentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                List<Map<String,Object>> group=(List<Map<String,Object>>)document.get("Groups");
                                for(int i=0;i<group.size();i++){
                                    if(group.get(i).get("Id").equals(getIntent().getStringExtra("groupId"))){
                                        group.remove(i);
                                        batch.update(CurrentUserRef,"Groups",group);
                                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                deletedGroupLayout.setVisibility(View.GONE);
                                                if(task.isSuccessful()){
                                                    Snackbar.make(findViewById(android.R.id.content),"Group Deleted.",Snackbar.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(VotingGroupActivity.this,"Cannot delete Group. "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                                finish();
                                            }
                                        });
                                        return;
                                    }
                                    Snackbar.make(findViewById(android.R.id.content),"Error restart the app.",Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
            }
        });
    }
    private void setMemberListView(List<Map<String,Object>> memberArrayMap) {
        VotingMemberListAdapter MemberListListAdapter=
                new VotingMemberListAdapter(VotingGroupActivity.this,memberArrayMap,currentUserInfo,getIntent().getStringExtra("groupId"));
        memberListRecyclerView.setAdapter(MemberListListAdapter);
        memberListRecyclerView.setLayoutManager(new LinearLayoutManager(VotingGroupActivity.this));
    }

    private void setOptionMenuForAdmin() {
        optionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(VotingGroupActivity.this,optionMenu);
                //inflating menu from xml resource
                popup.inflate(R.menu.group_option_menu_for_admin);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.votingPollsHistory:
                                //jump to polls history
                                jumpToPollsHistoryActivity();
                                break;
                            case R.id.changeGroupNameOption:
                                openGroupNameChangePanel();
                                break;
                            case R.id.leaveGroupOption:
                                leaveGroup();
                                break;
                            case R.id.deleteGroupOption:
                                deleteGroupForAdmins();
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    private void jumpToPollsHistoryActivity(){
        Intent intent = new Intent(VotingGroupActivity.this, VotingPollsHistory.class);
        intent.putExtra("groupId",getIntent().getStringExtra("groupId"));
        startActivity(intent);
    }
    private void deleteGroupForAdmins() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if(!currentUserInfo.get("role").equals("admin")){
                            Snackbar.make(findViewById(android.R.id.content),"You are not admin.",Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        deleteGroup();
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VotingGroupActivity.this);
        builder.setTitle("Delete").setMessage("Are you sure? You want to delete this group").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    private void deleteGroup(){
        final WriteBatch batch = fireStoreDB.batch();
        batch.delete(GroupDocRef);
        CurrentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    if(document.exists()){
                        List<Map<String,Object>> group=(List<Map<String,Object>>)document.get("Groups");
                        for(int i=0;i<group.size();i++){
                            if(group.get(i).get("Id").equals(GroupDocRef.getId())){
                                group.remove(i);
                                batch.update(CurrentUserRef,"Groups",group);
                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            finish();
                                            Snackbar.make(findViewById(android.R.id.content),"Group Deleted",Snackbar.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(VotingGroupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                return;
                            }
                            Snackbar.make(findViewById(android.R.id.content),"Error restart the app.",Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }
    private void leaveGroup() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Map<String,Object> tempGroupUpdate=new HashMap<>();
                        for(int i=0;i<memberArrayInfoMap.size();i++){
                            if(memberArrayInfoMap.get(i).get("Id").equals(firebaseUser.getUid())){
                                if(memberArrayInfoMap.get(i).get("role").equals("admin")){
                                    if(i==0&&memberArrayInfoMap.size()==1){
                                        deleteGroup();
                                        return;
                                    }else if(i==0&&memberArrayInfoMap.size()>1){
                                       memberArrayInfoMap.get(1).put("role","admin");
                                   }else if(i>0){
                                       memberArrayInfoMap.get(i-1).put("role","admin");
                                   }
                                    tempGroupUpdate.put("adminId",FieldValue.arrayRemove(currentUserInfo.get("Id")));
                                }
                                memberArrayInfoMap.remove(i);
                                tempGroupUpdate.put("Member",memberArrayInfoMap);
                                tempGroupUpdate.put("memberId",FieldValue.arrayRemove(currentUserInfo.get("Id")));
                                if(currentUserInfo.get("role").equals("admin")){
                                    tempGroupUpdate.put("adminId",FieldValue.arrayRemove(currentUserInfo.get("Id")));
                                }
                                final WriteBatch batch = fireStoreDB.batch();
                                batch.update(GroupDocRef,tempGroupUpdate);
                                CurrentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document=task.getResult();
                                            if(document.exists()){
                                                List<Map<String,Object>> group=(List<Map<String,Object>>)document.get("Groups");
                                                for(int i=0;i<group.size();i++){
                                                    if(group.get(i).get("Id").equals(GroupDocRef.getId())){
                                                        group.remove(i);
                                                        batch.update(CurrentUserRef,"Groups",group);
                                                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    finish();
                                                                    Snackbar.make(findViewById(android.R.id.content),"Group Name updated.",Snackbar.LENGTH_SHORT).show();
                                                                }else{
                                                                    Toast.makeText(VotingGroupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                        return;
                                                    }
                                                    Snackbar.make(findViewById(android.R.id.content),"Error restart the app.",Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    }
                                });
                                break;
                            }
                        }
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        break;
                }
            }
        };
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VotingGroupActivity.this);
        builder.setTitle("Leave Group").setMessage("Are you sure? You want to leave this group").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void setOptionMenuForMember() {
        optionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(VotingGroupActivity.this,optionMenu);
                //inflating menu from xml resource
                popup.inflate(R.menu.group_option_menu_for_member);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.votingPollsHistory:
                                //jump to polls history
                                jumpToPollsHistoryActivity();
                                break;
                            case R.id.leaveGroupOption:
                                leaveGroup();
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    private void openGroupNameChangePanel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VotingGroupActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        dialogBoxChangeGroupNameView = LayoutInflater.from(VotingGroupActivity.this).inflate(R.layout.voting_group_name_change, viewGroup, false);
        builder.setView( dialogBoxChangeGroupNameView);
        final AlertDialog alertChangeNameDialogBox = builder.create();
        alertChangeNameDialogBox.show();
        final ProgressBar loadingProgressBarDialogBox=dialogBoxChangeGroupNameView.findViewById(R.id.loadingProgressBarDialogBox);
        ((ImageView)dialogBoxChangeGroupNameView.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertChangeNameDialogBox.dismiss();
            }
        });
        ((Button)dialogBoxChangeGroupNameView.findViewById(R.id.changeGroupNameButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText inputGroupName=dialogBoxChangeGroupNameView.findViewById(R.id.inputGroupName);
                final TextInputLayout inputGroupNameLayout=dialogBoxChangeGroupNameView.findViewById(R.id.inputGroupNameLayout);
                final String GroupName=inputGroupName.getText().toString();
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
                final WriteBatch batch = fireStoreDB.batch();
                batch.update(GroupDocRef,"GroupName",GroupName);
                CurrentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document=task.getResult();
                            if(document.exists()){
                                List<Map<String,Object>> group=(List<Map<String,Object>>)document.get("Groups");
                                for(int i=0;i<group.size();i++){
                                    if(group.get(i).get("Id").equals(GroupDocRef.getId())){
                                        group.get(i).put("GroupName",GroupName);
                                        group.get(i).put("status","group name changed.");
                                        batch.update(CurrentUserRef,"Groups",group);
                                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Snackbar.make(findViewById(android.R.id.content),"Group Name updated.",Snackbar.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(VotingGroupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                                alertChangeNameDialogBox.dismiss();
                                                loadingProgressBarDialogBox.setVisibility(View.GONE);
                                            }
                                        });
                                        return;
                                    }
                                    alertChangeNameDialogBox.dismiss();
                                    Snackbar.make(findViewById(android.R.id.content),"You are not Admin of group",Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
            }
        });
    }
    private void updateNameOfGroupIfChanged(final String GroupName){
        if(!GroupInfo.get("GroupName").equals(getIntent().getStringExtra("GroupName"))){
            CurrentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document=task.getResult();
                        if(document.exists()){
                            List<Map<String,Object>> group=(List<Map<String,Object>>)document.get("Groups");
                            for(int i=0;i<group.size();i++){
                                if(group.get(i).get("Id").equals(GroupDocRef.getId())){
                                    group.get(i).put("GroupName",GroupName);
                                    group.get(i).put("status","group name changed.");
                                    CurrentUserRef.update("Groups",group).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Snackbar.make(findViewById(android.R.id.content),"Group Name updated.",Snackbar.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(VotingGroupActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    return;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

}
