package com.example.chat_app.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat_app.R;
import com.example.chat_app.listner.UserListener;
import com.example.chat_app.model.UserModel;
import com.example.chat_app.databinding.ItemContainerUserBinding;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<UserModel> users;
    private  final UserListener userListener;
    private List<UserModel> selectedUsers;
    private Context context;

    public UserAdapter(List<UserModel> users,UserListener userListener,Context context) {
        this.users = users;
        this.userListener = userListener;
        selectedUsers = new ArrayList<>();
        this.context = context;
    }

    public List<UserModel> getSelectedUsers() {
        return selectedUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;

        }

        void setUserData(UserModel user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            Glide.with(context).load(user.image).placeholder(R.drawable.ic_baseline_person_24)
                    .into(binding.imageProfile);
//            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> {
                userListener.onUserClicked(user);
                selectedUsers.add(user);
            });
        }
    }

   /* private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }*/
}
