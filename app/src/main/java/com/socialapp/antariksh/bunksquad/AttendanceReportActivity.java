package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import java.text.DecimalFormat;

public class AttendanceReportActivity extends AppCompatActivity {
    private int classAttended;
    private int classTotal;
    private int toAchieve;
    private int toAttendClass;
    private int toBunkClass;
    private String headerMsg;
    private char stats;
    private BunkSquad bunkSquad;
    //Setting up Recycler View
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);
        recyclerView= (RecyclerView)findViewById(R.id.result_stats_List_recycler_view);
        this.classAttended = Integer.valueOf(getIntent().getStringExtra("classAttended"));
        this.classTotal = Integer.valueOf(getIntent().getStringExtra("classTotal"));
        this.toAchieve = Integer.valueOf(getIntent().getStringExtra("toAchieve"));
        this.headerMsg = getIntent().getStringExtra("headerMsg");
        bunkSquad=new BunkSquad();
        //setting up the initial content
        setInitialValues(classAttended,classTotal);
        //set all Listener
        setLayoutListener();
    }

    private void setInitialValues(int classAttended, int classTotal) {
        //Initially the value of Attended and Total
        ((TextView)findViewById(R.id.headerMsg)).setText(headerMsg);
        ((TextView)findViewById(R.id.classAttended)).setText(String.valueOf(classAttended));
        ((TextView)findViewById(R.id.classTotal)).setText(String.valueOf(classTotal));
        ((TextView)findViewById(R.id.SliderValueView)).setText(String.valueOf(toAchieve)+"%");
        ((Slider)findViewById(R.id.ChangeSliderId)).setValue(toAchieve);

        //Calculate percentage attendance in Progress bar and ProgressBar TextView
        float PercentageAttendance = (((float)classAttended/(float)classTotal)*100);
        ((TextView)findViewById(R.id.AttendedPercentage)).setText(String.valueOf((new DecimalFormat("0.00")).format(PercentageAttendance))+"%");
        ((ProgressBar)findViewById(R.id.AttendedProgressBar)).setProgress((int) PercentageAttendance);

        //Initially set the value of ToAchieve Progress Bar
        ((ProgressBar)findViewById(R.id.ChangeSliderProgressBar)).setProgress(toAchieve);


        //Initially set the value of Attended + bunk + messages
        toAttendClass=bunkSquad.getToAttendClasses(toAchieve,classAttended,classTotal);
        toBunkClass=bunkSquad.getToBunkClasses(toAchieve,classAttended,classTotal);
        setAttendedBunkClassesView(toAttendClass,
                toBunkClass,toAchieve);

    }
    private void setLayoutListener(){
        //back Button click listener
        findViewById(R.id.back_btn_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //set the seekBar slider change Listener
        ((Slider)findViewById(R.id.ChangeSliderId)).addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                ((ProgressBar)findViewById(R.id.ChangeSliderProgressBar)).setProgress((int) value);
                ((TextView)findViewById(R.id.SliderValueView)).setText(String.valueOf((int) value)+"%");
                toAttendClass=bunkSquad.getToAttendClasses((int) value,classAttended,classTotal);
                toBunkClass=bunkSquad.getToBunkClasses((int) value,classAttended,classTotal);
                setAttendedBunkClassesView(toAttendClass,
                        toBunkClass,(int) value);
                //set the stats List
                if(stats=='a'){
                    changeStatsList(toAttendClass,'a');
                }else if(stats=='b'){
                    changeStatsList(toBunkClass,'b');
                }
            }
        });
    }
    private void onClickStateChange(int LayoutChecked1, int LayoutChecked2, String msg,char stats) {
        ((TextView)findViewById(LayoutChecked1)).setText("✔");
        ((TextView)findViewById(LayoutChecked2)).setText("");
        ((TextView)findViewById(R.id.rate_change_stats_text)).setText(msg);
        if(stats=='a'){
            this.stats='a';
            this.changeStatsList(toAttendClass,'a');
        }else if(stats=='b'){
            this.stats='b';
            this.changeStatsList(toBunkClass,'b');
        }
    }

    private void changeStatsList(int ListSize,char stats){
        CalculatorStatsListAdapter calculatorStatsListAdapter=new CalculatorStatsListAdapter(this,
                classAttended,classTotal,ListSize,stats);
        setHeightOfRecyclerLayout(ListSize);
        recyclerView.setAdapter(calculatorStatsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void setAttendedBunkClassesView(int getAttendedClasses,int getBunkClasses,int toAchieve){
        ((TextView)findViewById(R.id.toAttend)).setText(String.valueOf(
                getAttendedClasses
        ));
        ((TextView)findViewById(R.id.toBunk)).setText(String.valueOf(
                getBunkClasses
        ));
        TextView msgTextField = (TextView)findViewById(R.id.msgTextField1);

        if(getAttendedClasses==0&&getBunkClasses==0){
            msgTextField.setText("On Track, You can't miss the next lecture.");
            onClickStateChange(R.id.toAttendLayoutChecked,R.id.toBunkLayoutChecked,
                    "On Track",'a');
        }else if(getAttendedClasses>0){
            msgTextField.setText( "You must Attend next "+getAttendedClasses+ " lectures to achieve "+toAchieve+"% Attendance.");
            onClickStateChange(R.id.toAttendLayoutChecked,R.id.toBunkLayoutChecked,
                    "per class attendance growth",'a');
        }else if(getBunkClasses>0){
            msgTextField.setText("On Track, You may Leave next "+getBunkClasses+" lectures maintaining "+toAchieve+"% Attendance.");
            onClickStateChange(R.id.toBunkLayoutChecked,R.id.toAttendLayoutChecked,
                    "per class attendance loss",'b');
        }
    }
    private void setHeightOfRecyclerLayout(int element){
        LinearLayout.LayoutParams param= (LinearLayout.LayoutParams) ((LinearLayout)findViewById(R.id.recycler_view_layout)).getLayoutParams();
        switch (element){
            case 0:
                param.height=130;
                break;
            case 1:
                param.height=255;
                break;
            case 2:
                param.height=385;
                break;
            case 3:
                param.height=515;
                break;
            default:
                param.height=630;
                break;
        }
    }
}
