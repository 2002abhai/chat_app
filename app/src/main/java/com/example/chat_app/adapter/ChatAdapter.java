package com.example.chat_app.adapter;

import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private  String receiverProfileImage;
    private final String senderId;
    static String imageurl;
    StorageReference storageReference;
    private static final int  MEGABYTE = 1024 * 1024;

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

    class SentMessageViewHolder extends RecyclerView.ViewHolder {


        private final ItemConteinerSentMessageBinding binding;

        SentMessageViewHolder(ItemConteinerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.type != null) {
                if (chatMessage.type.equals("text")) {
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.textDateTime.setVisibility(View.VISIBLE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    binding.textDateTime.setText(chatMessage.dateTime);
                } else if (chatMessage.type.equals("pdf")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.VISIBLE);
                    binding.pdfDownload.setVisibility(View.VISIBLE);
                    binding.documentImages.setVisibility(View.VISIBLE);
                    binding.documentTextDateTime.setVisibility(View.VISIBLE);
                    binding.documentTextDateTime.setText(chatMessage.dateTime);
                } else if (chatMessage.type.equals("word")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.VISIBLE);
                    binding.wordSend.setVisibility(View.VISIBLE);
                    binding.wordImages.setVisibility(View.VISIBLE);
                    binding.wordTextDateTime.setVisibility(View.VISIBLE);
                    binding.wordTextDateTime.setText(chatMessage.dateTime);
                }else {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
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
            binding.pdfDownload.setOnClickListener(view -> download(chatMessage));
            binding.wordDownload.setOnClickListener(view -> download(chatMessage));
        }

        void deleteMsg(ChatMessage chatMessage) {
            FirebaseFirestore storage = FirebaseFirestore.getInstance();
               storage.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.id)
                       .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context,"Message is Deleted",Toast.LENGTH_LONG);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,"Message not Deleted",Toast.LENGTH_LONG);
                }
            });
        }

        @SuppressLint("ResourceType")
        private void showImagePicDialog(ChatMessage image) {
            Dialog builder = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            builder.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            builder.setContentView(R.layout.image_show);
            ZoomageView fullImage = builder.findViewById(R.id.imageFullView);
            Glide.with(context).load(image.message).into(fullImage);

            ImageView button = builder.findViewById(R.id.fullImageBack);
            builder.setCancelable(true);
            builder.show();

            button.setOnClickListener(v -> builder.dismiss());
        }

        public void download(ChatMessage message){
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.message);
            String FileName = storageReference.getName();

            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.message);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String url = uri.toString();
                    downloadFile(url,FileName);
                    /*if(message.type.equals("pdf")){
                        downloadFile(url,FileName);
                    }else if(message.type.equals("word")){
                        downloadFile(url,FileName);
                    }*/
              Log.e("url",url);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "File Not Download", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void downloadFile(String url, String fileName) {
            DownloadManager  manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI) ;
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
             manager.enqueue(request);

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
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordImageProfile.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    binding.textDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.imageProfile);
                    }
                } else if(chatMessage.type.equals("pdf")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageProfile.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.imageProfileImage.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordImageProfile.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
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
                }else if(chatMessage.type.equals("word")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageProfile.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.imageProfileImage.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.documentImageProfile.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.VISIBLE);
                    binding.wordImages.setVisibility(View.VISIBLE);
                    binding.wordDownload.setVisibility(View.VISIBLE);
                    binding.wordImageProfile.setVisibility(View.VISIBLE);
                    binding.wordTextDateTime.setVisibility(View.VISIBLE);
                    binding.wordTextDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.wordImageProfile);
                    }
                }
                else {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageProfile.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.documentImageProfile.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordImageProfile.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
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
            binding.wordDownload.setOnClickListener(view -> download(chatMessage));
            binding.pdfDownload.setOnClickListener(view -> download(chatMessage));

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

        public void download(ChatMessage message){
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.message);
            String FileName = storageReference.getName();

            storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.message);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String url = uri.toString();
                    downloadFile(url,FileName);
                    /*if(message.type.equals("pdf")){
                        downloadFile(url,FileName);
                    }else if(message.type.equals("word")){
                        downloadFile(url,FileName);
                    }*/
                    Log.e("url",url);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "File Not Download", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void downloadFile(String url, String fileName) {
            DownloadManager  manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI) ;
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
            manager.enqueue(request);

        }
    }

}
