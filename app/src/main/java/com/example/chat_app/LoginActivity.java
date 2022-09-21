package com.example.chat_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.databinding.ActivityLoginBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private Preferencemanager preferencemanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencemanager = new Preferencemanager(getApplicationContext());
        if(preferencemanager.getBoolean(Constants.KEY_IS_SIGN_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
            }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class)));
        binding.buttonLogin.setOnClickListener(v -> {
            if (isValidLoginDetail()) {
                login();
            }
        });
    }

    private void login(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size() >0){
                       DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                       preferencemanager.putBoolean(Constants.KEY_IS_SIGN_IN,true);
                       preferencemanager.putString(Constants.KEY_USER_ID,snapshot.getId());
                       preferencemanager.putString(Constants.KEY_NAME,snapshot.getString(Constants.KEY_NAME));
                       preferencemanager.putString(Constants.KEY_image,snapshot.getString(Constants.KEY_image));
                       Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }else{
                       loading(false);
                       showToast("Email and Password not match ");
                   }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonLogin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonLogin.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidLoginDetail(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter Valid Email");
            return false;
        }else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        }else {
            return  true;
        }
    }
}