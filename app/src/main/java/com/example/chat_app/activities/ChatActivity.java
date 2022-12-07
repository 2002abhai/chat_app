package com.example.chat_app.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.RemoteInput;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
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
    Uri audiouRi;
    UploadTask uploadImage;
    String downloadUri;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;
    public static final int RequestPermissionCode = 1;
    String file_name;
    String replyMessages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(101);
        broadcastReceiver();
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
        binding.reordButton.setRecordView(binding.recorderView);
        binding.reordButton.setListenForRecord(false);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSendDocument.setOnClickListener(view -> showPicDialog());
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
        binding.reordButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, RequestPermissionCode);
            } else {
                binding.reordButton.setListenForRecord(true);
            }
        });
        binding.recorderView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
                MediaRecorderReady();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast(e.toString());
                }
                binding.inputMessage.setVisibility(View.GONE);
                binding.recorderView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                Log.d("RecordView", "onCancel");
                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(AudioSavePathInDevice);
                if (file.exists()) {
                    file.delete();
                    binding.recorderView.setVisibility(View.GONE);
                    binding.inputMessage.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                Log.d("RecordView", "onFinish");
                mediaRecorder.stop();
                mediaRecorder.release();
                binding.recorderView.setVisibility(View.GONE);
                binding.inputMessage.setVisibility(View.VISIBLE);
                mediaScanner(AudioSavePathInDevice);
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(AudioSavePathInDevice);
                if (file.exists()) {
                    file.delete();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    binding.recorderView.setVisibility(View.GONE);
                    binding.inputMessage.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @SuppressLint("SimpleDateFormat")
    public void MediaRecorderReady() {
        if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, READ_EXTERNAL_STORAGE}, RequestPermissionCode);
        } else {
            file_name = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getPath();
//            file_name = (getCacheDir()).getPath();
            File file = new File(file_name);
            String date = String.valueOf(new Date().getTime());
            String fileDateName = dateFormatChange(date);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setAudioEncodingBitRate(16 * 44100);
            mediaRecorder.setAudioSamplingRate(44100);
            if (!file.exists()) {
                file.mkdirs();
            }
            AudioSavePathInDevice = file + "/" + fileDateName + ".mp3";
            Log.d("Audio file path ---", AudioSavePathInDevice);
            mediaRecorder.setOutputFile(AudioSavePathInDevice);
        }
    }

    private void mediaScanner(String file) {
        MediaScannerConnection.scanFile(this,
                new String[]{file}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        sendVoiceMessage(AudioSavePathInDevice);

                    }
                });
    }

    private void showPicDialog() {
        String options[] = {"Image", "Pdf", "word"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pickImage.launch(intent);
                } else if (which == 1) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("application/pdf");;
                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    pickPdf.launch(chooseFile);
                } else if (which == 2) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("*/*");
                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    pickWord.launch(chooseFile);
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
//        if (!isReceiverAvailable) {
        try {
            JSONObject data = new JSONObject();
            data.put("title", preferencemanager.getString(Constants.KEY_NAME));
            data.put("body", binding.inputMessage.getText().toString());
            data.put("SenderID", preferencemanager.getString(Constants.KEY_USER_ID));
            data.put("SenderName", preferencemanager.getString(Constants.KEY_NAME));
            data.put("SenderImage", preferencemanager.getString(Constants.KEY_image));
            data.put("reciverID", receiverUser.id);
            data.put("reciverImage", receiverUser.image);
            data.put("reciverName", receiverUser.name);
            data.put("conveId", conversationId);
            data.put("notificationType", "simple");

            JSONObject body = new JSONObject();
            body.put("data", data);
            body.put("to", receiverUser.token);
            body.put("priority", "high");

            Log.d("Notification Body ------", body.toString());
            sendNotification(body.toString());

        } catch (Exception exception) {
            showToast(exception.getMessage());
        }
