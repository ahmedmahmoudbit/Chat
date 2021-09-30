package com.example.chat_app.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import com.example.chat_app.databinding.ActivitySignUpBinding;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        set_Listeners();

    }

    // onBackPressed --> startActivity(new Intent(getApplicationContext() , SignInActivity.class))
    private void set_Listeners() {
        binding.signIn.setOnClickListener(v -> onBackPressed());

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidSignUpDetails()) {
                    sign_up();
                }
            }
        });

        binding.layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);

            }
        });
    }

    // Print Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Send Data to Firebase and Shared Preference
    private void sign_up() {
        loading(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        HashMap<String , Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME , binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL , binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD , binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE , encodeImage);

        firestore.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                loading(false);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN , true);
                preferenceManager.putString(Constants.KEY_USER_ID , documentReference.getId());
                preferenceManager.putString(Constants.KEY_NAME , binding.inputName.getText().toString());
                preferenceManager.putString(Constants.KEY_IMAGE , encodeImage);
                Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            loading(false);
            showToast(e.getMessage());
            }
        });
    }

    // convert image to encode
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap , previewWidth , previewHeight , false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG , 50 , byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes , Base64.DEFAULT);
    }

    // save Uri image
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult() , result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    // validations ( Account Data )
    private Boolean isValidSignUpDetails() {
        if (encodeImage == null) {
            showToast("Select Image  profile");
            return false;
        } else if (binding.inputName.getText().toString().isEmpty()) {
            showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().isEmpty()) {
            showToast("Please enter your Email");
            return false;

            // matches email .
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid Email");
            return false;
        } else if (binding.inputPassword.getText().toString().isEmpty()) {
            showToast("Please enter your Password");
            return false;
        } else if (binding.inputRepassword.getText().toString().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!binding.inputRepassword.getText().toString().equals(binding.inputRepassword.getText().toString())) {
            showToast("password & Confirm password must be same");
            return false;
        } else {
            return true;
        }
    }

    // ProgressBar off and on .
    protected void loading (Boolean isLoading) {
        if (isLoading) {
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.progress.setVisibility(View.VISIBLE);
        } else {
            binding.progress.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setVisibility(View.VISIBLE);
        }
    }
}