package com.example.chat_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.example.chat_app.activities.ChatActivity;
import com.example.chat_app.activities.IncomingActivity;
import com.example.chat_app.firebase.vedioMessangingService;
import com.example.chat_app.model.UserModel;
import com.example.chat_app.uitilies.Constants;
import com.example.chat_app.uitilies.Preferencemanager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = vedioMessangingService.class.getSimpleName();
    private Preferencemanager preferencemanager;
    int notificationId = 101;
    String channelId = "Chat_Message";
    CharSequence channelName = "Chat App";
    String channelDescription = "This notification channel is used for chat message notifications";


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCMTOKEN::", "toke - > " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationType = remoteMessage.getData().get("notificationType");

        if(notificationType.equalsIgnoreCase("Video")){
            String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);

            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                    Intent intent = new Intent(getApplicationContext(), IncomingActivity.class);

                    intent.putExtra(Constants.REMOTE_MSG_MEETING_TYPE, remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE));
                    intent.putExtra(Constants.KEY_USER, remoteMessage.getData().get(Constants.KEY_USER));
                    intent.putExtra(Constants.KEY_image,remoteMessage.getData().get(Constants.KEY_image));
                    intent.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN, remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN));
                    intent.putExtra(Constants.REMOTE_MSG_MEETING_ROOM, remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
                else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                    Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                    intent.putExtra(
                            Constants.REMOTE_MSG_INVITATION_RESPONSE,
                            remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                    );
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

            }
        }
        else if(notificationType.equalsIgnoreCase("image")){
             if(remoteMessage.getData().size() > 0){
                 sendImageNotification(
                         remoteMessage.getData().get("body"),
                         remoteMessage.getData().get("title"),
                         remoteMessage.getData().get("SenderName"),
                         remoteMessage.getData().get("SenderID"),
                         remoteMessage.getData().get("SenderImage"),
                         remoteMessage.getData().get("reciverID"),
                         remoteMessage.getData().get("reciverName"),
                         remoteMessage.getData().get("reciverImage"),
                         remoteMessage.getData().get("conveId")
                 );
             }
        }
        else {
            if (remoteMessage.getData().size() > 0) {
                sendNotification(
                        remoteMessage.getData().get("body"),
                        remoteMessage.getData().get("title"),
                        remoteMessage.getData().get("SenderName"),
                        remoteMessage.getData().get("SenderID"),
                        remoteMessage.getData().get("SenderImage"),
                        remoteMessage.getData().get("reciverID"),
                        remoteMessage.getData().get("reciverName"),
                        remoteMessage.getData().get("reciverImage"),
                        remoteMessage.getData().get("conveId")
                );
            }
        }

    }

    public void sendNotification(String messageBody, String title, String userName, String userId, String image, String SenderId, String SenderName, String senderImage, String conveId) {

        UserModel user = new UserModel();
        user.setId(userId);
        user.setImage(image);
        user.setName(userName);

        Intent i = new Intent(this, ChatActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("receiveFromName", user.getName());
        i.putExtra("receiveFromId", user.getId());
        i.putExtra("receiveFromImage", user.getImage());
        i.putExtra("senderId", SenderId);
        i.putExtra("senderName", SenderName);
        i.putExtra("senderImage", senderImage);
        i.putExtra("converId",conveId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), i, PendingIntent.FLAG_MUTABLE);

        RemoteInput remoteInput = new RemoteInput.Builder("key1").setLabel("Please enter message").build();
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_notifications,
                "Reply Now...", contentIntent)
                .addRemoteInput(remoteInput)
                .build();




        long[] pattern = {500, 500, 500, 500, 500};
        Notification notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setVibrate(pattern)
                .addAction(action)
                .setContentIntent(contentIntent)
                .build();

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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder);
    }

    public void sendImageNotification(String messageBody, String title, String userName, String userId, String image, String SenderId, String SenderName, String senderImage, String conveId) {

        Bitmap bitmap = null;

        UserModel user = new UserModel();
        user.setId(userId);
        user.setImage(image);
        user.setName(userName);

        Intent i = new Intent(this, ChatActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("receiveFromName", user.getName());
        i.putExtra("receiveFromId", user.getId());
        i.putExtra("receiveFromImage", user.getImage());
        i.putExtra("senderId", SenderId);
        i.putExtra("senderName", SenderName);
        i.putExtra("senderImage", senderImage);
        i.putExtra("converId",conveId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), i, PendingIntent.FLAG_MUTABLE);

        RemoteInput remoteInput = new RemoteInput.Builder("key1").setLabel("Please enter message").build();
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_notifications,
                "Reply Now...", contentIntent)
                .addRemoteInput(remoteInput)
                .build();

        FutureTarget futureTarget = Glide.with(this)
                .asBitmap()
                .load(messageBody)
                .submit();
        try {
            bitmap = (Bitmap) futureTarget.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        long[] pattern = {500, 500, 500, 500, 500};
        Notification notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setVibrate(pattern)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                .addAction(action)
                .setContentIntent(contentIntent)
                .build();

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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder);
    }

}
