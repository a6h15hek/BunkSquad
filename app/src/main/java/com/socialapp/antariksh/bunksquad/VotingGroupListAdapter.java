package com.socialapp.antariksh.bunksquad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VotingGroupListAdapter extends RecyclerView.Adapter<VotingGroupListAdapter.ListViewHolder> {
    private Context context;
    private List<Object> groups;
    public VotingGroupListAdapter(Context ct,List<Object> groups){
        this.context=ct;
        this.groups= groups;
       // List<Map<String, Object>> groups = (List<Map<String, Object>>) UserInfo.get("Groups");
        //((TextView)view.findViewById(R.id.testText)).setText((CharSequence) groups.get(0).get("GroupName"));
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.voting_group_row_recycler_view,parent,false);
        return new VotingGroupListAdapter.ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        final Map<String, Object> groupInfo=(Map<String,Object>) groups.get(position);
        holder.groupName.setText((String) groupInfo.get("GroupName"));
        holder.GroupAvatar.setImageDrawable(TextDrawableForImageView.builder()
                .buildRound(String.valueOf(((String) groupInfo.get("GroupName")).toUpperCase().charAt(0)),
                        Color.parseColor("#8d4de9"),Color.BLACK));
        holder.groupStatus.setText((String) groupInfo.get("status"));
        holder.GroupRowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VotingGroupActivity.class);
                intent.putExtra("groupId", String.valueOf(groupInfo.get("Id")));
                intent.putExtra("GroupName", String.valueOf(groupInfo.get("GroupName")));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView groupName,groupStatus;
        ImageView GroupAvatar;
        LinearLayout GroupRowLayout;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            GroupRowLayout=itemView.findViewById(R.id.groupRowLayout);
            groupName=itemView.findViewById(R.id.groupName);
            groupStatus=itemView.findViewById(R.id.GroupStatus);
            GroupAvatar=itemView.findViewById(R.id.GroupAvatar);
        }
    }
}
