package com.example.chat_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app.model.ChatMessage;
import com.example.chat_app.databinding.ItemContainerRecivedMessageBinding;
import com.example.chat_app.databinding.ItemConteinerSentMessageBinding;
import com.example.chat_app.uitilies.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private  String receiverProfileImage;
    private final String senderId;
    private static Context context;


    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_DELETE = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(String  image){
        receiverProfileImage = image;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId,Context context) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemConteinerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new ReceivedMessageViewHolder(
                    ItemContainerRecivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position),receiverProfileImage);
        }

        if(getItemViewType(position) == VIEW_TYPE_DELETE){
            ((SentMessageViewHolder) holder).deleteData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position){
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{


        private final ItemConteinerSentMessageBinding binding;

        SentMessageViewHolder( ItemConteinerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;

        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
        void deleteData(ChatMessage chatMessage){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            binding.textMessage.setOnClickListener(view -> {
                database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.id).delete();
                database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(chatMessage.id).delete();
            });
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerRecivedMessageBinding binding;

        ReceivedMessageViewHolder( ItemContainerRecivedMessageBinding itemContainerReceiveMessageBinding){
            super(itemContainerReceiveMessageBinding.getRoot());
            binding = itemContainerReceiveMessageBinding;
        }

        void setData(ChatMessage chatMessage, String receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if(receiverProfileImage != null){
                Glide.with(context).load(receiverProfileImage).into(binding.imageProfile);
//                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
