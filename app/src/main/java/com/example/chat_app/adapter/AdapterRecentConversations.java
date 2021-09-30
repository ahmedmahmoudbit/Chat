package com.example.chat_app.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.data.ChatMessage;
import com.example.chat_app.data.User;
import com.example.chat_app.databinding.ItemContainerRecentConversionBinding;
import com.example.chat_app.listeners.ConversionListener;

import java.util.List;

public class AdapterRecentConversations extends RecyclerView.Adapter<AdapterRecentConversations.ConversionHolder> {

    private final List<ChatMessage> chatMessageList;
    private final ConversionListener conversionListener;

    public AdapterRecentConversations(List<ChatMessage> chatMessageList , ConversionListener conversionListener) {
        this.chatMessageList = chatMessageList;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionHolder(ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()) , parent , false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    // كلاس الهولد وتعريف المتغيرات بداخله
    class ConversionHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding ;

        ConversionHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionClick(user);
                });
        }
    }

    // عرض الصورة والتعامل معها
    private Bitmap getConversionImage(String encodeImage){
        byte [] bytes = Base64.decode(encodeImage , Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes , 0 , bytes.length);
    }

}
