package com.example.chat_app.adapter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app.R;
import com.example.chat_app.model.ChatMessage;
import com.example.chat_app.databinding.ItemContainerRecivedMessageBinding;
import com.example.chat_app.databinding.ItemConteinerSentMessageBinding;
import com.example.chat_app.uitilies.Constants;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jsibbold.zoomage.ZoomageView;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private  String receiverProfileImage;
    private final String senderId;
    static String imageurl;

    @SuppressLint("StaticFieldLeak")
    private static Context context;


    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(String  image){
        receiverProfileImage = image;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId, Context context, String imageurl) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        ChatAdapter.imageurl = imageurl;
        ChatAdapter.context = context;
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
                   ));
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

        SentMessageViewHolder(ItemConteinerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            if(chatMessage.type!=null){
                if (chatMessage.type.equals("text")) {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textDateTime.setVisibility(View.VISIBLE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    binding.textDateTime.setText(chatMessage.dateTime);
                } else if(chatMessage.type.equals("document")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.VISIBLE);
                    binding.pdfDownload.setVisibility(View.VISIBLE);
                    binding.documentImages.setVisibility(View.VISIBLE);
                    binding.documentTextDateTime.setVisibility(View.VISIBLE);
                    Glide.with(context).load(chatMessage.message).into(binding.images);
                    binding.imageDateTime.setText(chatMessage.dateTime);
                }else {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.VISIBLE);
                    binding.images.setVisibility(View.VISIBLE);
                    Glide.with(context).load(chatMessage.message).into(binding.images);
                    binding.imageDateTime.setText(chatMessage.dateTime);
                }
            }

        binding.images.setOnClickListener(view -> showImagePicDialog(chatMessage));
            binding.textMessage.setOnLongClickListener(view -> {
                deleteMsg(chatMessage);
                return true;
            });
        }

        void deleteMsg(ChatMessage chatMessage){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
           database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.id).delete()
                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Log.d(TAG, "Message deleted!");
                       }
                   })
                   .addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Log.w(TAG, "Error deleting Message", e);
                       }
                   });
            /*query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                        if(dataSnapshot1.child(Constants.KEY_USER_ID).equals(Constants.KEY_USER_ID)){
                            dataSnapshot1.getRef().removeValue();
                        }else {
                            Toast.makeText(context, "you can delete only your msg....", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/
        }

        @SuppressLint("ResourceType")
        private void showImagePicDialog(ChatMessage image) {
           /* Dialog builder = new Dialog(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            builder.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            builder.setContentView(R.layout.image_show);
            ZoomageView fullImage = builder.findViewById(R.id.imageFullView);
            Glide.with(context).load(image.message).into(fullImage);

            ImageView button = builder.findViewById(R.id.fullImageBack);
            builder.setCancelable(true);
            builder.show();

            button.setOnClickListener(v -> builder.dismiss());*/
            final Dialog dialog=new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.setContentView(R.layout.image_show);
            ZoomageView fullImage = dialog.findViewById(R.id.imageFullView);
            Glide.with(context).load(image.message).into(fullImage);

            ImageView button = dialog.findViewById(R.id.fullImageBack);
            dialog.setCancelable(true);
            dialog.show();

            button.setOnClickListener(v -> dialog.dismiss());
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerRecivedMessageBinding binding;

        ReceivedMessageViewHolder(@NonNull ItemContainerRecivedMessageBinding itemContainerReceiveMessageBinding){
            super(itemContainerReceiveMessageBinding.getRoot());
            binding = itemContainerReceiveMessageBinding;
        }

        void setData(ChatMessage chatMessage, String receiverProfileImage){
            if(chatMessage.type!=null){
                if (chatMessage.type.equals("text")) {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textDateTime.setVisibility(View.VISIBLE);
                    binding.imageProfile.setVisibility(View.VISIBLE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.documentImageProfile.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.imageProfileImage.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    binding.textDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.imageProfile);
                    }
                } else if(chatMessage.type.equals("document")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageProfile.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.imageProfileImage.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.VISIBLE);
                    binding.documentSend.setVisibility(View.VISIBLE);
                    binding.pdfDownload.setVisibility(View.VISIBLE);
                    binding.documentTextDateTime.setVisibility(View.VISIBLE);
                    binding.documentImageProfile.setVisibility(View.VISIBLE);
                    Glide.with(context).load(chatMessage.message).into(binding.images);
                    binding.imageDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.documentImageProfile);
                    }
                }else {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageProfile.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.documentImageProfile.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.VISIBLE);
                    binding.imageProfileImage.setVisibility(View.VISIBLE);
                    binding.images.setVisibility(View.VISIBLE);
                    Glide.with(context).load(chatMessage.message).into(binding.images);
                    binding.imageDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.imageProfileImage);
                    }
                }
            }

            binding.images.setOnClickListener(view -> showImagePicDialog(chatMessage));

        }

        @SuppressLint("ResourceType")
        private void showImagePicDialog(ChatMessage image) {
            Dialog builder = new Dialog(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            builder.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            builder.setContentView(R.layout.image_show);
            ZoomageView fullImage = builder.findViewById(R.id.imageFullView);
            Glide.with(context).load(image.message).into(fullImage);

            ImageView button = builder.findViewById(R.id.fullImageBack);
            builder.setCancelable(true);
            builder.show();

            button.setOnClickListener(v -> builder.dismiss());
        }
    }

}
