package com.socialapp.antariksh.bunksquad;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationPublisher extends BroadcastReceiver {

    private static final String CHANNEL_ID ="attendanceManager198" ;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref=context.getSharedPreferences("dailyRemainderFile",context.MODE_PRIVATE);
        Calendar tempcal=Calendar.getInstance();
        switch (tempcal.get(Calendar.DAY_OF_WEEK)){
            case 1:
                if(sharedPref.getBoolean("SUNDAY", Boolean.parseBoolean(context.getString(R.string.SUNDAY)))==false)
                    return;
                break;
            case 2:
                if(sharedPref.getBoolean("MONDAY", Boolean.parseBoolean(context.getString(R.string.MONDAY)))==false)
                    return;
                break;
            case 3:
                if(sharedPref.getBoolean("TUESDAY", Boolean.parseBoolean(context.getString(R.string.TUESDAY)))==false)
                    return;
                break;
            case 4:
                if(sharedPref.getBoolean("WEDNESDAY", Boolean.parseBoolean(context.getString(R.string.WEDNESDAY)))==false)
                    return;
                break;
            case 5:
                if(sharedPref.getBoolean("THURSDAY", Boolean.parseBoolean(context.getString(R.string.THURSDAY)))==false)
                    return;
                break;
            case 6:
                if(sharedPref.getBoolean("FRIDAY", Boolean.parseBoolean(context.getString(R.string.FRIDAY)))==false)
                    return;
                break;
            case 7:
                if(sharedPref.getBoolean("SATURDAY", Boolean.parseBoolean(context.getString(R.string.SATURDAY)))==false)
                    return;
                break;
        }
        Intent notificationIntent = new Intent(context, BunksquadMainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra("FRAGMENT","AM");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bunksquad_logo)
                .setContentTitle("Mark your Attendance now")
                .setContentText("Click here to mark today's attendance.")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager=NotificationManagerCompat.from(context);
        notificationManager.notify(100,builder.build());
    }
}


