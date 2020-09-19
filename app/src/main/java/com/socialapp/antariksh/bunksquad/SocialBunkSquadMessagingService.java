package com.socialapp.antariksh.bunksquad;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SocialBunkSquadMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("abhishek", "onMessageReceived: "+remoteMessage.getData());
        showNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
    }

    private void showNotification(String title,String description){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"BunkSquadSocial1432")
                .setSmallIcon(R.drawable.test_insert_emoticon_black_24dp)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(description);
        NotificationManagerCompat manager =NotificationManagerCompat.from(this);
        manager.notify(234564,builder.build());
    }
}
