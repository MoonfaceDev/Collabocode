package com.moonface.collabocode;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        super.onMessageReceived(message);
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            String clickAction = message.getNotification().getClickAction();
            String teamId = message.getData().get("team_id");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "team");
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_group_notification);
            builder.setColor(getResources().getColor(R.color.colorAccent));
            builder.setContentTitle(title);
            builder.setContentText(body);

            Intent resultIntent = new Intent(clickAction);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            resultIntent.putExtra("team_id", teamId);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if(notificationManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("team", "Team notifications", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                int id = (int) System.currentTimeMillis();

                notificationManager.notify(id, builder.build());
            }
        }

    }

    @Override
    public void onNewToken(String token) {
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        if (FirebaseAuth.getInstance().getUid() != null) {
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).update("token_id",token);
        }
    }
}
