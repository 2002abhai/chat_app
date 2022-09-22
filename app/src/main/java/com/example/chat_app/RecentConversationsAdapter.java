package com.example.chat_app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.databinding.ItemContainerRecentConversionBinding;


import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationView> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages,ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversationView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationView(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationView holder, int position) {
        holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationView  extends RecyclerView.ViewHolder{

        ItemContainerRecentConversionBinding binding;

        ConversationView(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;

        }

        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                UserModel user = new UserModel();
                 user.id = chatMessage.conversionId;
                 user.image= chatMessage.conversionImage;
                 user.name = chatMessage.conversionName;
                 conversionListener.onConversionListener(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
