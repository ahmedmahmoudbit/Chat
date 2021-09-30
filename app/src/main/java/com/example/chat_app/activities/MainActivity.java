package com.example.chat_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chat_app.R;
import com.example.chat_app.adapter.AdapterRecentConversations;
import com.example.chat_app.data.ChatMessage;
import com.example.chat_app.data.User;
import com.example.chat_app.databinding.ActivityMainBinding;
import com.example.chat_app.listeners.ConversionListener;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations ;
    private AdapterRecentConversations adapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        loadUserData();
        get_token();
        listener();
        init();
        listenConversation();
    }

    // تعريف المتغيرات
    private void init() {
        conversations = new ArrayList<>();
        adapter = new AdapterRecentConversations(conversations , MainActivity.this);
        binding.conversationRecyclerview.setAdapter(adapter);
        firestore = FirebaseFirestore.getInstance();
    }

    // الازرار والانتقالات داخل الصفحة
    private void listener() {
        binding.imageSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading(true);
                sign_out();
            }
        });
        binding.faNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext() , UserActivity.class));
            }
        });
    }

    // عرض اسم المستخدم
    private void loadUserData() {
        binding.tvName.setText(preferenceManager.getString(Constants.KEY_NAME));
        get_image_from_preference();

    }

    // الحصول على البينات من eventListener اذا كانت تطابق بيانات المستخدم الحالي للمرسل والمستقبل
    private void listenConversation() {
        firestore.collection(Constants.KEY_COLLECTION_CONVERSATION).
                whereEqualTo(Constants.KEY_SENDER , preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        firestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER , preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    // عرض الاشخاص الذين تمت المحادثة معهم واظهار اخر رسالة تمت بينكم
    private final EventListener<QuerySnapshot> eventListener = (value , error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }

                    }

                }
            }
            Collections.sort(conversations, (obj1 , obj2) -> obj2.dataObject.compareTo(obj1.dataObject));
            adapter.notifyDataSetChanged();
            binding.conversationRecyclerview.smoothScrollToPosition(0);
            binding.conversationRecyclerview.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    // الحصول على توكن المستخدم
    private void get_token() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    // عرض صورة المستخدم من الفايربيز وحفظها في الشيرد
    private void get_image_from_preference () {
        byte[] bytes = android.util.Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE) , android.util.Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0 , bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    // ميثود لعرض التوست message
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // تحديث التوكن عند عملية تسجيل الدخول
    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN , token);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN , token).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toast(getString(R.string.token_updated_successfully));
            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toast(e.getLocalizedMessage());
            }
        });
    }

    // تسجيل الخروج وحذف التوكن من الفايربيز
    private void sign_out() {
        toast(getString(R.string.Sign_out_successful));
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String  , Object> update = new HashMap<>();
        update.put(Constants.KEY_FCM_TOKEN , FieldValue.delete());

        documentReference.update(update).addOnSuccessListener( unused-> {
            preferenceManager.clear();
            onBackPressed();
            finish();

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toast(e.getLocalizedMessage());
            }
        });

    }

    // تاثير التحميل
    protected void loading (Boolean isLoading) {
        if (isLoading) {
            binding.progressbar.setVisibility(View.VISIBLE);
        } else {
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onConversionClick(User user) {
        Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
        intent.putExtra(Constants.KEY_USER , user);
        startActivity(intent);
    }
}