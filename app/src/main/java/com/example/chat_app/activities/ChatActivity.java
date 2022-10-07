package com.example.chat_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.RemoteInput;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.chat_app.databinding.ActivityChatBinding;
import com.example.chat_app.network.ApiClient;
import com.example.chat_app.network.ApiService;
import com.example.chat_app.adapter.ChatAdapter;
import com.example.chat_app.model.ChatMessage;
import com.example.chat_app.uitilies.Constants;
import com.example.chat_app.uitilies.Preferencemanager;
import com.example.chat_app.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private UserModel receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private Preferencemanager preferencemanager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri imageUri;
    Uri documentUri;
    UploadTask uploadImage;
    String downloadUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Bundle remoteReply = RemoteInput.getResultsFromIntent(getIntent());

        if (remoteReply != null) {
            String message = remoteReply.getCharSequence("TEXT_REPLY").toString();
            Log.e(message, "message" + message);

        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(101);

        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }


    private void init() {
        preferencemanager = new Preferencemanager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.image,
                preferencemanager.getString(Constants.KEY_USER_ID),
                this,
                downloadUri
        );
        Glide.with(this).load(receiverUser.image).into(binding.chatImageProfile);
        binding.chatRecycleView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSendDocument.setOnClickListener(view ->showImagePicDialog());
        binding.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receiverUser.token == null || receiverUser.token.trim().isEmpty()) {
                    showToast(receiverUser + "is not available to video call");
                } else {
                    Intent intent = new Intent(getApplicationContext(), OutGoingActivity.class);
                    intent.putExtra("user", receiverUser);
                    intent.putExtra("type", "audio");
                    startActivity(intent);
                }
            }
        });
        binding.vedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receiverUser.token == null || receiverUser.token.trim().isEmpty()) {
                    showToast(receiverUser + "is not available to video call");
                } else {
                    Intent intent = new Intent(getApplicationContext(), OutGoingActivity.class);
                    intent.putExtra("user", receiverUser);
                    intent.putExtra("type", "video");
                    startActivity(intent);
                }
            }
        });
        binding.layoutSend.setOnClickListener(v -> {
            if (binding.inputMessage.getText().toString().isEmpty() || binding.inputMessage.getText() == null) {
                showToast("Please Enter Any Message");
            } else {
                sendMessage();
            }
        });
    }

    private void showImagePicDialog() {
        String options[] = {"Image", "Document"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pickImage.launch(intent);
//                  sendImage();
                } else if (which == 1) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("*/*");
                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    pickDocument.launch(chooseFile);
                }
            }
        });
        builder.create().show();
    }

    private void sendMessage() {
        DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
        String id = ref.getId();
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_CHAT_ID, id);
        message.put(Constants.MESSAGE_TYPE, "text");
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
        if (conversationId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferencemanager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferencemanager.getString(Constants.KEY_image));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONObject data = new JSONObject();
                data.put("title", preferencemanager.getString(Constants.KEY_NAME));
                data.put("body", binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put("notification", data);
                body.put("to", receiverUser.token);

                sendNotification(body.toString());

            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);

    }

    private void sendImage() {
        StorageReference ref = storageReference.child("images/" + imageUri.getLastPathSegment());
        uploadImage = ref.putFile(imageUri);
        uploadImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
                    String id = ref.getId();
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_CHAT_ID, id);
                    message.put(Constants.MESSAGE_TYPE, "image");
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_MESSAGE, downloadUri);
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
                }


            }
        });

    }

    private void sendDocument() {
        StorageReference ref = storageReference.child("document/" + documentUri.getLastPathSegment());
        uploadImage = ref.putFile(documentUri);
        uploadImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
                    String id = ref.getId();
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_CHAT_ID, id);
                    message.put(Constants.MESSAGE_TYPE, "document");
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_MESSAGE, downloadUri);
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
                }


            }
        });



    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        imageUri = result.getData().getData();
                        sendImage();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickDocument = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        documentUri = result.getData().getData();
                        sendDocument();
                    }
                }
            }
    );

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
   /* void deleteData(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        sentMessageBinding.textMessage.setOnClickListener(view -> {
            binding.imagedelete.setVisibility(View.VISIBLE);
            binding.vedio.setVisibility(View.GONE);
            database.collection(Constants.KEY_COLLECTION_CHAT).document("id").delete();
//            database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(chatMessage.id).delete();
            chatAdapter.notify();
        });
    }*/

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(Constants.getRemoteMsgHeaders(), messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully ");
                } else {
                    showToast("Error :" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });

    }

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USER).document(receiverUser.id)
                .addSnapshotListener(
                        ChatActivity.this, (value, error) -> {
                            if (error != null) {
                                return;
                            }
                            if (value != null) {
                                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                                    isReceiverAvailable = availability == 1;
                                }
                                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                                if (receiverUser.image == null) {
                                    receiverUser.image = value.getString(Constants.KEY_image);
                                    chatAdapter.setReceiverProfileImage(receiverUser.image);
                                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                                }
                            }
                            if (isReceiverAvailable) {
                                binding.textAvailability.setVisibility(View.VISIBLE);
                            } else {
                                binding.textAvailability.setVisibility(View.GONE);
                            }

                        });
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencemanager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.id = documentChange.getDocument().getString("id");
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.type = documentChange.getDocument().getString(Constants.MESSAGE_TYPE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkForConversion();
        }
    });


    private void loadReceiverDetails() {
        receiverUser = (UserModel) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }


    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd,yyyy - hh.mm a", Locale.getDefault()).format(date);
    }


    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkConversationRemotely(
                    preferencemanager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkConversationRemotely(
                    receiverUser.id,
                    preferencemanager.getString(Constants.KEY_USER_ID)
            );

        }
    }

    private void addConversion(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkConversationRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

}