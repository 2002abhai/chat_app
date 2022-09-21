package com.example.chat_app;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessangingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token){
    super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

    }
}
