package com.socialapp.antariksh.bunksquad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
