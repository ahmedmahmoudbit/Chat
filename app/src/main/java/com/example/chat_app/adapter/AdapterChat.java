package com.example.chat_app.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat_app.data.ChatMessage;
import com.example.chat_app.databinding.ItemContainerReceivedMessageBinding;
import com.example.chat_app.databinding.ItemContainerSendMessageBinding;

import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessageList;
    private  Bitmap receiverProfileImage;
    private final String sendrId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public AdapterChat(List<ChatMessage> chatMessageList, Bitmap receiverProfileImage, String sendrId) {
        this.chatMessageList = chatMessageList;
        this.receiverProfileImage = receiverProfileImage;
        this.sendrId = sendrId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SenderMessageHolder(
                    ItemContainerSendMessageBinding.inflate(LayoutInflater.from(parent.getContext()) , parent , false));
        } else {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()) , parent , false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SenderMessageHolder) holder).setData(chatMessageList.get(position));
        } else  {
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position) , receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessageList.get(position).senderId.equals(sendrId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SenderMessageHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSendMessageBinding binding;

        SenderMessageHolder(ItemContainerSendMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDataTime.setText(chatMessage.dataTime);
        }

    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerSendMessageBinding) {
            super(itemContainerSendMessageBinding.getRoot());
            binding = itemContainerSendMessageBinding;
        }

        void setData(ChatMessage chatMessage , Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDataTime.setText(chatMessage.dataTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }
}
