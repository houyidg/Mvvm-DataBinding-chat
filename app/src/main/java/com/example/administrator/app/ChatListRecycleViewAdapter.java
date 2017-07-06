package com.example.administrator.app;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.app.databinding.ItemChatBinding;

import java.util.ArrayList;
import java.util.List;

import entity.MsgModel;

public class ChatListRecycleViewAdapter extends RecyclerView.Adapter {
    public String EventId = "";
    EventListActivity activity;
    public MsgModel lastMsgModel;
    public List<MsgModel> msgModels = new ArrayList();

    public ChatListRecycleViewAdapter(EventListActivity activity, String eventId, List<MsgModel> data) {
        this.activity = activity;
        this.EventId = eventId;
        this.msgModels = data;
    }

    public int getItemCount() {
        if (this.msgModels == null)
            return 0;
        return this.msgModels.size();
    }

    public void resetData(List<MsgModel> paramList) {
        this.msgModels = paramList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder1, final int position) {
        final ChatViewHolder viewHolder = (ChatViewHolder) viewHolder1;
        ItemChatBinding itemChatBinding = viewHolder.itemChatBinding;
        final MsgModel msgModel = msgModels.get(position);
        itemChatBinding.setModel(msgModel);
        itemChatBinding.setViewmodel(new ChatItemViewModel(itemChatBinding, msgModel, activity, this));
        itemChatBinding.executePendingBindings();
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt) {
        return new ChatViewHolder(View.inflate(paramViewGroup.getContext(), R.layout.item_chat, null));
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ItemChatBinding itemChatBinding;
        public ChatViewHolder(View view) {
            super(view);
            itemChatBinding = ItemChatBinding.bind(view);
        }
    }
}