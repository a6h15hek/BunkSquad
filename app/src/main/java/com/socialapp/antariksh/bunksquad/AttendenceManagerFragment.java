package com.socialapp.antariksh.bunksquad;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ca.antonious.materialdaypicker.MaterialDayPicker;


import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;



import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AttendenceManagerFragment extends Fragment {
    private static final int NOTIFICATION_REQUEST_CODE = 1998;
    private View view;
    private final String ATTENDANCE_MANAGER_DATA_FILE="attendancemanager.json";
    private View dialogBoxAddTaskFromView;
    private File AttendanceManagerDataFileAddress;
    private org.json.simple.JSONArray attendanceManagerArray =null;
    private RecyclerView recyclerView;
    SharedPreferences sharedPref;
    SwitchMaterial dailyMarkingSwitch;
    private ProgressBar HeadingLoadingBar;
    private AttendanceManagerTaskListAdapter attendanceManagerTaskListAdapter;
    public AttendenceManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_attendence_manager, container, false);
        recyclerView=(RecyclerView)view.findViewById(R.id.attendanceMangerTaskList);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        HeadingLoadingBar=view.findViewById(R.id.headerLoadingProgressBar);
        HeadingLoadingBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                HeadingLoadingBar.setVisibility(View.INVISIBLE);
            }
        },1000);

        sharedPref = getActivity().getSharedPreferences("dailyRemainderFile",getActivity().MODE_PRIVATE);
        loadAttendanceMangerDataFile();
        setAddTaskLayoutListeners();
        updateTaskList();
        setWeekDaySelector();
        setTimeSelector();
        createNotificationChannel();

        //daily remainder switch
        dailyMarkingSwitch=view.findViewById(R.id.dailyMarkingRemainder);
        final LinearLayout TimeSelectorLayout=view.findViewById(R.id.timeSelectorLayout);
        final LinearLayout DaySelectorLayout=view.findViewById(R.id.weekSelectorLayout);
        if(sharedPref.getBoolean("dailyMarkingReminderSwitch", Boolean.parseBoolean(getResources().getString(R.string.dailyMarkingReminder)))){
            TimeSelectorLayout.setVisibility(View.VISIBLE);
            DaySelectorLayout.setVisibility(View.VISIBLE);
            dailyMarkingSwitch.setChecked(true);
        }else{
            TimeSelectorLayout.setVisibility(View.GONE);
            DaySelectorLayout.setVisibility(View.GONE);
            dailyMarkingSwitch.setChecked(false);
        }
        dailyMarkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if(isChecked){
                    editor.putBoolean("dailyMarkingReminderSwitch",true);
                    TimeSelectorLayout.setVisibility(View.VISIBLE);
                    DaySelectorLayout.setVisibility(View.VISIBLE);
                    cancelDailyMarkingRemainder();
                    setDailyMarkingRemainder();
                }else{
                    TimeSelectorLayout.setVisibility(View.GONE);
                    DaySelectorLayout.setVisibility(View.GONE);
                    editor.putBoolean("dailyMarkingReminderSwitch",false);
                    cancelDailyMarkingRemainder();
                }
                editor.commit();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setDailyMarkingRemainder(){
        Log.i("abhishek1","set Daily remainder");
        int hour = sharedPref.getInt("savedHour", Integer.parseInt(getResources().getString(R.string.hourOfDay)));
        int min = sharedPref.getInt("savedMinute", Integer.parseInt(getResources().getString(R.string.minute)));
        Calendar calNow=Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();
        calSet.set(Calendar.HOUR_OF_DAY, hour);
        calSet.set(Calendar.MINUTE, min);
        calSet.set(Calendar.SECOND, 0);
        if (calSet.compareTo(calNow) <= 0) {
            //Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        }
        Intent intent = new Intent(getActivity(), NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),NOTIFICATION_REQUEST_CODE,intent,0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        //alarmManager.set(AlarmManager.RTC_WAKEUP,calSet.getTimeInMillis(),pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calSet.getTimeInMillis(),86400000,pendingIntent);
    }
    public void cancelDailyMarkingRemainder(){
        Log.i("abhishek1","cancel daily remainder");
        Intent intent = new Intent(getActivity(), NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),NOTIFICATION_REQUEST_CODE,intent,0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BunkSquad Attendance Manager";
            String description = "Daily Remainder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("attendanceManager198", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void setTimeSelector() {
        final TextView timeView = view.findViewById(R.id.timeSelector);
        timeView.setText(getConvert24to12hour(sharedPref.getInt("savedHour", Integer.parseInt(getResources().getString(R.string.hourOfDay))),
                sharedPref.getInt("savedMinute", Integer.parseInt(getResources().getString(R.string.minute)))));
        timeView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeView.setText(getConvert24to12hour(selectedHour,selectedMinute));
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("savedHour",selectedHour);
                        editor.putInt("savedMinute",selectedMinute);
                        editor.commit();
                        Snackbar.make(view,"new time set successfully",Snackbar.LENGTH_SHORT).show();
                        cancelDailyMarkingRemainder();
                        if(dailyMarkingSwitch.isChecked()){
                            setDailyMarkingRemainder();
                        }
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.show();
            }
        });
    }
    private String getConvert24to12hour(int hour,int minute){
        int newHour = hour;
        int newMinutes = minute;
        String status;
        if (newHour > 12) {
            newHour -= 12;
            status = "PM";
        } else if (newHour == 0) {
            newHour += 12;
            status = "AM";
        } else if (newHour == 12){
            status = "PM";
        }else{
            status = "AM";
        }
        String min = "";
        if (newMinutes < 10)
            min = "0" + newMinutes ;
        else
            min = String.valueOf(newMinutes);
        return String.valueOf(newHour)+" : "+min+" "+status;
    }
    private void setWeekDaySelector(){
        MaterialDayPicker materialDayPicker=view.findViewById(R.id.day_picker);
        List<MaterialDayPicker.Weekday> listOfSelectedDay = new ArrayList<MaterialDayPicker.Weekday>();
        if(!sharedPref.getBoolean("MONDAY", Boolean.parseBoolean(getResources().getString(R.string.MONDAY)))){
            listOfSelectedDay.add(MaterialDayPicker.Weekday.MONDAY);
        }
        if(!sharedPref.getBoolean("TUESDAY", Boolean.parseBoolean(getResources().getString(R.string.TUESDAY)))){
            listOfSelectedDay.add(MaterialDayPicker.Weekday.TUESDAY);
        }
        if(!sharedPref.getBoolean("WEDNESDAY", Boolean.parseBoolean(getResources().getString(R.string.WEDNESDAY)))) {
            listOfSelectedDay.add(MaterialDayPicker.Weekday.WEDNESDAY);
        }
        if(!sharedPref.getBoolean("THURSDAY", Boolean.parseBoolean(getResources().getString(R.string.THURSDAY)))){
            listOfSelectedDay.add(MaterialDayPicker.Weekday.THURSDAY);
        }
        if(!sharedPref.getBoolean("FRIDAY", Boolean.parseBoolean(getResources().getString(R.string.FRIDAY)))){
            listOfSelectedDay.add(MaterialDayPicker.Weekday.FRIDAY);
        }
        if(!sharedPref.getBoolean("SATURDAY", Boolean.parseBoolean(getResources().getString(R.string.SATURDAY)))){
            listOfSelectedDay.add(MaterialDayPicker.Weekday.SATURDAY);
        }
        if(!sharedPref.getBoolean("SUNDAY", Boolean.parseBoolean(getResources().getString(R.string.SUNDAY)))){
            listOfSelectedDay.add(MaterialDayPicker.Weekday.SUNDAY);
        }
        materialDayPicker.setSelectedDays(listOfSelectedDay);
        materialDayPicker.setDayPressedListener(new MaterialDayPicker.DayPressedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDayPressed(MaterialDayPicker.Weekday weekday, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(String.valueOf(weekday),!b);
                editor.commit();
                if(!b){
                    Snackbar.make(view,"Remainder added for "+String.valueOf(weekday),Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(view,"Remainder Removed for "+String.valueOf(weekday),Snackbar.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void updateTaskList() {
        attendanceManagerTaskListAdapter=
                new AttendanceManagerTaskListAdapter(getActivity(),attendanceManagerArray);
        recyclerView.setAdapter(attendanceManagerTaskListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setAddTaskLayoutListeners() {
        ((ExtendedFloatingActionButton)view.findViewById(R.id.add_task_subject)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                ViewGroup viewGroup = view.findViewById(android.R.id.content);
                dialogBoxAddTaskFromView = LayoutInflater.from(v.getContext()).inflate(R.layout.attendance_manager_add_new_subject, viewGroup, false);
                builder.setView(dialogBoxAddTaskFromView);
                final AlertDialog alertAddFormDialogBox = builder.create();
                alertAddFormDialogBox.show();

                ((Button)dialogBoxAddTaskFromView.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertAddFormDialogBox.dismiss();
                    }
                });
                ((Slider)dialogBoxAddTaskFromView.findViewById(R.id.to_achieve_attendance_slider)).addOnChangeListener(new Slider.OnChangeListener() {
                    @Override
                    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                        ((TextView)dialogBoxAddTaskFromView.findViewById(R.id.slider_percentage_attendance)).setText((int)value+"%");
                    }
                });
                ((Button)dialogBoxAddTaskFromView.findViewById(R.id.add_task_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int noClassAttendedInt;
                        int noClassTotalInt;
                        String subjectName=((TextInputEditText)dialogBoxAddTaskFromView.findViewById(R.id.subject_name_inputField)).getText().toString();
                        String noClassAttended=((TextInputEditText)dialogBoxAddTaskFromView.findViewById(R.id.class_attended_inputField)).getText().toString();
                        String noClassTotal=((TextInputEditText)dialogBoxAddTaskFromView.findViewById(R.id.class_total_inputField)).getText().toString();

                        TextInputLayout subjectNameLayout=(TextInputLayout)dialogBoxAddTaskFromView.findViewById(R.id.subject_name_inputField_layout);
                        TextInputLayout noClassAttendedLayout=(TextInputLayout)dialogBoxAddTaskFromView.findViewById(R.id.class_attended_inputField_layout);
                        TextInputLayout noClassTotalLayout=(TextInputLayout)dialogBoxAddTaskFromView.findViewById(R.id.class_total_inputField_layout);
                        if(subjectName.isEmpty()){
                            //subjectNameLayout.setHelperText("Empty input");
                            subjectNameLayout.setError("Empty input !");
                            noClassAttendedLayout.setErrorEnabled(false);
                            noClassTotalLayout.setErrorEnabled(false);
                        }else if(noClassAttended.isEmpty()){
                            subjectNameLayout.setErrorEnabled(false);
                            noClassAttendedLayout.setError("Empty input !");
                            noClassTotalLayout.setErrorEnabled(false);
                        }else if(noClassTotal.isEmpty()){
                            subjectNameLayout.setErrorEnabled(false);
                            noClassAttendedLayout.setErrorEnabled(false);
                            noClassTotalLayout.setError("Empty input !");
                        }else{
                            subjectNameLayout.setErrorEnabled(false);
                            noClassAttendedLayout.setErrorEnabled(false);
                            noClassTotalLayout.setErrorEnabled(false);
                            try{
                                noClassAttendedInt=Integer.parseInt(noClassAttended);
                            }catch (NumberFormatException e){
                                subjectNameLayout.setErrorEnabled(false);
                                noClassAttendedLayout.setError("Invalid Input");
                                noClassTotalLayout.setErrorEnabled(false);
                                return;
                            }
                            try{
                                noClassTotalInt=Integer.parseInt(noClassTotal);
                            }catch (NumberFormatException e){
                                subjectNameLayout.setErrorEnabled(false);
                                noClassAttendedLayout.setErrorEnabled(false);
                                noClassTotalLayout.setError("Invalid Input");
                                return;
                            }
                            if(noClassAttendedInt<0){
                                subjectNameLayout.setErrorEnabled(false);
                                noClassAttendedLayout.setError("Invalid Input");
                                noClassTotalLayout.setErrorEnabled(false);
                                return;
                            }
                            if(noClassAttendedInt>noClassTotalInt||noClassTotalInt<=0){
                                subjectNameLayout.setErrorEnabled(false);
                                noClassAttendedLayout.setErrorEnabled(false);
                                noClassTotalLayout.setError("Invalid Input");
                                return;
                            }
                            //all condition passes
                            addTaskToFile(subjectName,noClassAttendedInt,noClassTotalInt);
                            alertAddFormDialogBox.dismiss();
                            Snackbar.make(view,"Task Added Successfully.",Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private void loadAttendanceMangerDataFile(){
        AttendanceManagerDataFileAddress = new File(getContext().getFilesDir(),ATTENDANCE_MANAGER_DATA_FILE);
        if(!AttendanceManagerDataFileAddress.exists()){
            try(FileWriter fileWrite = new FileWriter(AttendanceManagerDataFileAddress)){
                fileWrite.write("[]");
                fileWrite.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        JSONParser jsonParser=new JSONParser();
        try(FileReader fileReader = new FileReader(AttendanceManagerDataFileAddress)){
            Object obj=jsonParser.parse(fileReader);
            attendanceManagerArray= (org.json.simple.JSONArray) obj;
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException | ParseException e){
            e.printStackTrace();
        }
    }
    private void updateJsonFile(String dataLine){
        try(FileWriter fileWriter=new FileWriter(AttendanceManagerDataFileAddress)){
            fileWriter.write(dataLine);
            fileWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void addTaskToFile(String subjectName, int noClassAttendedInt, int noClassTotalInt) {
        org.json.simple.JSONObject jsonObject =new org.json.simple.JSONObject();
        jsonObject.put("TaskName",subjectName);
        jsonObject.put("ClassAttended",Long.valueOf(noClassAttendedInt));
        jsonObject.put("classTotal",Long.valueOf(noClassTotalInt));
        jsonObject.put("toAchieve",Long.valueOf((int)((Slider)dialogBoxAddTaskFromView.findViewById(R.id.to_achieve_attendance_slider)).getValue()));
        attendanceManagerArray.add(0,jsonObject);
        attendanceManagerTaskListAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(0);
        updateJsonFile(attendanceManagerArray.toJSONString());
    }
}
