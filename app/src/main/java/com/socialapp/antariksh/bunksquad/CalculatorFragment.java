package com.socialapp.antariksh.bunksquad;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;


public class CalculatorFragment extends Fragment {
    private int[] numericButtons = {R.id.calBtn0,R.id.calBtn1,R.id.calBtn2,R.id.calBtn3,R.id.calBtn4,
            R.id.calBtn5,R.id.calBtn6,R.id.calBtn7,R.id.calBtn8,R.id.calBtn9};
    private TextView AttendedClassTextBox;
    private TextView totalClassTextBox;
    private TextView SelectedTextView;
    View view; //To get the view of layout to get id of element of layout

    private boolean lastNumeric; //Represent whether last digit is numeric or not
    private boolean stateError; //Represent whether the current state is error or not

    public CalculatorFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.view = getView();
        this.totalClassTextBox = (TextView) view.findViewById(R.id.inputBoxTotal);
        this.AttendedClassTextBox = (TextView) view.findViewById(R.id.inputBoxAttended);
        this.SelectedTextView=AttendedClassTextBox;
        setColorSelectedTexBox(R.id.textInputBoxAttendedClass,R.id.attendedTitle,"#3419ac",
                R.id.textInputBoxTotalClass,R.id.totalTitle,"#4f5354");
        setNumericButtonOnClickListener();
        setActionButtonOnClickListner();
    }

    //On Numeric value click Listener activates a function
    private void setNumericButtonOnClickListener() {
        // Create a common OnClickListener
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SelectedTextView.length()>2){
                    Snackbar.make(view.findViewById(R.id.snackbar_message), "max input limit 3. 😥",
                            (Snackbar.LENGTH_SHORT)).show();
                    return;
                }
                // Just append/set the text of clicked button
                Button button = (Button) v;
                if (stateError) {
                    // If current state is Error, replace the error message
                    SelectedTextView.setText(button.getText());
                    stateError = false;
                } else {
                    // If not, already there is a valid expression so append to it
                    SelectedTextView.append(button.getText());
                }
                // Set the flag
                lastNumeric = true;
            }
        };
        // Assign the listener to all the numeric buttons
        for (int id : numericButtons) {
            view.findViewById(id).setOnClickListener(listener);
        }
    }

    //On Action button Click Listener
    private void setActionButtonOnClickListner(){
        view.findViewById(R.id.calBackSpace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SelectedTextView.length()!=0){
                    SelectedTextView.setText(SelectedTextView.getText().toString().substring(0,
                            SelectedTextView.getText().length() - 1)); // Clear the screen
                } else{
                    SelectedTextView.setText("");
                }
                // Reset all the states and flags
                lastNumeric = false;
                stateError = false;
            }
        });
        //function to respond when attended TextBox Clicked
        view.findViewById(R.id.textInputBoxAttendedClass).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setColorSelectedTexBox(R.id.textInputBoxAttendedClass,R.id.attendedTitle,"#3419ac",
                        R.id.textInputBoxTotalClass,R.id.totalTitle,"#4f5354");
                SelectedTextView = AttendedClassTextBox;
            }
        });
        //function to respond when Total TextBox Clicked
        view.findViewById(R.id.textInputBoxTotalClass).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setColorSelectedTexBox(R.id.textInputBoxTotalClass,R.id.totalTitle,"#3419ac",
                        R.id.textInputBoxAttendedClass,R.id.attendedTitle,"#4f5354");
                SelectedTextView = totalClassTextBox;
            }
        });
        //to switch between textBox
        view.findViewById(R.id.CalNextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SelectedTextView==AttendedClassTextBox){
                    setColorSelectedTexBox(R.id.textInputBoxTotalClass,R.id.totalTitle,"#3419ac",
                            R.id.textInputBoxAttendedClass,R.id.attendedTitle,"#4f5354");
                    SelectedTextView = totalClassTextBox;
                }else{
                    setColorSelectedTexBox(R.id.textInputBoxAttendedClass,R.id.attendedTitle,"#3419ac",
                            R.id.textInputBoxTotalClass,R.id.totalTitle,"#4f5354");
                    SelectedTextView = AttendedClassTextBox;
                }
            }
        });
        view.findViewById(R.id.getResult).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AttendedClassTextBox.length()>0&&totalClassTextBox.length()>0){
                    int classAttended = Integer.valueOf(AttendedClassTextBox.getText().toString());
                    int classTotal = Integer.valueOf(totalClassTextBox.getText().toString());
                    if(classTotal>0&&classAttended>=0&&classTotal>=classAttended){
                        Intent intent = new Intent(getActivity(), AttendanceReportActivity.class);
                        intent.putExtra("classAttended",String.valueOf(classAttended));
                        intent.putExtra("classTotal",String.valueOf(classTotal));
                        intent.putExtra("toAchieve",String.valueOf(75));
                        intent.putExtra("headerMsg","Report");
                        startActivity(intent);
                    }else{
                        Snackbar.make(view.findViewById(R.id.snackbar_message), "Invalid input",
                                (Snackbar.LENGTH_SHORT)).show();
                        return;
                    }
                }else{
                    Snackbar.make(view.findViewById(R.id.snackbar_message), "Empty input Box.",
                            (Snackbar.LENGTH_SHORT)).show();
                    return;
                }
            }
        });
    }

    private void setColorSelectedTexBox(int selectedLayoutId, int selectedTitle, String selectedColorString,
                                        int unSelectedLayoutId, int unSelectedTitle, String unSelectedColorString) {
        ((GradientDrawable)(view.findViewById(selectedLayoutId)).getBackground()).setStroke(9,Color.parseColor(selectedColorString));
        ((TextView)view.findViewById(selectedTitle)).setTextColor(Color.parseColor(selectedColorString));
        ((TextView)view.findViewById(selectedTitle)).setTextSize(16);
        ((GradientDrawable)(view.findViewById(unSelectedLayoutId)).getBackground()).setStroke(7,Color.parseColor(unSelectedColorString));
        ((TextView)view.findViewById(unSelectedTitle)).setTextColor(Color.parseColor(unSelectedColorString));
        ((TextView)view.findViewById(unSelectedTitle)).setTextSize(16);
    }

}
