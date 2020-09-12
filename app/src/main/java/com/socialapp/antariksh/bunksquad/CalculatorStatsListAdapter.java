package com.socialapp.antariksh.bunksquad;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalculatorStatsListAdapter extends RecyclerView.Adapter<CalculatorStatsListAdapter.ListViewHolder> {
    //Constructor to take input from main class
    private int classAttended,classTotal,listSize;
    private char stats;
    private float currentPercentAttendance=0;
    private BunkSquad bunkSquad;
    Context context;
    public CalculatorStatsListAdapter(Context ct, int classAttended, int classTotal,int listSize,char stats){
        bunkSquad=new BunkSquad();
        context=ct;
        this.classAttended=classAttended;
        this.classTotal=classTotal;
        this.listSize=listSize;
        this.stats=stats;
        if(stats=='a'){
            this.currentPercentAttendance=bunkSquad.getAttendanceGrowth(classAttended,classTotal,0);
        }else if(stats=='b'){
            this.currentPercentAttendance=bunkSquad.getAttendanceFall(classAttended, classTotal, 0);
        }else{
            this.currentPercentAttendance=0;
        }
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.calculator_stats_row,parent,false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        if(position==listSize){
            holder.colorLayout.setBackgroundColor(Color.parseColor("#4961e1"));
            holder.title.setText("BunkSquad");
            holder.noClass.setText("");
            holder.subtitle.setText("");
            holder.rateMsg.setText("");
            holder.percentRateMsg.setText("You will be on track.");
            holder.attendancePercentage.setText("");
            holder.arrowToPercentage.setVisibility(View.INVISIBLE);
            return;
        }
        if(stats=='a'){
            float newAttendanceGrowth=bunkSquad.getAttendanceGrowth(classAttended,classTotal,position+1);
            holder.colorLayout.setBackgroundColor(Color.parseColor("#ffb200"));
            holder.title.setText("Attend  ");
            holder.noClass.setText(String.valueOf(position+1));
            holder.subtitle.setText("  lectures");
            holder.rateMsg.setText("Growth Rate : ");
            holder.percentRateMsg.setText(String.valueOf("+"+(new DecimalFormat("0.00")).format(newAttendanceGrowth-currentPercentAttendance))+"%");
            holder.attendancePercentage.setText(String.valueOf((new DecimalFormat("0.00")).format(newAttendanceGrowth))+"%");
        } else if (stats == 'b') {
            float newAttendanceFall=bunkSquad.getAttendanceFall(classAttended, classTotal, position + 1);
            holder.colorLayout.setBackgroundColor(Color.parseColor("#e53935"));
            holder.title.setText("Leave  ");
            holder.noClass.setText(String.valueOf(position + 1));
            holder.subtitle.setText("  lectures");
            holder.rateMsg.setText("Fall Rate : ");
            holder.percentRateMsg.setText("-"+String.valueOf((new DecimalFormat("0.00")).format(currentPercentAttendance-newAttendanceFall)+"%"));
            holder.attendancePercentage.setText(String.valueOf((new DecimalFormat("0.00")).format(newAttendanceFall))+"%");
        }
    }

    @Override
    public int getItemCount() {
        return listSize+1;
    }


    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView title,noClass,subtitle,rateMsg,percentRateMsg,attendancePercentage;
        ImageView arrowToPercentage;
        LinearLayout colorLayout;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.stat_msg_title);
            noClass=itemView.findViewById(R.id.stat_msg_noclass);
            subtitle=itemView.findViewById(R.id.stat_msg_submsg);
            rateMsg=itemView.findViewById(R.id.stat_msg_rate);
            percentRateMsg=itemView.findViewById(R.id.stat_msg_rate_percentage);
            attendancePercentage=itemView.findViewById(R.id.stat_msg_percentage);
            arrowToPercentage=itemView.findViewById(R.id.arrow_point_to_percentage);
            colorLayout=itemView.findViewById(R.id.colorOfListRow);
        }
    }
}
