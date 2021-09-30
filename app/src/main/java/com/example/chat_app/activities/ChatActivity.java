package com.example.chat_app.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chat_app.R;
import com.example.chat_app.adapter.AdapterChat;
import com.example.chat_app.data.ChatMessage;
import com.example.chat_app.data.User;
import com.example.chat_app.databinding.ActivityChatBinding;
import com.example.chat_app.network.ApiClient;
import com.example.chat_app.network.ApisService;
import com.example.chat_app.utilities.Constants;
import com.example.chat_app.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import static com.example.chat_app.R.string.Notification_sent_successfully;
import static com.example.chat_app.R.string.error;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessageList;
    private AdapterChat adapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore firebaseFirestore;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
        loadRecyclerDetails();
        init();
        listenMessage();
        sendNotification("");
    }

    // تعريف ال recyclerView
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        adapter = new AdapterChat(chatMessageList, getBitmapFromEncode(receiverUser.image), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerview.setAdapter(adapter);
        binding.chatRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    // ارسال الرسائل
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        // اذا كانت هناك بيانات سابقة قم بتحديثها فقط دون رفع بيانات جديده
        if (conversionId != null) {
            updateConversion(binding.inputMessage.getText().toString());
        } else {

            // رفع البيانات كامله في الفايز بيز اذا لم يكن هناك بيانات سابقة
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray token = new JSONArray();
                token.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID , preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME , preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN , preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE , binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA , data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS , token);

            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        binding.inputMessage.setText(null);
    }

    // تخزين ورفع البيانات من الشيرد والفايربيز
    private void listenMessage() {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER, receiverUser.id)
                .addSnapshotListener(eventListener);

        firebaseFirestore.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    // عرض تفاصيل الرسائل
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dataTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessageList.add(chatMessage);
                }
            }
            Collections.sort(chatMessageList, (obj1, obj2) -> obj1.dataObject.compareTo(obj2.dataObject));
            if (count == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.chatRecyclerview.smoothScrollToPosition(chatMessageList.size() - 1);
            }
            binding.chatRecyclerview.setVisibility(View.VISIBLE);
        }
        binding.progress.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    };

    // عرض الصور
    private Bitmap getBitmapFromEncode(String encodeImage) {
        if (encodeImage != null) {
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }

    }

    // تحميل تفاصيل ال recycler
    private void loadRecyclerDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    // النقر على الازرار
    private void setListener() {
        binding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    // موعد قراءة الرساله
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMMM dd , yyyy - hh:mm a", Locale.getDefault()).format(date);

    }

    // ميثود لرفع البيانات على الفايربيز
    private void addConversion(HashMap<String, Object> conversion) {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSATION).add(conversion).addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    // ميثود لتحديث البيانات على الفايربيز لعدم تكرار كثرة المحادثات
    private void updateConversion(String message) {
        DocumentReference documentReference =
                firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversionId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date());
    }

    // عرض بيانات المحادثة
    private void checkForConversion() {
        if (chatMessageList.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id);

            checkForConversionRemotely(
                    receiverUser.id, preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    //  معرفة اذا كان المستخدم موجود ام لا واذا كان غير موجود يتم ارسال اشعار له في حالة قام مستخدم اخر بمحادثته واظهار ايقونة اونلاين اذا كان موجود
    private void listenAvailabilityOfReceiver() {
        // private Boolean isReceiverAvailable = false; --> global
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
            }
            receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
            if (receiverUser.image == null) {
                receiverUser.image = value.getString(Constants.KEY_IMAGE);
                adapter.setReceiverProfileImage(getBitmapFromEncode(receiverUser.image));
                adapter.notifyItemRangeChanged(0 , chatMessageList.size());
            }
            if (isReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
    }

    // التاكد من ارسال الاشعار الى المستخدم
    private void sendNotification(String messageBody) {
        ApiClient.getlint().create(ApisService.class).sendMessage(Constants.getRemoteMesHeader() , messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("result");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                Toast.makeText(ChatActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();;
                    }
                    Toast.makeText(ChatActivity.this, Notification_sent_successfully, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkForConversionRemotely(String senderId, String receiverId) {
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSATION).whereEqualTo(Constants.KEY_SENDER, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER, receiverId).get().addOnCompleteListener(completionListener);
    }

    private final OnCompleteListener<QuerySnapshot> completionListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}