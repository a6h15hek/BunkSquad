package com.socialapp.antariksh.bunksquad;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNotification();
        Thread welcomeThread=new Thread(){
            @Override
            public void run(){
                try{
                    super.run();
                    sleep(900);//900
                }catch (Exception e){

                }finally {
                    Intent intent = new Intent(MainActivity.this, BunksquadMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        welcomeThread.start();
        /*Intent intent = new Intent(MainActivity.this, MassBunkSignUpActivity.class);
        startActivity(intent);
        finish();*/
    }

    private void setNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Social BunkSquad";
            String description = "Polls Notification.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("BunkSquadSocial1432", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
