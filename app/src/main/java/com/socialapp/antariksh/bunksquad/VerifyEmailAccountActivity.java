package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailAccountActivity extends AppCompatActivity {
    TextView userEmailView;
    Button iHaveAlreadyVerifiedButton,sendEmailVerification,signOutButton;
    FirebaseAuth firebaseAuth;
    FirebaseUser bunkSquadUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email_account);
        firebaseAuth=FirebaseAuth.getInstance();
        bunkSquadUser=firebaseAuth.getCurrentUser();
        //initialize variable
        userEmailView=findViewById(R.id.userEmail);
        iHaveAlreadyVerifiedButton=findViewById(R.id.iHaveVerifiedContinue);
        sendEmailVerification=findViewById(R.id.sendEmailVerification);
        signOutButton=findViewById(R.id.signOutButton);
        userEmailView.setText(bunkSquadUser.getEmail());
        iHaveAlreadyVerifiedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bunkSquadUser.reload();
                if(bunkSquadUser.isEmailVerified()){
                    Intent intent = new Intent(VerifyEmailAccountActivity.this, BunksquadMainActivity.class);
                    intent.putExtra("FRAGMENT","MB");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    Snackbar.make(findViewById(android.R.id.content),"you have not verified. If you have Verified then wait for some time.",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        sendEmailVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bunkSquadUser.reload();
                if(bunkSquadUser.isEmailVerified()){
                    Snackbar.make(findViewById(R.id.content),"Your account is verified. Click continue button.",Snackbar.LENGTH_SHORT).show();
                }else{
                    bunkSquadUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Snackbar.make(findViewById(android.R.id.content),"Verification Email is Sent to you.",Snackbar.LENGTH_SHORT).show();
                            }else{
                                Snackbar.make(findViewById(android.R.id.content),"Error in sending mail. Check your Connection.",Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Toast.makeText(VerifyEmailAccountActivity.this,"Sign Out successfully",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VerifyEmailAccountActivity.this, BunksquadMainActivity.class);
                intent.putExtra("FRAGMENT","MB");
                startActivity(intent);
                finish();
            }
        });
    }
}
