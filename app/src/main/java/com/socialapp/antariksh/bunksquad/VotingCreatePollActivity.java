package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class VotingCreatePollActivity extends AppCompatActivity {
    private TextView addNewInputBoxAndroid,removeNewInputBox;
    private LinearLayout answerOptionsLayout;
    private Button createPollButton;
    private FirebaseFirestore firestoreDB;
    private FirebaseUser bunkSquadUser;
    private TextInputEditText titleInput,descriptionInput,validityInput;
    private TextInputLayout titleInputLayout,descriptionInputLayout,validityInputLayout;
    Calendar validityInputDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_create_poll);

        addNewInputBoxAndroid=findViewById(R.id.addMoreAnswerOption);
        removeNewInputBox=findViewById(R.id.removeAnswerOption);
        answerOptionsLayout=findViewById(R.id.answerOptionInputBoxListLayout);
        createPollButton=findViewById(R.id.createPollButton);
        validityInputLayout=findViewById(R.id.validityInputLayout);
        validityInput=findViewById(R.id.validityInput);

        //initialize textbox and layout
        titleInput=findViewById(R.id.titleQuestionInput);
        titleInputLayout=findViewById(R.id.titleQuestionInputLayout);
        descriptionInput=findViewById(R.id.descriptionInput);
        descriptionInputLayout=findViewById(R.id.descriptionInputLayout);
        validityInputDateTime=Calendar.getInstance();
        firestoreDB=FirebaseFirestore.getInstance();
        bunkSquadUser = FirebaseAuth.getInstance().getCurrentUser();

        //back Button click listener
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView)findViewById(R.id.groupName)).setText(getIntent().getStringExtra("GroupName"));
        setAnswerOptionButton();
        //initially two textInputBox
        AddNewAnswerInputBox();
        AddNewAnswerInputBox();

        setValiditySelector();
        setCreatePollListener();
    }

    private void setValiditySelector() {
        validityInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar presentTime = Calendar.getInstance();
                final int mYear = presentTime.get(Calendar.YEAR);
                int mMonth = presentTime.get(Calendar.MONTH);
                int mDay = presentTime.get(Calendar.DAY_OF_MONTH);
                final int mHour = presentTime.get(Calendar.HOUR_OF_DAY);
                final int mMinute = presentTime.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(VotingCreatePollActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                                // Launch Time Picker Dialog
                                TimePickerDialog timePickerDialog = new TimePickerDialog(VotingCreatePollActivity.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                validityInputDateTime.set(year,monthOfYear,dayOfMonth,hourOfDay,minute);
                                                Calendar calendarTemp1hour = Calendar.getInstance();
                                                calendarTemp1hour.set(Calendar.MINUTE,calendarTemp1hour.get(Calendar.MINUTE)+60);
                                                if(validityInputDateTime.after(calendarTemp1hour)){
                                                    validityInput.setText(String.valueOf(validityInputDateTime.getTime()));
                                                    descriptionInputLayout.setErrorEnabled(false);
                                                    titleInputLayout.setErrorEnabled(false);
                                                    validityInputLayout.setErrorEnabled(false);
                                                }else{
                                                    descriptionInputLayout.setErrorEnabled(false);
                                                    titleInputLayout.setErrorEnabled(false);
                                                    validityInputLayout.setError("Select validity to future time.");
                                                }
                                            }
                                        }, mHour, mMinute, false);
                                timePickerDialog.show();
                            }
                        }, mYear, mMonth, mDay);
                presentTime.set(Calendar.DATE,presentTime.get(Calendar.DATE));
                datePickerDialog.getDatePicker().setMinDate(presentTime.getTimeInMillis());
                presentTime.set(Calendar.DATE,presentTime.get(Calendar.DATE)+7);
                datePickerDialog.getDatePicker().setMaxDate(presentTime.getTimeInMillis());
                datePickerDialog.show();
            }
        });
    }

    private void setAnswerOptionButton(){
        addNewInputBoxAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewAnswerInputBox();
            }
        });
        removeNewInputBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAnswerOption();
            }
        });
    }
    private void AddNewAnswerInputBox() {
        if(answerOptionsLayout.getChildCount()>4){
            Snackbar.make(findViewById(android.R.id.content),"You cannot add more option.",Snackbar.LENGTH_SHORT).show();
            return;
        }
        TextInputLayout textInputLayout=new TextInputLayout(this);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxBackgroundColor(getResources().getColor(R.color.colorMainScreenWhite));
        textInputLayout.setHintEnabled(false);
        textInputLayout.setId(answerOptionsLayout.getChildCount()+1+10);
        TextInputEditText textInputEditText=new TextInputEditText(textInputLayout.getContext());
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; ++i) {
                    if (!Pattern.compile("[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890]*").matcher(String.valueOf(source.charAt(i))).matches()) {
                        return "";
                    }
                }
                return null;
            }
        };
        textInputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        textInputEditText.setFilters(new InputFilter[]{filter,new InputFilter.LengthFilter(22)});
        textInputEditText.setPadding(20,20,20,20);
        textInputEditText.setHint("Option "+(answerOptionsLayout.getChildCount()+1));
        textInputEditText.setMaxLines(2);
        textInputEditText.setId(answerOptionsLayout.getChildCount()+1);

        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,6,0,6);

        textInputLayout.addView(textInputEditText);
        answerOptionsLayout.addView(textInputLayout,layoutParams);

    }
    private void removeAnswerOption(){
        if(answerOptionsLayout.getChildCount()>2){
            answerOptionsLayout.removeViewAt(answerOptionsLayout.getChildCount()-1);
        }else{
            Snackbar.make(findViewById(android.R.id.content),"You cannot remove more option.",Snackbar.LENGTH_SHORT).show();
        }
    }
    private void setCreatePollListener() {
        createPollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleInput.getText().toString().isEmpty()){
                    titleInputLayout.setError("Empty Field !!!");
                    descriptionInputLayout.setErrorEnabled(false);
                    validityInputLayout.setErrorEnabled(false);
                    return;
                }
                if(validityInput.getText().toString().isEmpty()){
                    descriptionInputLayout.setErrorEnabled(false);
                    titleInputLayout.setErrorEnabled(false);
                    validityInputLayout.setError("Empty Field !!!");
                    return;
                }
                //setting error false for field
                titleInputLayout.setErrorEnabled(false);
                descriptionInputLayout.setErrorEnabled(false);
                validityInputLayout.setErrorEnabled(false);

                for(int noOfOption=1;noOfOption<answerOptionsLayout.getChildCount()+1;noOfOption++){
                    TextInputLayout optionLayout=findViewById(10+noOfOption);
                    TextInputEditText optionInput=findViewById(noOfOption);
                    if(optionInput.getText().toString().isEmpty()){
                        optionLayout.setError("Empty option !");
                        return;
                    }else{
                        optionLayout.setErrorEnabled(false);
                    }
                }
                DocumentReference pollInfoDoc=firestoreDB
                        .collection("BunkSquadVoting")
                        .document();
                Map<String, Object> pollsInfo = new HashMap<>();
                pollsInfo.put("groupId",getIntent().getStringExtra("GroupId"));
                pollsInfo.put("groupName",getIntent().getStringExtra("GroupName"));
                pollsInfo.put("title",titleInput.getText().toString());
                pollsInfo.put("description",descriptionInput.getText().toString());
                pollsInfo.put("lastDate",validityInputDateTime.getTime());
                pollsInfo.put("createdBy",bunkSquadUser.getDisplayName());
                pollsInfo.put("createdById",bunkSquadUser.getUid());
                pollsInfo.put("createdOn", Timestamp.now());
                pollsInfo.put("isOn",true);//on,off,result

                ArrayList<String> options=new ArrayList<>();
                ArrayList<Integer> noOfVotes = new ArrayList<>();
                for(int i=1;i<answerOptionsLayout.getChildCount()+1;i++){
                    TextInputEditText optionInput=findViewById(i);
                    options.add(optionInput.getText().toString());
                    noOfVotes.add(0);
                }
                pollsInfo.put("optionAnswer", options);
                pollsInfo.put("NumberOfVotes",noOfVotes);

                WriteBatch batch = firestoreDB.batch();
                batch.set(pollInfoDoc,pollsInfo);
                
                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Snackbar.make(findViewById(android.R.id.content),"successfully created.",Snackbar.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Snackbar.make(findViewById(android.R.id.content),task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}