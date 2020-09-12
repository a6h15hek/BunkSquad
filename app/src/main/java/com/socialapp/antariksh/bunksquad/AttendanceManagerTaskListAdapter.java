package com.socialapp.antariksh.bunksquad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import org.json.simple.JSONArray;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceManagerTaskListAdapter extends RecyclerView.Adapter<AttendanceManagerTaskListAdapter.ListViewHolder> {
    private Context context;
    private JSONArray taskListArray=null;
    private BunkSquad bunkSquad;
    public AttendanceManagerTaskListAdapter(Context ct,JSONArray taskListArray){
        this.context=ct;
        this.taskListArray=taskListArray;
        bunkSquad=new BunkSquad();
    }
    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.task_attendance_list_row,parent,false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListViewHolder holder, final int position) {
        if(position==taskListArray.size()){
            holder.title.setText("");
            holder.subHeading.setText("No More Task");
            holder.subHeadingValue.setText("");
            holder.subHeadingGoal.setText("");
            holder.listStatus.setText("");
            holder.percentAttendance.setText("");
            holder.sideLinearLayoutColor.setBackgroundColor(Color.parseColor("#ffffff"));
            holder.attendance.setVisibility(View.GONE);
            holder.goal.setVisibility(View.GONE);
            holder.fullProgressBar.setVisibility(View.GONE);
            holder.classAttendedButton.setVisibility(View.GONE);
            holder.classLeavedButton.setVisibility(View.GONE);
            holder.optionMenu.setVisibility(View.GONE);
            holder.ContentMainListLayout.setClickable(false);
            return;
        }
        holder.attendance.setVisibility(View.VISIBLE);
        holder.goal.setVisibility(View.VISIBLE);
        holder.fullProgressBar.setVisibility(View.VISIBLE);
        holder.classAttendedButton.setVisibility(View.VISIBLE);
        holder.classLeavedButton.setVisibility(View.VISIBLE);
        holder.optionMenu.setVisibility(View.VISIBLE);
        //addTask error
        final org.json.simple.JSONObject taskData = (org.json.simple.JSONObject) taskListArray.get(position);
        holder.title.setText(String.valueOf(taskData.get("TaskName")));
        holder.subHeading.setText("Attendance: ");
        final int ClassAttended= ((Long)taskData.get("ClassAttended")).intValue();
        final int ClassTotal= ((Long)taskData.get("classTotal")).intValue();
        final int toAchieve=((Long)taskData.get("toAchieve")).intValue();
        int toAttendClasses=bunkSquad.getToAttendClasses(toAchieve,ClassAttended,ClassTotal);
        int toBunkClasses=bunkSquad.getToBunkClasses(toAchieve,ClassAttended,ClassTotal);
        holder.subHeadingValue.setText(String.valueOf(ClassAttended+"/"+ClassTotal));
        holder.subHeadingGoal.setText(String.valueOf(taskData.get("toAchieve"))+"%");
        holder.listStatus.setText("Status: "+String.valueOf(bunkSquad.getStatus(toAttendClasses
                ,toBunkClasses,toAchieve)));
        if(toAttendClasses<=toBunkClasses){
            holder.sideLinearLayoutColor.setBackgroundColor(Color.parseColor("#5383d3"));
            holder.subHeadingValue.setTextColor(Color.parseColor("#5383d3"));
        }else{
            holder.sideLinearLayoutColor.setBackgroundColor(Color.parseColor("#e53935"));
            holder.subHeadingValue.setTextColor(Color.parseColor("#e53935"));
        }
        float PercentageAttendance = (float) (((float)ClassAttended/(float)ClassTotal)*100);
        holder.attendance.setProgress((int) PercentageAttendance);
        holder.percentAttendance.setText(String.valueOf((new DecimalFormat("0.0")).format(PercentageAttendance))+"%");
        holder.goal.setProgress(toAchieve);
        holder.ContentMainListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AttendanceReportActivity.class);
                intent.putExtra("classAttended",String.valueOf(ClassAttended));
                intent.putExtra("classTotal",String.valueOf(ClassTotal));
                intent.putExtra("toAchieve",String.valueOf(toAchieve));
                intent.putExtra("headerMsg",taskData.get("TaskName")+" Report");
                context.startActivity(intent);
            }
        });
        holder.classAttendedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClassAttended < 999){
                    taskData.put("ClassAttended",Long.valueOf(ClassAttended+1));
                    taskData.put("classTotal",Long.valueOf(ClassTotal+1));
                    notifyItemChanged(position);
                    updateJsonFile(taskListArray.toJSONString(),context);
                }
            }
        });
        holder.classLeavedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClassTotal < 999){
                    taskData.put("ClassAttended", Long.valueOf(ClassAttended));
                    taskData.put("classTotal", Long.valueOf(ClassTotal + 1));
                    notifyItemChanged(position);
                    updateJsonFile(taskListArray.toJSONString(),context);
                }
            }
        });
        holder.optionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context,holder.optionMenu);
                //inflating menu from xml resource
                popup.inflate(R.menu.am_task_list_option_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.editOption:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                ViewGroup viewGroup = holder.Content;
                                final View dialogBoxEditTaskFromView = LayoutInflater.from(context).inflate(R.layout.attendance_manager_edit_subject, viewGroup, false);
                                builder.setView(dialogBoxEditTaskFromView);
                                final AlertDialog alertAddFormDialogBox = builder.create();
                                alertAddFormDialogBox.show();
                                final TextInputEditText subjectName=(TextInputEditText)dialogBoxEditTaskFromView.findViewById(R.id.subject_name_inputField);
                                final TextInputEditText noClassAttended=(TextInputEditText)dialogBoxEditTaskFromView.findViewById(R.id.class_attended_inputField);
                                final TextInputEditText noClassTotal=(TextInputEditText)dialogBoxEditTaskFromView.findViewById(R.id.class_total_inputField);
                                subjectName.setText(String.valueOf(taskData.get("TaskName")));
                                noClassAttended.setText(String.valueOf(ClassAttended));
                                noClassTotal.setText(String.valueOf(ClassTotal));
                                final TextInputLayout subjectNameLayout=(TextInputLayout)dialogBoxEditTaskFromView.findViewById(R.id.subject_name_inputField_layout);
                                final TextInputLayout noClassAttendedLayout=(TextInputLayout)dialogBoxEditTaskFromView.findViewById(R.id.class_attended_inputField_layout);
                                final TextInputLayout noClassTotalLayout=(TextInputLayout)dialogBoxEditTaskFromView.findViewById(R.id.class_total_inputField_layout);

                                ((Button)dialogBoxEditTaskFromView.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertAddFormDialogBox.dismiss();
                                    }
                                });
                                ((Button)dialogBoxEditTaskFromView.findViewById(R.id.add_task_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(subjectName.getText().toString().isEmpty()){
                                            subjectNameLayout.setError("Empty input !");
                                            noClassAttendedLayout.setErrorEnabled(false);
                                            noClassTotalLayout.setErrorEnabled(false);
                                            return;
                                        }
                                        String tempClassAttended=String.valueOf(noClassAttended.getText());
                                        String tempClassTotal=String.valueOf(noClassTotal.getText());
                                        if(tempClassAttended.isEmpty()){
                                            subjectNameLayout.setErrorEnabled(false);
                                            noClassAttendedLayout.setError("Empty Input !");
                                            noClassTotalLayout.setErrorEnabled(false);
                                            return;
                                        }
                                        if(tempClassTotal.isEmpty()){
                                            subjectNameLayout.setErrorEnabled(false);
                                            noClassAttendedLayout.setErrorEnabled(false);
                                            noClassTotalLayout.setError("Empty Input !");
                                            return;
                                        }
                                        int noClassAttendedInt=Integer.parseInt(tempClassAttended);
                                        int noClassTotalInt=Integer.parseInt(tempClassTotal);
                                        if(noClassAttendedInt<0){
                                            subjectNameLayout.setErrorEnabled(false);
                                            noClassAttendedLayout.setError("Invalid Input !");
                                            noClassTotalLayout.setErrorEnabled(false);
                                            return;
                                        }
                                        if(noClassTotalInt<0||noClassTotalInt<noClassAttendedInt){
                                            subjectNameLayout.setErrorEnabled(false);
                                            noClassAttendedLayout.setErrorEnabled(false);
                                            noClassTotalLayout.setError("Invalid Input !");
                                            return;
                                        }
                                        taskData.put("TaskName",subjectName.getText().toString());
                                        taskData.put("ClassAttended",Long.valueOf(noClassAttendedInt));
                                        taskData.put("classTotal",Long.valueOf(noClassTotalInt));
                                        taskData.put("toAchieve",Long.valueOf((int)((Slider)dialogBoxEditTaskFromView.findViewById(R.id.to_achieve_attendance_slider)).getValue()));
                                        alertAddFormDialogBox.dismiss();
                                        notifyItemChanged(position);
                                        updateJsonFile(taskListArray.toJSONString(),context);
                                        Toast.makeText(context,"Task updated successfully",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case R.id.deleteOption:
                                //handle menu2 click
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked
                                                taskListArray.remove(position);
                                                notifyDataSetChanged();
                                                Toast.makeText(context,"Task Deleted successfully",Toast.LENGTH_SHORT).show();
                                                updateJsonFile(taskListArray.toJSONString(),context);
                                                dialog.dismiss();
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                dialog.dismiss();
                                                break;
                                        }
                                    }
                                };
                                androidx.appcompat.app.AlertDialog.Builder confirmDeleteBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
                                confirmDeleteBuilder.setMessage("Are you sure? you want to delete task.").setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener).show();
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });
    }

    public void updateJsonFile(String dataLine,Context ct){
        File AttendanceManagerDataFileAddress = new File(ct.getFilesDir(),"attendancemanager.json");
        try(FileWriter fileWriter=new FileWriter(AttendanceManagerDataFileAddress)){
            fileWriter.write(dataLine);
            fileWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return taskListArray.size()+1;
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        TextView title,subHeading,subHeadingValue,subHeadingGoal,listStatus,percentAttendance,optionMenu;
        LinearLayout sideLinearLayoutColor,ContentMainListLayout;
        ProgressBar attendance,goal,fullProgressBar;
        ImageView classAttendedButton,classLeavedButton;
        ViewGroup Content;
        Context context;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.Task_list_title);
            subHeading=itemView.findViewById(R.id.Task_list_subhead);
            subHeadingValue=itemView.findViewById(R.id.Task_list_subhead_value);
            subHeadingGoal=itemView.findViewById(R.id.Task_list_goal_value);
            listStatus=itemView.findViewById(R.id.Task_list_status);
            percentAttendance=itemView.findViewById(R.id.Task_list_attendance_no);
            sideLinearLayoutColor=itemView.findViewById(R.id.colorOfListRow);
            attendance = itemView.findViewById(R.id.Task_list_present_attendance);
            goal = itemView.findViewById(R.id.Task_list_present_Goal);
            fullProgressBar = itemView.findViewById(R.id.Task_list_progress_full);
            classAttendedButton = itemView.findViewById(R.id.class_attended_button);
            classLeavedButton = itemView.findViewById(R.id.class_leaved_button);
            context = itemView.getContext();
            ContentMainListLayout=itemView.findViewById(R.id.mainListLayoutContent);
            optionMenu = itemView.findViewById(R.id.OptionMenu);
            Content=itemView.findViewById(android.R.id.content);
        }
    }
}
