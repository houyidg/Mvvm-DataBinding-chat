package com.example.administrator.app;

import android.content.Context;
import android.databinding.BaseObservable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.administrator.app.databinding.ItemChatBinding;

import entity.MsgModel;
import utils.AppConfig;
import utils.MediaHelper;
import widget.GlideCircleTransform;

/**
 * Created by Administrator on 2017/5/9.
 */

public class ChatItemViewModel extends BaseObservable {
    public final String TAG = "ChatViewModel";
    public final boolean DEBUG = true;
    ItemChatBinding mItemChatBinding;
    public MsgModel msgModel;
    Context context;
    public MediaHelper mediaHelper = MediaHelper.getManager();
    ChatListRecycleViewAdapter adapter;

    public ChatItemViewModel(ItemChatBinding itemChatBinding, MsgModel msgModel, Context context, ChatListRecycleViewAdapter adapter) {
        mItemChatBinding = itemChatBinding;
        this.msgModel = msgModel;
        this.context = context;
        this.adapter = adapter;
        init();
    }

    private void init() {
        mItemChatBinding.rlRoot.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (AppConfig.RV_CARD_HEIGHT / AppConfig.EVENT_CARD_SHOW_CHAT_SIZE)));

        if (msgModel.userAvatar == null || msgModel.userAvatar.trim().equals("")) {
            Glide.with(context)
                    .load(R.mipmap.default_user_photo)
                    .crossFade()
                    .transform(new GlideCircleTransform(context))
                    .into(mItemChatBinding.ivChatPhoto);
        }
        else {
            Glide.with(context)
                    .load(msgModel.userAvatar)
                    .transform(new GlideCircleTransform(context))
                    .error(R.mipmap.default_user_photo)
                    .crossFade()
                    .into(mItemChatBinding.ivChatPhoto);
        }
    }

    public void itemClick(MsgModel model) {
        if (DEBUG) {
            Log.e(TAG, "itemClick:model.status.get():" + model.status.get());
        }
        if (adapter.lastMsgModel != model) {//不是同一个
            if (adapter.lastMsgModel != null) {//关闭上一个
                adapter.lastMsgModel.status.set((byte) 1);
                stopVoice();
            }
        }
        if (model.status.get() == 3) {//正在play
            stopVoice();
            //if before status is the same as now status, stop play
            model.status.set((byte) 1);
        }
        else {
            startVoice();
            setIsReaded();
            model.status.set((byte) 3);
        }
        setLastMsgRecord(model);
    }

    private void setLastMsgRecord(MsgModel model) {
        adapter.lastMsgModel = model;
    }

    private void startVoice() {
        mediaHelper.play(R.raw.music, new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                try {
                    if (mediaPlayer != null) {
                        msgModel.status.set((byte) 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setIsReaded() {
        if (msgModel != null && !msgModel.isRead.get()) {
            msgModel.isRead.set(true);
        }
    }


    /**
     */
    private void stopVoice() {
        if (mediaHelper != null) {
            mediaHelper.stopPlay();
        }
    }
}
