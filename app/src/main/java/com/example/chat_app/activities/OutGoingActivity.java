package com.example.chat_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat_app.network.ApiClient;
import com.example.chat_app.network.ApiService;
import com.example.chat_app.uitilies.Constants;
import com.example.chat_app.uitilies.Preferencemanager;
import com.example.chat_app.R;
import com.example.chat_app.model.UserModel;
import com.example.chat_app.databinding.ActivityOutGoingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutGoingActivity extends AppCompatActivity {

    ActivityOutGoingBinding binding;
    private Preferencemanager preferencemanager;
    private String inviteToken = null;
    private String meetingRoom=null;
    private String meetingType=null;
    private int rejectionCount=0;
    private int totalReceivers=0;
    private  UserModel user ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding =  ActivityOutGoingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencemanager = new Preferencemanager(getApplicationContext());
        meetingType=getIntent().getStringExtra("type");

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                binding.imagemeetingtype.setImageResource(R.drawable.ic_videocam);
            }else {
                binding.imagemeetingtype.setImageResource(R.drawable.ic_call);
            }
        }

        user =  (UserModel) getIntent().getSerializableExtra("user");

        if (user != null) {
            Glide.with(this).load(user.image).into(binding.outgoingImageProfile);
//            binding.outgoingImageProfile.setImageBitmap(getBitmapFromEncodedString(user.image));
            binding.textusername.setText(user.name);
            binding.textEmail.setText(user.email);
        }
        binding.imageStopInvitation.setOnClickListener(view -> {
            cancelInvitation(user.token, null);
                }
        );

       /* binding.imageStopInvitation.setOnClickListener(view -> {
                    if (getIntent().getBooleanExtra("isMultiple", false)) {
                        Type type = new TypeToken<ArrayList<UserModel>>() {
                        }.getType();
                        ArrayList<UserModel> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                        if (receivers != null) {
                            totalReceivers = receivers.size();
                        }
                        cancelInvitation(null, receivers);
                    } else {
                        if (user != null) {
                            cancelInvitation(user.token, null);
                        }
                    }
                }
        );
*/

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    inviteToken = task.getResult();
                    if (meetingType != null) {
                            Type type = new TypeToken<ArrayList<UserModel>>() {}.getType();
                        UserModel user = (UserModel) getIntent().getSerializableExtra("user");
                      /*  String token =  getIntent().getStringExtra("token");
                        inviteToken = token;*/
                            initiateMeeting(meetingType, null, user);
                        } else {
                            if (user != null) {
                                totalReceivers=1;
                                initiateMeeting(meetingType, user.token,null);
                            }
                        }
                }
            }
        });


       /* FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    inviteToken = task.getResult();
                    if (meetingType != null) {
                        if (getIntent().getBooleanExtra("isMultiple", false)) {
                            Type type = new TypeToken<UserModel>() {

                            }.getType();
                            ArrayList<UserModel> receivers = new Gson().fromJson(getIntent().getStringExtra("selectedUsers"), type);
                            if (receivers!=null)
                            {
                                totalReceivers=receivers.size();
                            }
                            initiateMeeting(meetingType, null, receivers);
                        } else {
                            if (user != null) {
                                totalReceivers=1;
                                initiateMeeting(meetingType, user.token,null);
                            }
                        }
                    }
                }
            }
        });*/
    }

    private void initiateMeeting(String meetingType, String receiverToken, UserModel receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken!=null)
            {
                tokens.put(receiverToken);
            }

            if (receivers != null)
            {
                StringBuilder userNames=new StringBuilder();
                tokens.put(receivers.token);
                    userNames.append(receivers.name);
                binding.textEmail.setVisibility(View.GONE);
                binding.textusername.setText(userNames.toString());
            }

            JSONObject main = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_USER, preferencemanager.getString(Constants.KEY_NAME));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviteToken);
            data.put(Constants.KEY_image,preferencemanager.getString(Constants.KEY_image));
            meetingRoom=preferencemanager.getString(Constants.KEY_USER_ID)+" "+ UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom);

            main.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            main.put(Constants.REMOTE_MSG_DATA,data);


            sendRemoteMessage(main.toString(), Constants.REMOTE_MSG_INVITATION);
            Log.e("sendMessage",main.toString());
        } catch (Exception exception) {
            Toast.makeText(OutGoingActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendMessage(Constants.getRemoteMsgHeaders(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                                Toast.makeText(OutGoingActivity.this, "Invitation Sent Successfully", Toast.LENGTH_SHORT).show();
                            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                                Toast.makeText(OutGoingActivity.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(OutGoingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(OutGoingActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }


    private void cancelInvitation(String receiverToken,UserModel receivers) {
        try {
            JSONArray tokens = new JSONArray();
            if (receiverToken!=null) {
                tokens.put(receiverToken);
            }
            if (receivers!=null ) {
                    tokens.put(receivers.token);
            }
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL=new URL("https://meet.jit.si");

                        JitsiMeetConferenceOptions.Builder builder=new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio"))
                        {
                            builder.setVideoMuted(true);
                            builder.setAudioOnly(true);
                        }
                        JitsiMeetActivity.launch(OutGoingActivity.this,builder.build());
                        finish();
                    }
                    catch (Exception exception)
                    {
                        Toast.makeText(context,exception.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    cancelInvitation(user.token, null);
                    rejectionCount+=1;
                    if (rejectionCount==totalReceivers)
                    {
                        Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}