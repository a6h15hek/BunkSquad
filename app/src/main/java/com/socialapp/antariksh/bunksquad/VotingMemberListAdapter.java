package com.socialapp.antariksh.bunksquad;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class VotingMemberListAdapter extends RecyclerView.Adapter<VotingMemberListAdapter.ListViewHolder> {
    private DocumentReference GroupDocRef;
    private Context context;
    private List<Map<String ,Object>>  memberArrayMap;
    private Map<String ,Object> currentUserInfo;
    FirebaseStorage storage;
    VotingMemberListAdapter(Context context, List<Map<String ,Object>> memberArrayMap,Map<String ,Object> currentUserInfo,String groupId){
        this.context=context;
        this.memberArrayMap=memberArrayMap;
        this.currentUserInfo=currentUserInfo;
        FirebaseFirestore fireStoreDB;
        fireStoreDB= FirebaseFirestore.getInstance();
        GroupDocRef=fireStoreDB.collection("BunkSquadGroups").document(groupId);
    }
    @NonNull
    @Override
    public VotingMemberListAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        storage=FirebaseStorage.getInstance();
        View view=inflater.inflate(R.layout.voting_member_list_row,parent,false);
        return new VotingMemberListAdapter.ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VotingMemberListAdapter.ListViewHolder holder, final int position) {
        final Map<String, Object> memberInfo=memberArrayMap.get(position);
        if(currentUserInfo.get("Id").equals(memberInfo.get("Id"))){
            holder.memberName.setText((String) memberInfo.get("name")+" (You)");
        }else{
            holder.memberName.setText((String) memberInfo.get("name"));
        }

        if(memberInfo.get("role").equals("admin")){
            holder.role.setText("admin");
        }else {
            holder.role.setVisibility(View.GONE);
            holder.role.setText("");
        }
        //setuping option menu
        if(currentUserInfo.get("role").equals("admin")&&(!currentUserInfo.get("Id").equals(memberInfo.get("Id")))){
            holder.memberOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context,holder.memberOptionMenu);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.member_list_row_options);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.changeRole:
                                    DialogInterface.OnClickListener ChangeRoleDialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    if(memberArrayMap.get(position).get("role").equals("admin")){
                                                        memberArrayMap.get(position).put("role","member");
                                                        GroupDocRef.update("Member",memberArrayMap,"adminId", FieldValue.arrayRemove(memberInfo.get("Id"))).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(context,"Role Changed",Toast.LENGTH_SHORT).show();
                                                                    notifyDataSetChanged();
                                                                }else{
                                                                    Toast.makeText(context,"Unable to change role. "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        memberArrayMap.get(position).put("role","admin");
                                                        GroupDocRef.update("Member",memberArrayMap,"adminId", FieldValue.arrayUnion(memberInfo.get("Id"))).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(context,"Role Changed",Toast.LENGTH_SHORT).show();
                                                                    notifyDataSetChanged();
                                                                }else{
                                                                    Toast.makeText(context,"Unable to change role. "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
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
                                    androidx.appcompat.app.AlertDialog.Builder ChangeRolebuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
                                    ChangeRolebuilder.setTitle("Member role").setMessage("Are you sure? You want to change member role \n* admin -> member\n* member -> admin ").setPositiveButton("Yes", ChangeRoleDialogClickListener)
                                            .setNegativeButton("No", ChangeRoleDialogClickListener).show();
                                    break;
                                case R.id.removeMember:
                                    DialogInterface.OnClickListener RemoveMemberDialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    memberArrayMap.remove(position);
                                                    Map<String,Object> groupUpdate = new HashMap<String, Object>();
                                                    groupUpdate.put("Member",memberArrayMap);
                                                    groupUpdate.put("adminId", FieldValue.arrayRemove(memberInfo.get("Id")));
                                                    groupUpdate.put("memberId",FieldValue.arrayRemove(memberInfo.get("Id")));

                                                    GroupDocRef.update(groupUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(context,"Member Removed",Toast.LENGTH_SHORT).show();
                                                                notifyItemRemoved(position);
                                                            }else{
                                                                Toast.makeText(context,"Unable to remove the member"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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
                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                                    builder.setTitle("Remove Member").setMessage("Are you sure? You want to change remove member. ").setPositiveButton("Yes", RemoveMemberDialogClickListener)
                                            .setNegativeButton("No", RemoveMemberDialogClickListener).show();
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }else{
            holder.memberOptionMenu.setVisibility(View.GONE);
        }
        StorageReference imageRef=storage.getReferenceFromUrl("gs://bunksquad-893fc.appspot.com/userAvatar/").child(memberInfo.get("Id").toString()+".jpeg");
        try {
            final File file=File.createTempFile("image","jpeg");
            imageRef.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        try {
                            Glide.with(context)
                                    .load(file.getAbsolutePath())
                                    .into(holder.avatar);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return memberArrayMap.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView memberName,role,memberOptionMenu;
        ImageView avatar;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName=itemView.findViewById(R.id.memberName);
            role=itemView.findViewById(R.id.memberRole);
            avatar=itemView.findViewById(R.id.memberAvatar);
            memberOptionMenu=itemView.findViewById(R.id.memberOptionMenu);
        }
    }
}
