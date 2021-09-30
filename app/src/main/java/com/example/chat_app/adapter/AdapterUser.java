package com.example.chat_app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.data.User;
import com.example.chat_app.databinding.ItemContainerUserBinding;
import com.example.chat_app.listeners.UserListener;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.Holder> {
    private List<User> users;
    private final UserListener userListtener;


    public AdapterUser(List<User> users , UserListener userListtener) {
        this.users = users;
        this.userListtener = userListtener;
    }

    // show image from Server
    private Bitmap getUserImage(String encodeImage) {
        byte[] bytes = Base64.decode(encodeImage , Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemContainerUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.setData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;

        public Holder(@NonNull ItemContainerUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // define all id in ItemContainerUser .
        void setData(User user) {
            binding.textEmail.setText(user.email);
            binding.textName.setText(user.name);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));

            binding.getRoot().setOnClickListener(v -> {
                    userListtener.onUserClick(user);
            });
        }
    }
}
