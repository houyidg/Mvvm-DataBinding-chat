package com.example.administrator.app;

import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.example.administrator.app.data.DataRepository;
import com.example.administrator.app.databinding.EventListBinding;

import java.util.List;

import entity.MsgModel;
import recordlib.RecorderButton;
import utils.RandomGUID;

/**
 * Created by Administrator on 2017/5/9.
 */

public class ChatViewModel extends BaseObservable {
    public final String TAG = "ChatViewModel";
    public final boolean DEBUG = true;
    public final ObservableList<MsgModel> items = new ObservableArrayList<>();
    public final ObservableBoolean dataLoading = new ObservableBoolean(false);
    public final ObservableField<String> bg = new ObservableField<>("xxxxxxxxxx");
    private MsgModel needPlayModel;
    private String playVoiceBaseUrl = Environment.getExternalStorageDirectory().getPath() + "/Glood/cache/Audio/";
    EventListBinding eventListBinding;

    public ChatViewModel(EventListBinding eventListBinding) {
        List<MsgModel> msgModels = DataRepository.produceData();
        //init data
        items.addAll(msgModels);
        String str = playVoiceBaseUrl;
        this.eventListBinding = eventListBinding;
        RecorderButton rbEnterChat = eventListBinding.rbEnterChat;
        rbEnterChat.setAudioStateRecorderListener(new ChatViewModel.MyRecordListener());
        rbEnterChat.initAudioRecordManager(str);
    }

    public void loadData(boolean isRefresh) {
        loadData(isRefresh, true);
    }

    public void loadData(final boolean isRefresh, boolean isShowUi) {
        if (DEBUG) {
            Log.e(TAG, "loadData:isRefresh" + isRefresh);
        }
        if (isShowUi) {
            dataLoading.set(true);
        }
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRefresh) {
                    items.clear();
                }
                List<MsgModel> msgModels = DataRepository.produceData(isRefresh);
                items.addAll(msgModels);
                dataLoading.set(false);
                bg.set("I love u");
            }
        }, 1000);
    }

    public class MyRecordListener implements RecorderButton.AudioStateRecorderListener {
        private boolean isReallyPlaying = false;

        public MyRecordListener() {
        }

        public String getPlayingVoiceFileName() {
            String name = "temp.amr";
            try {
//            String id = getPreFakeMsgId();
//            name = id + suffixName;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return name;
        }

        public void onCancel(boolean paramBoolean) {
            resetRecordingView();
            removeFirstPreModel();
        }

        private void removeFirstPreModel() {
            items.remove(0);
            eventListBinding.rvList.scrollToPosition(0);
        }

        public void onFinish(float time, String path) {
            try {
                resetRecordingView();
                needPlayModel.playTime.set(time);
                needPlayModel.status.set((byte) 2);
                eventListBinding.rvList.scrollToPosition(0);
                sendSoundToServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onLongClick(View paramView) {
        }

        public void onReleaseMediaPlayer() {
//        if (mRecycleViewAdapter.mediaHelper != null)
//            mRecycleViewAdapter.mediaHelper.stopPlay();
        }

        public void onReturnToRecord() {
        }

        public void onStart(float paramFloat) {
            needPlayModel = new MsgModel();
            needPlayModel.status.set((byte) 0);
            addPreModel(needPlayModel);
            this.isReallyPlaying = true;
        }

        public void addPreModel(MsgModel msgModel) {
            msgModel.userName = "zx" + Math.random() * 100;
            msgModel.status.set((byte) 0);
            msgModel.isRead.set(true);
            msgModel.id = getPreFakeMsgId();
            msgModel.setIdempotentId(msgModel.id);
            eventListBinding.rvList.scrollToPosition(0);
            items.add(0, msgModel);
//            //加入列表
//            mRecycleViewAdapter.add(msgModel, 0);
//
//            //加入缓存列表
//            preAddCacheMsgList.add(0, msgModel);
        }

        public void onUpdateTime(float currentTime, float minTime, float maxTime) {
            if (DEBUG) {
                Log.e(TAG, "onUpdateTime:currentTime:" +currentTime);
            }
            if(isReallyPlaying){
                needPlayModel.playTime.set(currentTime);
                eventListBinding.rvList.scrollToPosition(0);
            }
        }

        public void onVoiceChange(int paramInt) {
        }

        public void onWantToCancel() {
        }

        public void resetRecordingView() {
            this.isReallyPlaying = false;
        }
    }

    private void sendSoundToServer() {
        if (DEBUG) {
            Log.e(TAG, "sendSoundToServer:");
        }
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                needPlayModel.status.set((byte) 1);
            }
        }, 5000);
    }

    @NonNull
    private String getPreFakeMsgId() {
        return new RandomGUID().toString();
    }
}
