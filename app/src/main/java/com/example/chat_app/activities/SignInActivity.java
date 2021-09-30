package com.example.chat_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat_app.R;
import com.example.chat_app.databinding.ActivitySignInBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {
    ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        sign_in_to_app_always();
        set_Listeners();
    }

    private void set_Listeners() {
        binding.createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext() , SignUpActivity.class));
            }
        });
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validation()) {
                    setData_for_firebase ();
                }
            }
        });
    }

    private void sign_in_to_app_always() {

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            startActivity(new Intent(getApplicationContext() , MainActivity.class));
            finish();
        }
    }

    private void setData_for_firebase () {
        progressbar(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS).
                whereEqualTo(Constants.KEY_EMAIL , binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD , binding.inputPassword.getText().toString()).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                    DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN , true);
                    preferenceManager.putString(Constants.KEY_USER_ID , snapshot.getId());
                    preferenceManager.putString(Constants.KEY_NAME , snapshot.getString(Constants.KEY_NAME));
                    preferenceManager.putString(Constants.KEY_IMAGE , snapshot.getString(Constants.KEY_IMAGE));

                    Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    progressbar(false);
                    showText(getString(R.string.Unable_to_sign_in));
                }
            }
        });
    }

    private Boolean Validation() {
    if (binding.inputEmail.getText().toString().trim().isEmpty()) {
        showText("Enter your email ! ");
        return false;
//    } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
//        showText("Enter Valid email !");
//        return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
        showText("enter Password");
        return false;
    } else  {
        return true;
    }}

    private void showText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void progressbar (Boolean loading) {
        if (loading) {
            binding.btnSignIn.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.progressbar.setVisibility(View.INVISIBLE);
        }

    }

}