//        }
        binding.inputMessage.setText(null);

    }

    private void sendImage() {
        StorageReference ref = storageReference.child("images/" + imageUri.getLastPathSegment());
        uploadImage = ref.putFile(imageUri);
        uploadImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                showToast("wait image is sending");
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


                try {
                    JSONObject data = new JSONObject();
                    data.put("title", preferencemanager.getString(Constants.KEY_NAME));
                    data.put("body",downloadUri );
                    data.put("SenderID", preferencemanager.getString(Constants.KEY_USER_ID));
                    data.put("SenderName", preferencemanager.getString(Constants.KEY_NAME));
                    data.put("SenderImage", preferencemanager.getString(Constants.KEY_image));
                    data.put("reciverID", receiverUser.id);
                    data.put("reciverImage", receiverUser.image);
                    data.put("reciverName", receiverUser.name);
                    data.put("conveId", conversationId);
                    data.put("notificationType", "image");

                    JSONObject body = new JSONObject();
                    body.put("data", data);
                    body.put("to", receiverUser.token);
                    body.put("priority", "high");

                    Log.d("Notification Body ------", body.toString());
                    sendNotification(body.toString());

                } catch (Exception exception) {
                    showToast(exception.getMessage());
                }


            }
        });

    }

    private void sendPdf() {
        String fileName = System.currentTimeMillis() + ".pdf";
        StorageReference ref = storageReference.child("document/").child(fileName);
//        StorageReference ref = storageReference.child("document/").child(documentUri.getLastPathSegment());
        Log.d("pdfname",documentUri.getLastPathSegment());
        Log.d("Downloadpdfuri",documentUri.toString());
        uploadImage = ref.putFile(documentUri);
        uploadImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                showToast("wait pdf is sending");
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
                    String id = ref.getId();
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_CHAT_ID, id);
                    message.put(Constants.MESSAGE_TYPE, "pdf");
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_MESSAGE, downloadUri);
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
                    StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUri);
                    httpsReference.getName();

                }

                try {
                    JSONObject data = new JSONObject();
                    data.put("title", preferencemanager.getString(Constants.KEY_NAME));
                    data.put("body",fileName);
                    data.put("SenderID", preferencemanager.getString(Constants.KEY_USER_ID));
                    data.put("SenderName", preferencemanager.getString(Constants.KEY_NAME));
                    data.put("SenderImage", preferencemanager.getString(Constants.KEY_image));
                    data.put("reciverID", receiverUser.id);
                    data.put("reciverImage", receiverUser.image);
                    data.put("reciverName", receiverUser.name);
                    data.put("conveId", conversationId);
                    data.put("notificationType", "simple");

                    JSONObject body = new JSONObject();
                    body.put("data", data);
                    body.put("to", receiverUser.token);
                    body.put("priority", "high");

                    Log.d("Notification Body ------", body.toString());
                    sendNotification(body.toString());

                } catch (Exception exception) {
                    showToast(exception.getMessage());
                }
            }
        });
    }

    private void sendWord() {
        String fileName = System.currentTimeMillis() + ".word";
        StorageReference ref = storageReference.child("document/").child(fileName);
        uploadImage = ref.putFile(documentUri);
        uploadImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                showToast("wait wordFile  is sending");
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                downloadUri = uriTask.getResult().toString();

                if (uriTask.isSuccessful()) {
                    DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
                    String id = ref.getId();
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_CHAT_ID, id);
                    message.put(Constants.MESSAGE_TYPE, "word");
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_MESSAGE, downloadUri);
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
                    StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUri);
                    httpsReference.getName();
                }
                try {
                    JSONObject data = new JSONObject();
                    data.put("title", preferencemanager.getString(Constants.KEY_NAME));
                    data.put("body",fileName);
                    data.put("SenderID", preferencemanager.getString(Constants.KEY_USER_ID));
                    data.put("SenderName", preferencemanager.getString(Constants.KEY_NAME));
                    data.put("SenderImage", preferencemanager.getString(Constants.KEY_image));
                    data.put("reciverID", receiverUser.id);
                    data.put("reciverImage", receiverUser.image);
                    data.put("reciverName", receiverUser.name);
                    data.put("conveId", conversationId);
                    data.put("notificationType", "simple");

                    JSONObject body = new JSONObject();
                    body.put("data", data);
                    body.put("to", receiverUser.token);
                    body.put("priority", "high");

                    Log.d("Notification Body ------", body.toString());
                    sendNotification(body.toString());

                } catch (Exception exception) {
                    showToast(exception.getMessage());
                }
            }
        });
    }

    private void sendVoiceMessage(String audioPath) {
        Uri audioFile = Uri.fromFile(new File(audioPath));
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("*/*").build();
        StorageReference ref = storageReference.child("Audio/" + audioFile.getLastPathSegment());
        UploadTask audio = ref.putFile(audioFile);
        audio.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String audioURi = uriTask.getResult().toString();
                audiouRi = Uri.parse(audioURi);
                if (uriTask.isSuccessful()) {
                    DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
                    String id = ref.getId();
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferencemanager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_CHAT_ID, id);
                    message.put(Constants.MESSAGE_TYPE, "audio");
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_MESSAGE, audioURi);
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
                }


            }
        });

    }

    private String dateFormatChange(String date) {
        System.out.println(date);
        SimpleDateFormat spf = new SimpleDateFormat("MMM d, yyyy HH:mm:ss", Locale.ENGLISH);
        Date newDate;
        try {
            newDate = spf.parse(date);
            date = spf.format(newDate);
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return date;
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(Constants.getRemoteMsgHeaders(), messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("results");
                            Log.d("response--------", responseJson.toString());
                            Log.d("result--------", result.toString());
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
                    Log.d("response.code", "${}");
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
                                Log.d("UserChat::", "Token 1 -> " + receiverUser.token);
                                Log.d("UserChat::", "User 1 -> " + receiverUser.name);
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
        if (receiverUser == null) {
            Intent intent = getIntent();
            receiverUser = new UserModel();

            receiverUser.setName(intent.getStringExtra("receiveFromName"));
            receiverUser.setId(intent.getStringExtra("receiveFromId"));
            receiverUser.setImage(intent.getStringExtra("receiveFromImage"));

            binding.textName.setText(receiverUser.name);
            Log.d("reciverUserModel:", String.valueOf(receiverUser));
            String senderId = intent.getStringExtra("senderId");
            String senderName = intent.getStringExtra("senderName");
            String senderImage = intent.getStringExtra("senderImage");
            conversationId = intent.getStringExtra("converId");
            Log.d("UserID::", "ID -> " + senderId);

            database = FirebaseFirestore.getInstance();
            DocumentReference ref = database.collection(Constants.KEY_COLLECTION_CHAT).document();
            String id = ref.getId();
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, senderId);
            message.put(Constants.KEY_CHAT_ID, id);
            message.put(Constants.MESSAGE_TYPE, "text");
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_MESSAGE, replyMessages);
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).document(id).set(message);
            if (conversationId != null) {
                updateConversion(replyMessages);
            } else {
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, senderId);
                conversion.put(Constants.KEY_SENDER_NAME, senderName);
                conversion.put(Constants.KEY_SENDER_IMAGE, senderImage);
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, replyMessages);
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
//        if (!isReceiverAvailable) {
            try {
                JSONObject data = new JSONObject();
                data.put("title", senderName);
                data.put("body", replyMessages);
                data.put("SenderID", senderId);
                data.put("SenderName", senderName);
                data.put("SenderImage", senderImage);
                data.put("reciverID", receiverUser.id);
                data.put("reciverImage", receiverUser.image);
                data.put("reciverName", receiverUser.name);
                data.put("conveId", conversationId);
                data.put("notificationType", "simple");

                JSONObject body = new JSONObject();
                body.put("data", data);
                body.put("to", receiverUser.token);
                body.put("priority", "high");

                Log.d("Notification Body ------", body.toString());
                sendNotification(body.toString());

            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
//        }

        }
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

    private void broadcastReceiver() {
        Intent intent = getIntent();
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            replyMessages = remoteInput.getCharSequence("key1") + "";
            Log.d("remotemessaeg", replyMessages);
        }
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

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

    private final ActivityResultLauncher<Intent> pickPdf = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        documentUri = result.getData().getData();
                        sendPdf();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickWord = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        documentUri = result.getData().getData();
                        sendWord();
                    }
                }
            }
    );

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        receiverUser = (UserModel) intent.getSerializableExtra("userBun");
        Log.d("reciverUserModel:", String.valueOf(receiverUser));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

}