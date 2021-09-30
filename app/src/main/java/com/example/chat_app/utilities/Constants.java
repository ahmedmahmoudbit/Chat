package com.example.chat_app.utilities;

import java.util.HashMap;

public class Constants {

    public static final String KEY_COLLECTION_USERS = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER = "senderId";
    public static final String KEY_RECEIVER = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";

    // visible all data sender
    public static final String KEY_COLLECTION_CONVERSATION = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";

    // availability user
    public static final String KEY_AVAILABILITY = "availability";

    // MSG to user
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String KEY_MSG_CONTENT_TYPE  = "Content-Type";
    public static HashMap<String , String> remoteMesHeader = null ;
    public static HashMap<String , String> getRemoteMesHeader() {

        if (remoteMesHeader == null) {
            remoteMesHeader = new HashMap<>();
            // write key = ( paste Server key )
            remoteMesHeader.put(REMOTE_MSG_AUTHORIZATION ,
                    "key=AAAA6lZQu0A:APA91bGAopTGX0ZbrzF_Jhf_rCiTBLLKSOgKV66pzpRJJrXsAmtonSQXdDbLyqbmnzH1oa_M-f9mFRjIoHj0nz90bOoJ3UdQ30NVqcZgYpf5W6HCvGiQ3q6RJxX2cqNBaFbNwqdQMuUe");
            remoteMesHeader.put(KEY_MSG_CONTENT_TYPE , "application/json");
        }
        return remoteMesHeader;

    }
}
