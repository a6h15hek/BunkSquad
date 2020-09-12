package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.internal.$Gson$Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BunkSquadSignUpActivity extends AppCompatActivity {
    //textBox Input variable
    TextInputEditText nameOfUser,emailOfUser,usernameOfUser,passwordOfUser,ConfirmPasswordOfUser;
    TextInputLayout nameOfUserLayout,emailOfUserLayout,usernameOfUserLayout,passwordOfUserLayout,ConfirmPasswordOfUserLayout;
    Button signUpButton;
    ProgressBar loadingIcon;


    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fireStoreDB;
    CollectionReference usersReference;
    Query usernameCheckQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bunksquad_sign_up);


        //initialize firebase variables
        firebaseAuth=FirebaseAuth.getInstance();
        fireStoreDB=FirebaseFirestore.getInstance();
        usersReference=fireStoreDB.collection("BunkSquadUsers");

        //initialize variable
        loadingIcon=findViewById(R.id.loadingProgressBar);
        nameOfUser=findViewById(R.id.signUpName);
        emailOfUser=findViewById(R.id.signUpEmail);
        usernameOfUser=findViewById(R.id.signUpUsername);
        passwordOfUser=findViewById(R.id.signUpPassword);
        ConfirmPasswordOfUser=findViewById(R.id.signUpConfirmPassword);
        signUpButton=findViewById(R.id.signUpButton);

        //initialize layout variable
        nameOfUserLayout=findViewById(R.id.signUpNameLayout);
        emailOfUserLayout=findViewById(R.id.signUpEmailLayout);
        usernameOfUserLayout=findViewById(R.id.signUpUsernameLayout);
        passwordOfUserLayout=findViewById(R.id.signUpPasswordLayout);
        ConfirmPasswordOfUserLayout=findViewById(R.id.signUpConfirmPasswordLayout);

        setInputBoxClickedListener();
        setSignUpButtonListener();
        ((TextView)findViewById(R.id.goToSignInButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BunkSquadSignUpActivity.this, BunkSquadSignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        usernameOfUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                checkUsernameExist(usernameOfUser.getText().toString());
            }
        });
        usernameOfUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                checkUsernameExist(usernameOfUser.getText().toString());
            }
        });
    }
    private void checkUsernameExist(String username){
        usernameOfUserLayout.setErrorEnabled(false);
        usernameOfUserLayout.setHelperTextEnabled(false);
        if(username.length()<1){
            return;
        }
        if(username.length()<4){
            usernameOfUserLayout.setError("username should at least 6 char long.");
        }else{
            usernameCheckQuery=usersReference.whereEqualTo("username",username);
            usernameCheckQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            usernameOfUserLayout.setHelperText("✔ username is available.");
                        } else {
                            usernameOfUserLayout.setError("✖ username not available.");
                        }
                    } else {
                        Toast.makeText(BunkSquadSignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private void setInputBoxClickedListener() {
        nameOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnInputBoxOnClick();
            }
        });
        usernameOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnInputBoxOnClick();
                usernameOfUserLayout.setHelperTextEnabled(false);
            }
        });
        emailOfUser.setOnClickListener(new View.OnClickListener() {
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
        ConfirmPasswordOfUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnInputBoxOnClick();
            }
        });
    }
    private void actionOnInputBoxOnClick(){
        nameOfUserLayout.setErrorEnabled(false);
        emailOfUserLayout.setErrorEnabled(false);
        usernameOfUserLayout.setErrorEnabled(false);
        passwordOfUserLayout.setErrorEnabled(false);
        ConfirmPasswordOfUserLayout.setErrorEnabled(false);
    }

    private void setSignUpButtonListener() {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameOfUser.getText().toString().isEmpty()){
                    nameOfUser.setSelection(nameOfUser.getText().length());
                    nameOfUser.requestFocus();
                    nameOfUserLayout.setError("Empty Input!");
                    emailOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setErrorEnabled(false);
                    ConfirmPasswordOfUserLayout.setErrorEnabled(false);
                    return;
                }
                final String usernameStr=usernameOfUser.getText().toString();
                if(usernameStr.isEmpty()||usernameStr.length()<4){
                    usernameOfUser.setSelection(usernameStr.length());
                    usernameOfUser.requestFocus();
                    nameOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setError("Username should be at least 4 character");
                    emailOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setErrorEnabled(false);
                    ConfirmPasswordOfUserLayout.setErrorEnabled(false);
                    return;
                }

                if(emailOfUser.getText().toString().isEmpty()){
                    emailOfUser.setSelection(emailOfUser.getText().length());
                    emailOfUser.requestFocus();
                    nameOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setErrorEnabled(false);
                    emailOfUserLayout.setError("Empty Input!");
                    passwordOfUserLayout.setErrorEnabled(false);
                    ConfirmPasswordOfUserLayout.setErrorEnabled(false);
                    return;
                }
                if(!isEmailValid(emailOfUser.getText().toString())){
                    emailOfUser.setSelection(emailOfUser.getText().length());
                    emailOfUser.requestFocus();
                    nameOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setErrorEnabled(false);
                    emailOfUserLayout.setError("Invalid email!");
                    passwordOfUserLayout.setErrorEnabled(false);
                    ConfirmPasswordOfUserLayout.setErrorEnabled(false);
                    return;
                }
                if(passwordOfUser.getText().toString().isEmpty()){
                    passwordOfUser.setSelection(passwordOfUser.getText().length());
                    passwordOfUser.requestFocus();
                    nameOfUserLayout.setErrorEnabled(false);
                    emailOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setError("Empty Input!");
                    ConfirmPasswordOfUserLayout.setErrorEnabled(false);
                    return;
                }
                if(passwordOfUser.getText().toString().length()<8){
                    passwordOfUser.setSelection(passwordOfUser.getText().length());
                    passwordOfUser.requestFocus();
                    nameOfUserLayout.setErrorEnabled(false);
                    emailOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setError("Password length should be at least 8 digit!");
                    ConfirmPasswordOfUserLayout.setErrorEnabled(false);
                    return;
                }
                if(!(ConfirmPasswordOfUser.getText().toString()).equals(passwordOfUser.getText().toString())){
                    ConfirmPasswordOfUser.setSelection(ConfirmPasswordOfUser.getText().length());
                    ConfirmPasswordOfUser.requestFocus();
                    nameOfUserLayout.setErrorEnabled(false);
                    emailOfUserLayout.setErrorEnabled(false);
                    usernameOfUserLayout.setErrorEnabled(false);
                    passwordOfUserLayout.setErrorEnabled(false);
                    ConfirmPasswordOfUserLayout.setError("Confirm password not matched!");
                    return;
                }
                if(!isNetworkConnected()){
                    Snackbar.make(findViewById(android.R.id.content),"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                loadingIcon.setVisibility(View.VISIBLE);
                //main code goes here
                usernameCheckQuery=usersReference.whereEqualTo("username",usernameStr);
                usernameCheckQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                usernameOfUserLayout.setHelperText("✔ username is available.");
                                SignUpUser(nameOfUser.getText().toString(),emailOfUser.getText().toString(),
                                        usernameStr,passwordOfUser.getText().toString());
                            } else {
                                loadingIcon.setVisibility(View.GONE);
                                usernameOfUserLayout.setError("✖ username not available.");
                                usernameOfUser.setSelection(usernameOfUser.length());
                                usernameOfUser.requestFocus();
                            }
                        } else {
                            loadingIcon.setVisibility(View.GONE);
                            Toast.makeText(BunkSquadSignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
    private void SignUpUser(final String nameOfUser, String emailOfUser, String usernameOfUser, String password){
        final Map<String, Object> userUniqueInfo = new HashMap<>();
        userUniqueInfo.put("name",nameOfUser);
        userUniqueInfo.put("email", emailOfUser);
        userUniqueInfo.put("username",usernameOfUser);
        firebaseAuth.createUserWithEmailAndPassword(emailOfUser,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getInstance().getCurrentUser();
                            //Setting up batch
                            WriteBatch batch = fireStoreDB.batch();
                            //1st database set
                            DocumentReference BasicInfoDoc=fireStoreDB.collection("BunkSquadUsers")
                                    .document(user.getUid());
                                    batch.set(BasicInfoDoc,userUniqueInfo);
                            //2nd database set
                            userUniqueInfo.remove("email");
                            DocumentReference UserDataDoc=fireStoreDB.collection("BunkSquadUserData")
                                    .document(user.getUid());
                            batch.set(UserDataDoc,userUniqueInfo);
                            // Commit the batch
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(BunkSquadSignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nameOfUser)
                                    .build();
                            user.updateProfile(profileUpdates);

                            user.sendEmailVerification();
                            Toast.makeText(BunkSquadSignUpActivity.this,"Sign Up successfully. Verify your account to continue",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(BunkSquadSignUpActivity.this, BunksquadMainActivity.class);
                            intent.putExtra("FRAGMENT","MB");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(BunkSquadSignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        loadingIcon.setVisibility(View.GONE);
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
