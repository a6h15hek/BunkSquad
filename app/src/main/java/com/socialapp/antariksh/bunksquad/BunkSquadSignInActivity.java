package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BunkSquadSignInActivity extends AppCompatActivity {
    TextInputEditText usernameOfUser,passwordOfUser;
    TextInputLayout usernameOfUserLayout,passwordOfUserLayout;
    Button signInButton;
    ProgressBar loadingIcon;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bunksquad_sign_in);
        //initialize firebase variable
        firebaseAuth=FirebaseAuth.getInstance();

        //initialize variable
        loadingIcon=findViewById(R.id.loadingProgressBar);
        usernameOfUser=findViewById(R.id.signUpUsername);
        passwordOfUser=findViewById(R.id.signUpPassword);
        signInButton=findViewById(R.id.signInButton);

        //initialize layout variable
        usernameOfUserLayout=findViewById(R.id.signUpUsernameLayout);
        passwordOfUserLayout=findViewById(R.id.signUpPasswordLayout);

        setInputBoxClickedListener();
        setSignInButtonListener();
        ((TextView)findViewById(R.id.goToSignUpButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BunkSquadSignInActivity.this, BunkSquadSignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ((TextView)findViewById(R.id.forgetPassword)).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BunkSquadSignInActivity.this);
                final EditText inputEmail = new EditText(BunkSquadSignInActivity.this);
                inputEmail.setHint("Enter Email");
                inputEmail.setGravity(android.view.Gravity.TOP|android.view.Gravity.LEFT);
                inputEmail.setLines(1);
                inputEmail.setMaxLines(1);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                String UserEmail=inputEmail.getText().toString();
                                if(UserEmail.isEmpty()||!isEmailValid(UserEmail)){
                                    Snackbar.make(findViewById(android.R.id.content),"Invalid email!",Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                   FirebaseAuth.getInstance().sendPasswordResetEmail(UserEmail)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Snackbar.make(findViewById(android.R.id.content),"Password Reset Link sent to your Account.",Snackbar.LENGTH_SHORT).show();
                                                    }else{
                                                        Snackbar.make(findViewById(android.R.id.content),"Invalid Email. User not found",Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                builder.setTitle("Forget Password")
                        .setMessage("Password Reset Link will be sent to your email.")
                        .setView(inputEmail,50,0,50,50)
                        .setPositiveButton("Send Reset Link", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).create().show();
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    private void setInputBoxClickedListener() {
        usernameOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnInputBoxOnClick();
            }
        });
        passwordOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnInputBoxOnClick();
            }
        });
    }
    private void actionOnInputBoxOnClick(){
        usernameOfUserLayout.setErrorEnabled(false);
        passwordOfUserLayout.setErrorEnabled(false);
    }
    private void setSignInButtonListener() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usernameOfUser.getText().toString().isEmpty()){
                    usernameOfUser.setSelection(usernameOfUser.getText().length());
                    usernameOfUser.requestFocus();
                    usernameOfUserLayout.setError("Empty Input!");
                    passwordOfUserLayout.setErrorEnabled(false);
                    return;
                }
                if(passwordOfUser.getText().toString().isEmpty()){
                    passwordOfUser.setSelection(passwordOfUser.getText().length());
                    passwordOfUser.requestFocus();
                    usernameOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setError("Empty Input!");
                    return;
                }
                if(passwordOfUser.getText().toString().length()<8){
                    passwordOfUser.setSelection(passwordOfUser.getText().length());
                    passwordOfUser.requestFocus();
                    usernameOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setError("Password length should be at least 8 digit!");
                    return;
                }
                if(isNetworkConnected()){
                    loadingIcon.setVisibility(View.VISIBLE);
                    if(!isEmailValid(usernameOfUser.getText().toString())){
                        FirebaseFirestore db=FirebaseFirestore.getInstance();
                        db.collection("BunkSquadUsers")
                                .whereEqualTo("username",usernameOfUser.getText().toString())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        loadingIcon.setVisibility(View.GONE);
                                        if(task.isSuccessful()){
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                SignInWithEmail(document.getString("email"));
                                            }
                                            if(task.getResult().size()==0){
                                                Snackbar.make(findViewById(android.R.id.content),"user not found.",Snackbar.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            Snackbar.make(findViewById(android.R.id.content),task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        SignInWithEmail(usernameOfUser.getText().toString());
                    }
                }else{
                    Snackbar.make(findViewById(android.R.id.content),"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void SignInWithEmail(String email){
        firebaseAuth.signInWithEmailAndPassword(email,passwordOfUser.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loadingIcon.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Toast.makeText(BunkSquadSignInActivity.this,"Sign In successfully",Toast.LENGTH_SHORT).show();
                            FirebaseUser BunkSquadUser=firebaseAuth.getCurrentUser();
                            Intent intent = new Intent(BunkSquadSignInActivity.this, BunksquadMainActivity.class);
                            intent.putExtra("FRAGMENT","MB");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(BunkSquadSignInActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
