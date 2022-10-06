package com.example.chat_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app.model.UserModel;
import com.example.chat_app.databinding.ItemContainerRecentConversionBinding;
import com.example.chat_app.model.ChatMessage;
import com.example.chat_app.listner.ConversionListener;


import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationView> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    private final Context context;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener,Context context) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.context = context;
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

    class ConversationView extends RecyclerView.ViewHolder {

        ItemContainerRecentConversionBinding binding;

        ConversationView(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;

        }

        void setData(ChatMessage chatMessage) {
            Glide.with(context).load(chatMessage.conversionImage).into(binding.imageProfile);
//            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                UserModel user = new UserModel();
                user.id = chatMessage.conversionId;
                user.image = chatMessage.conversionImage;
                user.name = chatMessage.conversionName;
                conversionListener.onConversionListener(user);
            });
        }
    }

  /*  private Bitmap getConversationImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }*/
}
