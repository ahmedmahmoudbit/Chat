package com.example.chat_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chat_app.R;
import com.example.chat_app.adapter.AdapterUser;
import com.example.chat_app.data.User;
import com.example.chat_app.databinding.ActivityUserBinding;
import com.example.chat_app.listeners.UserListener;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setListeners();

    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // عرض كل الاشخاص الذين قامو بالتسجيل في التطبيق داخل recyclerView
    private void getUsers() {
        loading(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                loading(false);
                String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                if (task.isSuccessful() && task.getResult() != null) {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                            continue;
                        }
                        User user = new User();
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        user.id = queryDocumentSnapshot.getId();
                        users.add(user);
                        }
                        if (users.size() > 0) {
                        AdapterUser adapterUser = new AdapterUser(users , UserActivity.this);
                        binding.recyclerview.setAdapter(adapterUser);
                        binding.recyclerview.setLayoutManager(new LinearLayoutManager(UserActivity.this));
                        binding.recyclerview.setVisibility(View.VISIBLE);
                    } else {
                        showErrorMessage();
                    }
                } else {
                    showErrorMessage();
                }
            }
        });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s" , getString(R.string.No_User_available)));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    protected void loading (Boolean isLoading) {
        if (isLoading) {
            binding.progress.setVisibility(View.VISIBLE);
        } else {
            binding.progress.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
        intent.putExtra(Constants.KEY_USER , user);
        startActivity(intent);
        finish();
    }
}