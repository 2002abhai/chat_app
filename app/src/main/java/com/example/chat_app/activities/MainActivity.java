package com.example.chat_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.chat_app.model.ChatMessage;
import com.example.chat_app.uitilies.Constants;
import com.example.chat_app.listner.ConversionListener;
import com.example.chat_app.uitilies.Preferencemanager;
import com.example.chat_app.adapter.RecentConversationsAdapter;
import com.example.chat_app.model.UserModel;
import com.example.chat_app.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private Preferencemanager preferencemanager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter recentConversationsAdapter;
    private FirebaseFirestore database;
    private String encodeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencemanager = new Preferencemanager(getApplicationContext());
        init();
        loadUserDetail();
        getToken();
        setListeners();
        listenConversions();

        Intent intent = getIntent();
        if(intent != null){
            String action = intent.getAction();
            String type = intent.getType();
            if(Intent.ACTION_SEND.equals(action) && type!=null){
                if(type.equalsIgnoreCase("text/plain")){
                    handleTextData(intent);
                }else if(type.startsWith("image/")){
                    handleImageData(intent);
                }else if(type.startsWith("audio/")){
                    handleAudioData(intent);
                }else if(type.startsWith("video/")){
                    handleVideoData(intent);
                }

            }else if(Intent.ACTION_SEND_MULTIPLE.equals(action)&& type!= null){
                if(type.startsWith("image/")){
                    handleMultipleImage(intent);
                }

            }
        }

    }

    private void handleVideoData(Intent intent) {
        Uri video =intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(video != null){
            Log.d("audio file path ",video.getPath());
        }
    }

    private void handleAudioData(Intent intent) {
        Uri audio =intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(audio != null){
            Log.d("audio file path ",audio.getPath());
        }
    }

    private void handleImageData(Intent intent) {
        Uri image =intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(image != null){
            Log.d("image file path ",image.getPath());
        }
    }

    private void handleTextData(Intent intent) {
        String textData =intent.getStringExtra(Intent.EXTRA_TEXT);
        if(textData != null){
            Log.d("Text data",textData);
        }
    }

    private void handleMultipleImage(Intent intent) {
        ArrayList<Uri> imageList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if(imageList != null){
            for(Uri uri :imageList){
                Log.d("image file path ",uri.getPath());
            }
        }
    }

    private void init(){
        conversations = new ArrayList<>();
        recentConversationsAdapter =  new RecentConversationsAdapter(conversations,this,this);
        binding.conversationsRecycleView.setAdapter(recentConversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.febNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UserActivity.class)));

        binding.imageProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void loadUserDetail() {
        binding.textName.setText(preferencemanager.getString(Constants.KEY_NAME));
          String image = preferencemanager.getString(Constants.KEY_image);
        Glide.with(this).load(image).into(binding.imageProfile);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversions(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferencemanager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferencemanager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value !=null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderID;
                    chatMessage.receiverId = receiverId;
                    if(preferencemanager.getString(Constants.KEY_USER_ID).equals(senderID)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i =0; i<conversations.size();i++){
                        String senderID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderID)&&conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }

                }
            }
            Collections.sort(conversations,(obj1,obj2)->obj2.dateObject.compareTo(obj1.dateObject));
            recentConversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecycleView.smoothScrollToPosition(0);
            binding.conversationsRecycleView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Glide.with(this).load(imageUri).into(binding.imageProfile);
                           /* InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            encodeImage = encodeImage(bitmap);*/
                        } catch (Error e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferencemanager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(
                preferencemanager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut() {
        showToast("Signing Out.....");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(
                preferencemanager.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
                    preferencemanager.clear();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    @Override
    public void onConversionListener(UserModel user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}