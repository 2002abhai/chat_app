package com.example.chat_app.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chat_app.uitilies.Constants;
import com.example.chat_app.uitilies.Preferencemanager;
import com.example.chat_app.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private Preferencemanager preferencemanager;
//    private String encodeImage;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri imageUri;
    UploadTask uploadImage;
    Uri downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        preferencemanager = new Preferencemanager(getApplicationContext());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonRegister.setOnClickListener(v -> {
            if (isValidSignUpDetail()) {
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void signUp() {
        loading(true);
        StorageReference ref = storageReference.child("images/"+ imageUri.getLastPathSegment());
        uploadImage = ref.putFile(imageUri);
         uploadImage.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                     downloadUri = task.getResult();
                    Log.e("Imageuri",downloadUri.toString());
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
                    user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
                    user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
                    user.put(Constants.KEY_image, downloadUri);

                    database.collection(Constants.KEY_COLLECTION_USER).add(user)
                            .addOnSuccessListener(documentReference -> {
                                loading(false);
                                preferencemanager.putBoolean(Constants.KEY_IS_SIGN_IN,true);
                                preferencemanager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                preferencemanager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                                preferencemanager.putString(Constants.KEY_image, downloadUri.toString());
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }).addOnFailureListener(exception -> {
                                loading(false);
                                showToast(exception.getMessage());
                            });
                }
            }
        });

    }

   /* private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }*/


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                         imageUri = result.getData().getData();
                        try {
                            Glide.with(this).load(imageUri).into(binding.imageProfile);
                            binding.textAddImage.setVisibility(View.GONE);
                          /*  InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);*/
                        } catch (Error e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            );

    private Boolean isValidSignUpDetail() {
        if (imageUri == null) {
            showToast("Select Profile Image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter Name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else if (binding.inputConformPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Conform Password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConformPassword.getText().toString())) {
            showToast("Password and Conform Password must be same");
            return false;
        } else {
            return true;
        }

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonRegister.setVisibility(View.INVISIBLE);
            binding.progresBar.setVisibility(View.VISIBLE);
        } else {
            binding.progresBar.setVisibility(View.INVISIBLE);
            binding.buttonRegister.setVisibility(View.VISIBLE);
        }
    }
}