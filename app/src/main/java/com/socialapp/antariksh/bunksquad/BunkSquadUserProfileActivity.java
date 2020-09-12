package com.socialapp.antariksh.bunksquad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BunkSquadUserProfileActivity extends AppCompatActivity {
    private final int SELECT_IMAGE_CODE=1234;
    TextInputEditText inputNameOfUser,inputUsernameOfUser;
    Button updateUserInfoButton;
    ProgressBar loadingProgressBar,loadingProgressBarMain;
    ImageView userProfileAvatar;
    TextView headerUsername;
    TextInputLayout inputNameOfUserLayout,inputUsernameOfUserLayout;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    FirebaseFirestore fireStoreDB;
    DocumentReference UserDocRef;
    DocumentReference UserDataDocRef;
    CollectionReference usersReference;
    Query usernameCheckQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bunksquad_user_profile);
        //initialize view variable
        inputNameOfUser=findViewById(R.id.inputNameOfUser);
        inputUsernameOfUser=findViewById(R.id.inputUsernameOfUser);
        updateUserInfoButton=findViewById(R.id.UpdateUserInfoUpdateButton);
        loadingProgressBar=findViewById(R.id.loadingProgressBar);
        loadingProgressBarMain=findViewById(R.id.loadingProgressBarMain);
        userProfileAvatar=findViewById(R.id.user_avatar);
        headerUsername=findViewById(R.id.headerUsername);
        inputNameOfUserLayout=findViewById(R.id.inputNameOfUserLayout);
        inputUsernameOfUserLayout=findViewById(R.id.inputUsernameOfUserLayout);


        //loadingProgressBarMain.setVisibility(View.VISIBLE);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseStorage=FirebaseStorage.getInstance();
        fireStoreDB=FirebaseFirestore.getInstance();
        usersReference=fireStoreDB.collection("BunkSquadUsers");
        UserDocRef=fireStoreDB.collection("BunkSquadUsers").document(firebaseUser.getUid());
        UserDataDocRef=fireStoreDB.collection("BunkSquadUserData").document(firebaseUser.getUid());

        UserDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    inputUsernameOfUser.setText(snapshot.getString("username"));
                    headerUsername.setText(snapshot.getString("username"));
                }
            }
        });

        if(firebaseUser!=null){
            ((TextView)findViewById(R.id.userEmail)).setText(firebaseUser.getEmail());
            ((TextView)findViewById(R.id.NameOfUser)).setText(firebaseUser.getDisplayName());
            ((LinearLayout)findViewById(R.id.SignOutButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseAuth.signOut();
                    Toast.makeText(BunkSquadUserProfileActivity.this,"Sign Out successfully",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BunkSquadUserProfileActivity.this, BunksquadMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("FRAGMENT","MB");
                    startActivity(intent);
                    finish();
                }
            });

            //set the values of Input box
            if(firebaseUser.getDisplayName()!=null){
                inputNameOfUser.setText(firebaseUser.getDisplayName());
            }
            if(firebaseUser.getPhotoUrl()!=null){
                Glide.with(this)
                        .load(firebaseUser.getPhotoUrl())
                        .into(userProfileAvatar);
            }

            setUpdateAvatarOfUser();
            setUpdateInfoListener();
            setChangeEmailListener();
            setChangeUserPasswordListener();
            setDeleteUserAccountListener();
            ((ImageView)findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkRunningProcessIfPressedBack();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        checkRunningProcessIfPressedBack();
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void setUpdateAvatarOfUser() {
        ((ImageView)findViewById(R.id.selectImageForAvatar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(intent.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(intent,SELECT_IMAGE_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_CODE && resultCode == RESULT_OK && null != data) {
            Uri imagePath = data.getData();
            try {
                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                //((ImageView)findViewById(R.id.user_avatar)).setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,8,baos);

                //loading bar appear
                if(!isNetworkConnected()){
                    Snackbar.make(findViewById(android.R.id.content),"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                loadingProgressBarMain.setVisibility(View.VISIBLE);

                final StorageReference photoReference = firebaseStorage.getReference()
                        .child("userAvatar")
                        .child(firebaseUser.getUid()+".jpeg");
                photoReference.putBytes(baos.toByteArray())
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    photoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            firebaseUser.updateProfile(
                                                    new UserProfileChangeRequest.Builder()
                                                            .setPhotoUri(uri)
                                                            .build()
                                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        loadingProgressBarMain.setVisibility(View.GONE);
                                                        userProfileAvatar.setImageBitmap(bitmap);
                                                        Snackbar.make(findViewById(android.R.id.content), "User avatar updated successfully. ", Snackbar.LENGTH_SHORT).show();
                                                    }else{
                                                        loadingProgressBarMain.setVisibility(View.GONE);
                                                        Snackbar.make(findViewById(android.R.id.content), "User avatar not updated. "+task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }else{
                                    loadingProgressBarMain.setVisibility(View.GONE);
                                    Snackbar.make(findViewById(android.R.id.content), "User Avatar not updated. "+task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void setUpdateInfoListener() {
        updateUserInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkConnected()){
                    Snackbar.make(findViewById(android.R.id.content),"You are not Connected to Network",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final String nameOfUser=inputNameOfUser.getText().toString();
                final String usernameOfUser=inputUsernameOfUser.getText().toString();
                if(nameOfUser.isEmpty()){
                    inputNameOfUserLayout.setError("Empty input!");
                    inputUsernameOfUserLayout.setErrorEnabled(false);
                    return;
                }
                if(usernameOfUser.isEmpty()||usernameOfUser.length()<4){
                    inputNameOfUserLayout.setErrorEnabled(false);
                    inputUsernameOfUserLayout.setError("Username should be at least 4 character");
                    return;
                }

                loadingProgressBar.setVisibility(View.VISIBLE);
                if(headerUsername.getText().toString().equals(inputUsernameOfUser.getText().toString())){
                    updateUserInfo(nameOfUser,usernameOfUser);
                    return;
                }
                usernameCheckQuery=usersReference.whereEqualTo("username",usernameOfUser);
                usernameCheckQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                inputUsernameOfUserLayout.setHelperTextColor(ColorStateList.valueOf(Color.parseColor("#008000")));
                                inputUsernameOfUserLayout.setHelperText("✔ username is available.");
                                updateUserInfo(nameOfUser,usernameOfUser);
                            } else {
                                loadingProgressBar.setVisibility(View.GONE);
                                inputUsernameOfUserLayout.setError("✖ username not available.");
                                inputUsernameOfUser.setSelection(inputUsernameOfUser.length());
                                inputUsernameOfUser.requestFocus();
                            }
                        } else {
                            Toast.makeText(BunkSquadUserProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });
    }

    private void updateUserInfo(final String nameOfUser, final String usernameOfUser){
        //main code goes here
        firebaseUser.updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nameOfUser)
                        .build()
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                inputNameOfUserLayout.setErrorEnabled(false);
                inputUsernameOfUserLayout.setErrorEnabled(false);
                if(task.isSuccessful()){
                    //updating username
                    Map<String, Object> userUniqueInfo = new HashMap<>();
                    userUniqueInfo.put("name",nameOfUser);
                    userUniqueInfo.put("email",firebaseUser.getEmail());
                    userUniqueInfo.put("username",usernameOfUser);
                    WriteBatch batch = fireStoreDB.batch();
                    //1sr database set
                    batch.set(UserDocRef,userUniqueInfo, SetOptions.merge());

                    //2nd database set
                    userUniqueInfo.remove("email");
                    DocumentReference UserDataDoc=fireStoreDB.collection("BunkSquadUserData")
                            .document(firebaseUser.getUid());
                    batch.set(UserDataDoc,userUniqueInfo,SetOptions.merge());

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingProgressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                ((TextView)findViewById(R.id.NameOfUser)).setText(firebaseUser.getDisplayName());
                                inputUsernameOfUser.setText(usernameOfUser);
                                headerUsername.setText(usernameOfUser);
                                Snackbar.make(findViewById(android.R.id.content), "User Info Updated Successfully", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Snackbar.make(findViewById(android.R.id.content), "User Info not Updated. "+task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setChangeUserPasswordListener(){
        ((LinearLayout)findViewById(R.id.changePasswordButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BunkSquadUserProfileActivity.this);
                                ViewGroup viewGroup = findViewById(android.R.id.content);
                                final View dialogBoxChangeUserPassword = LayoutInflater.from(BunkSquadUserProfileActivity.this).inflate(R.layout.change_user_password, viewGroup, false);
                                builder.setView(dialogBoxChangeUserPassword);
                                final android.app.AlertDialog alertChangeFormDialogBox = builder.create();
                                alertChangeFormDialogBox.show();

                                ((Button)dialogBoxChangeUserPassword.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertChangeFormDialogBox.dismiss();
                                    }
                                });
                                ((Button)dialogBoxChangeUserPassword.findViewById(R.id.change_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final ProgressBar loadingProgressBar=dialogBoxChangeUserPassword.findViewById(R.id.loadingProgressBar);
                                        final TextInputEditText currentPassword=dialogBoxChangeUserPassword.findViewById(R.id.inputCurrentPassword);
                                        TextInputEditText password=dialogBoxChangeUserPassword.findViewById(R.id.inputNewPassword);
                                        final TextInputEditText confirmPassword=dialogBoxChangeUserPassword.findViewById(R.id.inputNewConfirmPassword);
                                        //initialize layout variable
                                        final TextInputLayout currentPasswordLayout=dialogBoxChangeUserPassword.findViewById(R.id.inputCurrentPasswordLayout);
                                        final TextInputLayout newPasswordLayout=dialogBoxChangeUserPassword.findViewById(R.id.inputNewPasswordLayout);
                                        final TextInputLayout newConfirmPasswordLayout=dialogBoxChangeUserPassword.findViewById(R.id.inputNewConfirmPasswordLayout);

                                        currentPassword.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                currentPasswordLayout.setErrorEnabled(false);
                                                newPasswordLayout.setErrorEnabled(false);
                                                newConfirmPasswordLayout.setErrorEnabled(false);
                                            }
                                        });
                                        password.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                currentPasswordLayout.setErrorEnabled(false);
                                                newPasswordLayout.setErrorEnabled(false);
                                                newConfirmPasswordLayout.setErrorEnabled(false);
                                            }
                                        });
                                        confirmPassword.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                currentPasswordLayout.setErrorEnabled(false);
                                                newPasswordLayout.setErrorEnabled(false);
                                                newConfirmPasswordLayout.setErrorEnabled(false);
                                            }
                                        });
                                        if(currentPassword.getText().toString().isEmpty()){
                                            currentPassword.requestFocus();
                                            currentPasswordLayout.setError("Empty input!");
                                            newPasswordLayout.setErrorEnabled(false);
                                            newConfirmPasswordLayout.setErrorEnabled(false);
                                            return;
                                        }
                                        if(password.getText().toString().isEmpty()||password.getText().toString().length()<8){
                                            password.requestFocus();
                                            currentPasswordLayout.setErrorEnabled(false);
                                            newPasswordLayout.setError("input is empty or length less than 8 digit!");
                                            newConfirmPasswordLayout.setErrorEnabled(false);
                                            return;
                                        }
                                        if(confirmPassword.getText().toString().isEmpty()||confirmPassword.getText().toString().length()<8){
                                            confirmPassword.requestFocus();
                                            currentPasswordLayout.setErrorEnabled(false);
                                            newPasswordLayout.setErrorEnabled(false);
                                            newConfirmPasswordLayout.setError("input is empty or length less than 8 digit!");
                                            return;
                                        }
                                        if(!(password.getText().toString()).equals(confirmPassword.getText().toString())){
                                            confirmPassword.setSelection(confirmPassword.getText().length());
                                            confirmPassword.requestFocus();
                                            currentPasswordLayout.setErrorEnabled(false);
                                            newPasswordLayout.setErrorEnabled(false);
                                            newConfirmPasswordLayout.setError("Confirm new password not matched!");
                                            return;
                                        }
                                        loadingProgressBar.setVisibility(View.VISIBLE);
                                        // Get auth credentials from the user for re-authentication. The example below shows
                                        // email and password credentials but there are multiple possible providers,
                                        // such as GoogleAuthProvider or FacebookAuthProvider.
                                        AuthCredential credential = EmailAuthProvider
                                                .getCredential(firebaseUser.getEmail(), currentPassword.getText().toString());

                                        // Prompt the user to re-provide their sign-in credentials
                                        firebaseUser.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> reauthenticateTask) {
                                                        if(reauthenticateTask.isSuccessful()){
                                                            firebaseUser.updatePassword(confirmPassword.getText().toString())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            loadingProgressBar.setVisibility(View.GONE);
                                                                            if (task.isSuccessful()) {
                                                                                alertChangeFormDialogBox.dismiss();
                                                                                Snackbar.make(findViewById(android.R.id.content), "Password Changed Successfully.", Snackbar.LENGTH_SHORT).show();
                                                                            }else{
                                                                                alertChangeFormDialogBox.dismiss();
                                                                                Snackbar.make(findViewById(android.R.id.content), "Password Not Changed. "+task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }else{
                                                            loadingProgressBar.setVisibility(View.GONE);
                                                            currentPassword.setSelection(currentPassword.getText().length());
                                                            currentPassword.requestFocus();
                                                            currentPasswordLayout.setError("Your Password is incorrect. "+reauthenticateTask.getException().getMessage());
                                                            newPasswordLayout.setErrorEnabled(false);
                                                            newConfirmPasswordLayout.setErrorEnabled(false);
                                                            loadingProgressBar.setVisibility(View.GONE);
                                                            return;
                                                        }
                                                    }
                                                });
                                    }
                                });
                                dialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(BunkSquadUserProfileActivity.this);
                builder.setTitle("Change Password").setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }
    private void setDeleteUserAccountListener(){
        ((LinearLayout)findViewById(R.id.deleteUserAccount)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                final WriteBatch batch = fireStoreDB.batch();
                                final DocumentReference CurrentUserRef=fireStoreDB.collection("BunkSquadUserData").document(firebaseUser.getUid());
                                CurrentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document=task.getResult();
                                            if(document.exists()){
                                                List<Object> groupList=(List<Object>) document.get("Groups");
                                                if(groupList.size()>0){
                                                    dialog.dismiss();
                                                    Snackbar.make(findViewById(android.R.id.content),"You are member of some Groups. Exit all groups to delete accounts.",Snackbar.LENGTH_SHORT).show();
                                                }else{
                                                    //if user is not part of any group
                                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BunkSquadUserProfileActivity.this);
                                                    ViewGroup viewGroup = findViewById(android.R.id.content);
                                                    final View dialogBoxDeleteUserAccount = LayoutInflater.from(BunkSquadUserProfileActivity.this).inflate(R.layout.confirm_deleted_user, viewGroup, false);
                                                    builder.setView(dialogBoxDeleteUserAccount);
                                                    final android.app.AlertDialog alertDeleteFormDialogBox = builder.create();
                                                    alertDeleteFormDialogBox.show();
                                                    ((Button)dialogBoxDeleteUserAccount.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDeleteFormDialogBox.dismiss();
                                                        }
                                                    });
                                                    ((Button)dialogBoxDeleteUserAccount.findViewById(R.id.delete_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            final ProgressBar loadingProgressBar=dialogBoxDeleteUserAccount.findViewById(R.id.loadingProgressBar);
                                                            TextInputEditText password=dialogBoxDeleteUserAccount.findViewById(R.id.inputPassword);
                                                            if(password.getText().toString().isEmpty()){
                                                                ((TextInputLayout)dialogBoxDeleteUserAccount.findViewById(R.id.inputPasswordLayout)).setError("Empty Input!");
                                                                return;
                                                            }
                                                            loadingProgressBar.setVisibility(View.VISIBLE);
                                                            // Get auth credentials from the user for re-authentication. The example below shows
                                                            // email and password credentials but there are multiple possible providers,
                                                            // such as GoogleAuthProvider or FacebookAuthProvider.
                                                            AuthCredential credential = EmailAuthProvider
                                                                    .getCredential(firebaseUser.getEmail(), password.getText().toString());

                                                            // Prompt the user to re-provide their sign-in credentials
                                                            firebaseUser.reauthenticate(credential)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> reauthenticateTask) {
                                                                            if(reauthenticateTask.isSuccessful()){
                                                                                WriteBatch batch = fireStoreDB.batch();
                                                                                batch.delete(UserDocRef);
                                                                                batch.delete(UserDataDocRef);
                                                                                batch.commit();
                                                                                if(firebaseUser.getPhotoUrl()!=null){
                                                                                    StorageReference photoReference = firebaseStorage.getReference()
                                                                                            .child("userAvatar")
                                                                                            .child(firebaseUser.getUid()+".jpeg");
                                                                                    photoReference.delete();
                                                                                }
                                                                                firebaseUser.delete()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    loadingProgressBar.setVisibility(View.GONE);
                                                                                                    alertDeleteFormDialogBox.dismiss();
                                                                                                    Intent intent = new Intent(BunkSquadUserProfileActivity.this, BunksquadMainActivity.class);
                                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                    intent.putExtra("FRAGMENT","MB");
                                                                                                    startActivity(intent);
                                                                                                    finish();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }else{
                                                                                loadingProgressBar.setVisibility(View.GONE);
                                                                                alertDeleteFormDialogBox.dismiss();
                                                                                Snackbar.make(findViewById(android.R.id.content),"Your password is incorrect. "+reauthenticateTask.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                                    dialog.dismiss();
                                                }
                                            }
                                        }
                                    }
                                });

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(BunkSquadUserProfileActivity.this);
                builder.setTitle("Delete Account").setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }
    private void setChangeEmailListener(){
        ((LinearLayout)findViewById(R.id.changeEmailButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BunkSquadUserProfileActivity.this);
                                ViewGroup viewGroup = findViewById(android.R.id.content);
                                final View dialogUpdateEmailLayout = LayoutInflater.from(BunkSquadUserProfileActivity.this).inflate(R.layout.change_user_email, viewGroup, false);
                                builder.setView(dialogUpdateEmailLayout);
                                final android.app.AlertDialog alertUpdateEmailDialogBox = builder.create();
                                alertUpdateEmailDialogBox.show();
                                ((Button)dialogUpdateEmailLayout.findViewById(R.id.cancel_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertUpdateEmailDialogBox.dismiss();
                                    }
                                });
                                ((Button)dialogUpdateEmailLayout.findViewById(R.id.update_email_button_dialogBox)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        final ProgressBar loadingProgressBar=dialogUpdateEmailLayout.findViewById(R.id.loadingProgressBar);
                                        TextInputEditText password=dialogUpdateEmailLayout.findViewById(R.id.inputPassword);
                                        final TextInputEditText email=dialogUpdateEmailLayout.findViewById(R.id.inputUserEmail);
                                        TextInputLayout passwordLayout=dialogUpdateEmailLayout.findViewById(R.id.inputPasswordLayout);
                                        TextInputLayout emailLayout=dialogUpdateEmailLayout.findViewById(R.id.inputUserEmailLayout);
                                        if(password.getText().toString().isEmpty()){
                                            passwordLayout.setError("Empty Input!");
                                            emailLayout.setErrorEnabled(false);
                                            return;
                                        }
                                        if(email.getText().toString().isEmpty()||!isEmailValid(email.getText().toString())){
                                            passwordLayout.setErrorEnabled(false);
                                            emailLayout.setError("Empty Input or invalid email!");
                                            return;
                                        }
                                        loadingProgressBar.setVisibility(View.VISIBLE);
                                        // Get auth credentials from the user for re-authentication. The example below shows
                                        // email and password credentials but there are multiple possible providers,
                                        // such as GoogleAuthProvider or FacebookAuthProvider.
                                        AuthCredential credential = EmailAuthProvider
                                                .getCredential(firebaseUser.getEmail(), password.getText().toString());
                                        // Prompt the user to re-provide their sign-in credentials
                                        firebaseUser.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> reauthenticateTask) {
                                                        if(reauthenticateTask.isSuccessful()){
                                                            firebaseUser.updateEmail(email.getText().toString())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Map<String, Object> userUniqueInfo = new HashMap<>();
                                                                                userUniqueInfo.put("email",firebaseUser.getEmail());
                                                                                UserDocRef.set(userUniqueInfo, SetOptions.merge())
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                loadingProgressBar.setVisibility(View.GONE);
                                                                                                alertUpdateEmailDialogBox.dismiss();
                                                                                                Snackbar.make(findViewById(android.R.id.content), "Email Updated successfully. you need to verify email.", Snackbar.LENGTH_SHORT).show();
                                                                                                Intent intent = new Intent(BunkSquadUserProfileActivity.this, BunksquadMainActivity.class);
                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                intent.putExtra("FRAGMENT","MB");
                                                                                                startActivity(intent);
                                                                                                finish();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        }else{
                                                            loadingProgressBar.setVisibility(View.GONE);
                                                            alertUpdateEmailDialogBox.dismiss();
                                                            Snackbar.make(findViewById(android.R.id.content),"Your password is incorrect. "+reauthenticateTask.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });
                                dialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(BunkSquadUserProfileActivity.this);
                builder.setTitle("Update Email").setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
    }
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private void checkRunningProcessIfPressedBack(){
        if(loadingProgressBarMain.getVisibility()==View.VISIBLE||loadingProgressBar.getVisibility()==View.VISIBLE){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            dialog.dismiss();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            dialog.dismiss();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(BunkSquadUserProfileActivity.this);
            builder.setTitle("Unsaved changes").setMessage("Are you sure that you have to cancel?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }else{
            finish();
        }
    }
}
