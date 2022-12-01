package com.example.chat_app.adapter;

import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;

import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private  String receiverProfileImage;
    private final String senderId;
    static String imageUrl;
    MediaPlayer mediaPlayer = new MediaPlayer();
    boolean wasPlaying = false;
    Handler handler;

    @SuppressLint("StaticFieldLeak")
    private static Context context;


    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(String  image){
        receiverProfileImage = image;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId, Context context, String imageUrl ) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        ChatAdapter.imageUrl = imageUrl;
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

        @SuppressLint("ClickableViewAccessibility")
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
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    binding.textDateTime.setText(chatMessage.dateTime);
                }
                else if (chatMessage.type.equals("pdf")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.VISIBLE);
                    binding.pdfDownload.setVisibility(View.VISIBLE);
                    binding.documentImages.setVisibility(View.VISIBLE);
                    binding.documentTextDateTime.setVisibility(View.VISIBLE);
                    binding.documentTextDateTime.setText(chatMessage.dateTime);
                }
                else if (chatMessage.type.equals("word")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
                    binding.documentSend.setVisibility(View.GONE);
                    binding.pdfDownload.setVisibility(View.GONE);
                    binding.documentImages.setVisibility(View.GONE);
                    binding.documentTextDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.GONE);
                    binding.images.setVisibility(View.GONE);
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.VISIBLE);
                    binding.wordSend.setVisibility(View.VISIBLE);
                    binding.wordImages.setVisibility(View.VISIBLE);
                    binding.wordTextDateTime.setVisibility(View.VISIBLE);
                    binding.wordTextDateTime.setText(chatMessage.dateTime);
                }
                else if (chatMessage.type.equals("audio")) {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.textDateTime.setVisibility(View.GONE);
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
                    binding.playAndPushLayout.setVisibility(View.VISIBLE);
                    binding.audioDateTime.setVisibility(View.VISIBLE);
                    binding.audioDateTime.setText(chatMessage.dateTime);
                }
                else {
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
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.imageDateTime.setVisibility(View.VISIBLE);
                    binding.images.setVisibility(View.VISIBLE);
                    Glide.with(context).load(chatMessage.message).into(binding.images);
                    binding.imageDateTime.setText(chatMessage.dateTime);
                }
            }
            binding.images.setOnClickListener(view -> showImagePicDialog(chatMessage));
            binding.textMessage.setOnLongClickListener(view -> {deleteMsg(chatMessage);return true;});
            binding.pdfDownload.setOnClickListener(view -> download(chatMessage));
            binding.wordDownload.setOnClickListener(view -> download(chatMessage));
            binding.playButton.setOnClickListener(view -> playSound());

            binding.seekbar.setOnTouchListener((view, motionEvent) -> {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                binding.playerCurrentTimeText.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            });

            mediaPlayer.setOnBufferingUpdateListener((mp1, i) -> binding.seekbar.setSecondaryProgress(i));

        }

        public void playSound(){
            try{
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler = new Handler();
                    handler.removeCallbacks(updater);
                    mediaPlayer.stop();
                    wasPlaying = true;
                    binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                }
                if(!wasPlaying) {
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }
                    mediaPlayer = MediaPlayer.create(context, Uri.parse(chatMessages.get(getAbsoluteAdapterPosition()).message));
                    mediaPlayer.start();
                    binding.playerTotalTimeText.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                    binding.playButton.setImageResource(android.R.drawable.ic_media_pause);
                    updateSeekBar();

                    mediaPlayer.setOnCompletionListener(mp2 -> {
                        binding.seekbar.setProgress(0);
                        binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                        binding.playerCurrentTimeText.setText(R.string.timeZero);
                        binding.playerTotalTimeText.setText(R.string.timeZero);
                        mp2.stop();
                    });
                }
                wasPlaying = false;

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private Runnable updater = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                long currentDuration = mediaPlayer.getCurrentPosition();
                binding.playerCurrentTimeText.setText(milliSecondsToTimer(currentDuration));
            }
        };

        private void updateSeekBar() {
            if (mediaPlayer.isPlaying()) {
                handler = new Handler();
                binding.seekbar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
                handler.postDelayed(updater, 1000);
            }
        }

        private String milliSecondsToTimer(long milliSeconds) {

            String timerString = "";
            String secondsString;

            int hours = (int) (milliSeconds / (1000 * 60 * 60));
            int minutes = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

            if (hours > 0) {
                timerString = hours + ":";
            }
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            timerString = timerString + minutes + ":" + secondsString;
            return timerString;
        }

        void deleteMsg(ChatMessage chatMessage) {
            FirebaseFirestore storage = FirebaseFirestore.getInstance();
          storage.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo("id",chatMessage.id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String idDelete = document.getId();
                        storage.collection(Constants.KEY_COLLECTION_CHAT).document(idDelete).delete().addOnSuccessListener(unused -> {
                            context.getApplicationContext();
                            chatMessages.remove(getAbsoluteAdapterPosition());
                            notifyItemRemoved(getAbsoluteAdapterPosition());
                            Toast.makeText(context,"Message is Deleted",Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(e -> Toast.makeText(context,"Message not Deleted",Toast.LENGTH_LONG).show());

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
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
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                downloadFile(url,FileName);
          Log.e("url",url);
            }).addOnFailureListener(e -> Toast.makeText(context, "File Not Download", Toast.LENGTH_SHORT).show());
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

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerRecivedMessageBinding binding;

        ReceivedMessageViewHolder(@NonNull ItemContainerRecivedMessageBinding itemContainerReceiveMessageBinding){
            super(itemContainerReceiveMessageBinding.getRoot());
            binding = itemContainerReceiveMessageBinding;
        }

        @SuppressLint("ClickableViewAccessibility")
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
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.audioImageProfile.setVisibility(View.GONE);
                    binding.textMessage.setText(chatMessage.message);
                    binding.textDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.imageProfile);
                    }
                }
                else if(chatMessage.type.equals("pdf")) {
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
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.audioImageProfile.setVisibility(View.GONE);
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
                }
                else if(chatMessage.type.equals("word")) {
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
                    binding.audioImageProfile.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.playAndPushLayout.setVisibility(View.GONE);
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
                else if(chatMessage.type.equals("audio")) {
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
                    binding.wordSend.setVisibility(View.GONE);
                    binding.wordImages.setVisibility(View.GONE);
                    binding.wordDownload.setVisibility(View.GONE);
                    binding.wordImageProfile.setVisibility(View.GONE);
                    binding.wordTextDateTime.setVisibility(View.GONE);
                    binding.audioImageProfile.setVisibility(View.VISIBLE);
                    binding.playAndPushLayout.setVisibility(View.VISIBLE);
                    binding.audioDateTime.setVisibility(View.VISIBLE);
                    binding.audioDateTime.setText(chatMessage.dateTime);
                    if(receiverProfileImage != null){
                        Glide.with(context).load(receiverProfileImage).into(binding.audioImageProfile);
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
                    binding.playAndPushLayout.setVisibility(View.GONE);
                    binding.audioDateTime.setVisibility(View.GONE);
                    binding.audioImageProfile.setVisibility(View.GONE);
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
            binding.playButton.setOnClickListener(view -> playSound());

            binding.seekbar.setOnTouchListener((view, motionEvent) -> {
                SeekBar seekBar = (SeekBar) view;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                binding.playerCurrentTimeText.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            });

            mediaPlayer.setOnBufferingUpdateListener((mp1, i) -> binding.seekbar.setSecondaryProgress(i));

        }

        public void playSound(){
            try{
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler = new Handler();
                    handler.removeCallbacks(updater);
                    mediaPlayer.stop();
                    wasPlaying = true;
                    binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                }
                if(!wasPlaying) {
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }
                    mediaPlayer = MediaPlayer.create(context, Uri.parse(chatMessages.get(getAbsoluteAdapterPosition()).message));
                    mediaPlayer.start();
                    binding.playerTotalTimeText.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                    binding.playButton.setImageResource(android.R.drawable.ic_media_pause);
                    updateSeekBar();
                    mediaPlayer.setOnCompletionListener(mp2 -> {
                        binding.seekbar.setProgress(0);
                        binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                        binding.playerCurrentTimeText.setText(R.string.timeZero);
                        binding.playerTotalTimeText.setText(R.string.timeZero);
                        mp2.stop();
                    });
                }
                wasPlaying = false;

            }catch (Exception e){
                e.printStackTrace();
            }

         /*  else{
                assert mediaPlayer != null;
                mediaPlayer.start();
                binding.playButton.setImageResource(android.R.drawable.ic_media_pause);
                binding.playerTotalTimeText.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                updateSeekBar();
            }
            mediaPlayer = MediaPlayer.create(context, Uri.parse(chatMessages.get(getAbsoluteAdapterPosition()).message));
            mediaPlayer.start();
            binding.playerTotalTimeText.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
            updateSeekBar();
            if(milliSecondsToTimer(mediaPlayer.getDuration()) == milliSecondsToTimer(mediaPlayer.getCurrentPosition())){
                binding.seekbar.setProgress(0);
                binding.playButton.setImageResource(android.R.drawable.ic_media_play);
                binding.playerCurrentTimeText.setText(R.string.timeZero);
                binding.playerTotalTimeText.setText(R.string.timeZero);
                assert mediaPlayer != null;
                mediaPlayer.stop();
            }*/
        }

        private Runnable updater = new Runnable() {
            @Override
            public void run() {
                updateSeekBar();
                long currentDuration = mediaPlayer.getCurrentPosition();
                binding.playerCurrentTimeText.setText(milliSecondsToTimer(currentDuration));
            }
        };

        private void updateSeekBar() {
            if (mediaPlayer.isPlaying()) {
                handler = new Handler();
                binding.seekbar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
                handler.postDelayed(updater, 1000);
            }
        }

        private String milliSecondsToTimer(long milliSeconds) {

            String timerString = "";
            String secondsString;

            int hours = (int) (milliSeconds / (1000 * 60 * 60));
            int minutes = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

            if (hours > 0) {
                timerString = hours + ":";
            }
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            timerString = timerString + minutes + ":" + secondsString;
            return timerString;
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
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                downloadFile(url,FileName);
                Log.e("url",url);
            }).addOnFailureListener(e -> Toast.makeText(context, "File Not Download", Toast.LENGTH_SHORT).show());
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
