package com.example.chat_app.firebase;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.example.chat_app.uitilies.Constants;
import com.example.chat_app.activities.ChatActivity;
import com.example.chat_app.model.UserModel;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class    MessangingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_REPLY = "NotificationReply";

    public static final String KEY_INTENT_MORE = "keyintentmore";
    public static final String KEY_INTENT_HELP = "keyintenthelp";

    public static final int REQUEST_CODE_MORE = 100;
    public static final int REQUEST_CODE_HELP = 101;
//    public static final int NOTIFICATION_ID = 200;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }


   /* @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        UserModel user = new UserModel();
        Intent intent1 = new Intent("user");
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);
        broadcaster.sendBroadcast(intent1);

        int notificationId = new Random().nextInt();
        String channelId = "Chat_Message";


        Intent intent = new Intent(this,ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER,user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        *//*Intent replyIntent = new Intent(this, ChatActivity.class);
        replyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingReplyIntent = PendingIntent.getActivity(this, 1, replyIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_outline_send_24, "Click to reply", pendingReplyIntent).addRemoteInput(remoteInput).build();*//*


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notifications);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Constants.KEY_MESSAGE)));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);


       *//* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            RemoteInput remoteInput = new RemoteInput.Builder("TEXT_REPLY").setLabel("Enter your text").build();
            Intent replyIntent = new Intent(this, ChatActivity.class);
            replyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingReplyIntent = PendingIntent.getActivity(this, 1, replyIntent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_outline_send_24, "Click to reply", pendingReplyIntent).addRemoteInput(remoteInput).build();
            builder.addAction(action);
        }*//*

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            CharSequence channelName = "Chat App";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId,channelName,importance);
            channel.setDescription(channelDescription);
            channel.enableVibration(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }*/


    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        UserModel user = new UserModel();
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "Chat_Message";
        CharSequence channelName = "Chat App";
        String channelDescription = "This notification channel is used for chat message notifications";




        //Pending intent for a notification button named More
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent morePendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_MORE,
                new Intent(this, ChatActivity.class)
                        .putExtra(KEY_INTENT_MORE, REQUEST_CODE_MORE),
                PendingIntent.FLAG_IMMUTABLE
        );

        //Pending intent for a notification button help
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent helpPendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_HELP,
                new Intent(this, ChatActivity.class)
                        .putExtra(KEY_INTENT_HELP, REQUEST_CODE_HELP),
                PendingIntent.FLAG_IMMUTABLE
        );


        //We need this object for getting direct input from notification
        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY)
                .setLabel("Please enter your name")
                .build();


        //For the remote input we need this action object
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_dialog_email,
                        "Reply Now...", helpPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        //Creating the notifiction builder object
        @SuppressLint("LaunchActivityFromNotification") NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(user.name)
                .setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE))
               .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get(Constants.KEY_MESSAGE)))
                .setAutoCancel(true)
                .setContentIntent(helpPendingIntent)
                .addAction(action)
                .addAction(android.R.drawable.ic_menu_compass, "More", morePendingIntent)
                .addAction(android.R.drawable.ic_menu_directions, "Help", helpPendingIntent);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mChannel.setDescription(channelDescription);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        //finally displaying the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
    }
}
