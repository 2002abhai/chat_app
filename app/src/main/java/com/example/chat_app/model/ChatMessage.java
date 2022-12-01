package com.example.chat_app.model;

import java.util.Date;

public class ChatMessage {

    public String senderId,receiverId,message,dateTime,id,type;
    public Date dateObject;
    public String conversionId,conversionName,conversionImage;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
