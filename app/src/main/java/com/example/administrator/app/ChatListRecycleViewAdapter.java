package com.example.administrator.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.administrator.app.databinding.ItemChatBinding;
import com.xing.common.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import entity.MsgModel;
import utils.AnimationUtil;
import utils.AppConfig;
import utils.CommonUtils;
import utils.MediaHelper;
import utils.NameChangeUtils;
import widget.MyFrameLayout;

public class ChatListRecycleViewAdapter extends RecyclerView.Adapter {
    private static final boolean DEBUG = AppConfig.DEBUG;
    private static final String TAG = "ChatListRecy";
    private static final float END_ALARM_CIRCLE_SCALE = 2.1F;
    private static final int MSG_SHOW_ANIM_CIRCLE_2 = 1;
    public String EventId = "";
    EventListActivity activity;
    public MsgModel lastMsgModel;
    public MyGridViewLayoutManager gridLayoutManager;
    public boolean isMyFrameCanScroll = true;
    public boolean isShowName = true;
    public String lastNeedPlayMsgId = null;
    ItemViewClickListener itemViewClickListener;
    ImageView mIvChatPhoto;
    ValueAnimator mSosAlarmAnim1;
    ValueAnimator mSosAlarmAnim2;
    ImageView mSosAlarmCircle1;
    ImageView mSosAlarmCircle2;
    public MediaHelper mediaHelper = null;
    public MyFrameLayout mflLen, myPreFrameLayout;
    public FrameLayout mflBg;
    public List<MsgModel> msgModels = new ArrayList();
    public int needHoldSize = 0;
    ChatViewHolder playViewHolder;
    private String playVoiceBaseUrl = AppConfig.PATH_SAVE_AUDIO;
    public MsgModel playingMsgModel;
    private int playPos = -1;
    private int wwith = 0;
    Runnable nextAutoRunnable;
    final RecyclerView mRecycleView;
    public boolean isAutoPlaySlide = true;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_ANIM_CIRCLE_2: {
                    if (mSosAlarmAnim2 != null && !mSosAlarmAnim2.isStarted()) {
                        mSosAlarmAnim2.start();
                    }
                    break;
                }
            }
        }
    };

    public ChatListRecycleViewAdapter(EventListActivity activity, String eventId, List<MsgModel> data, RecyclerView recycleView) {
        this.activity = activity;
        this.EventId = eventId;
        this.msgModels = data;
        mRecycleView = recycleView;
        if (DEBUG) {
            Log.e(TAG, "mRecycleView:" + (mRecycleView == null));
        }
        this.wwith = CommonUtils.getWwithAndHheight(activity)[0];
        filterFulFilPreconertedSize(this.msgModels);
        itemViewClickListener = activity;
    }

    private void addAHoldMsg(List<MsgModel> paramList) {
        addAHoldMsg(paramList, 0);
    }

    private void addAHoldMsg(List<MsgModel> paramList, int paramInt) {
        MsgModel localMsgModel = new MsgModel();
        localMsgModel.preHoldId = AppConfig.HOLD_MSG_ID;
        paramList.add(paramInt, localMsgModel);
    }

    private void filterFulFilPreconertedSize(List<MsgModel> paramList) {
        int size = paramList.size();
        if (size < AppConfig.EVENT_CARD_SHOW_CHAT_SIZE) {
            this.needHoldSize = (AppConfig.EVENT_CARD_SHOW_CHAT_SIZE - size);
            int i = 1;
            while (i <= this.needHoldSize) {
                addAHoldMsg(paramList);
                i += 1;
            }
        }
        else {
            this.needHoldSize = 0;
        }
    }

    private void playViewAndVoice(final MsgModel playMsgModel, ChatViewHolder viewHolder, File file) {
        if (DEBUG) {
            Log.e(TAG, "playViewAndVoice");
        }
        playingMsgModel = playMsgModel;
        setAnimView(viewHolder);
        mediaHelper.playSound(file.getAbsolutePath(), new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                boolean autoPlayNext = false;
                try {
                    if (mediaPlayer != null) {
                        autoPlayNext = isAutoPlayNext(playMsgModel);
                        if (DEBUG) {
                            Log.e(TAG, "playViewAndVoice  autoPlayNext:" + autoPlayNext);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                clearAnimViewVoice(autoPlayNext);
            }
        }, new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                try {
                    clearAllSound();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;// already handle it
            }
        }, new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    private boolean isAutoPlayNext(MsgModel previousModel) {
        if (lastNeedPlayMsgId != null && previousModel != null) {
            final List<MsgModel> useFulData = getUseFulData();
            final int unReadModelPos = getNextUnReadModelIndex(useFulData, previousModel, lastNeedPlayMsgId);
            if (unReadModelPos != -1) { //则可以继续播放
                ensureUnReadIndexIsVisible(unReadModelPos, mRecycleView);
                nextAutoRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mediaHelper.play(R.raw.fx, new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) { //不管失败还是成功都会经历此方法
                                if (mp == null) {
                                    return;
                                }
                                //可以播放自动播放
                                ChatViewHolder needPlayViewHolder = getNeedPlayViewHolder(unReadModelPos, mRecycleView);
                                MsgModel needMsgModel = useFulData.get(unReadModelPos);
                                playVoice(needMsgModel, needPlayViewHolder, unReadModelPos);
                            }
                        });
                    }
                };
                mRecycleView.postDelayed(nextAutoRunnable, 200);
                return true;
            }
        }
        return false;
    }

    private void ensureUnReadIndexIsVisible(int unReadModelPos, RecyclerView mRecycleView) {
        if (isAutoPlaySlide) {
            int firstCompletelyVisibleItemPosition = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
            if (unReadModelPos < firstCompletelyVisibleItemPosition) {
                //scroll
                mRecycleView.smoothScrollToPosition(unReadModelPos);
            }
        }
    }

    /**
     * model不在List范围内就返回 null
     *
     * @param position
     * @param mRecycleView
     * @return
     */
    private ChatViewHolder getNeedPlayViewHolder(int position, RecyclerView mRecycleView) {
        boolean visibleItemCountRange = CommonUtils.isVisibleItemCountRange(mRecycleView, position);
        if (visibleItemCountRange) {
            //maybe is null when notifyDataSetChanged(noUI thread)
            return (ChatViewHolder) mRecycleView.findViewHolderForAdapterPosition(position);
        }
        else {
            return null;
        }
    }

    private int getNextUnReadModelIndex(List<MsgModel> useFulData, MsgModel previousPlayMsgModel, String lastNeedPlayMsgId) {
        int indexOf = useFulData.indexOf(previousPlayMsgModel);
        int lastOf = useFulData.indexOf(new MsgModel(lastNeedPlayMsgId));
        //下一个如果是读过了 返回-1、
        if (indexOf > lastOf) {
            int index = indexOf - 1;
            MsgModel msgModel = useFulData.get(index);
            return msgModel.isRead.get() ? -1 : index;
        }
        else {
            return -1;
        }
    }


    private void setAnimAndView(ChatViewHolder viewHolder) {
        playViewHolder = viewHolder;
        if (playViewHolder != null) {
            this.mSosAlarmCircle1 = playViewHolder.itemChatBinding.ivAnim1;
            this.mSosAlarmCircle2 = playViewHolder.itemChatBinding.ivAnim2;
            this.mflLen = playViewHolder.itemChatBinding.flLen;
            this.mflBg = playViewHolder.itemChatBinding.flBg;
            this.mIvChatPhoto = playViewHolder.itemChatBinding.ivChatPhoto;
        }
        else {
            this.mSosAlarmCircle1 = null;
            this.mSosAlarmCircle2 = null;
            this.mflLen = null;
            mflBg = null;
            this.mIvChatPhoto = null;
        }
    }

    private void startPhotoAnim() {
        if ((this.mSosAlarmAnim1 != null) && (!this.mSosAlarmAnim1.isStarted()))
            this.mSosAlarmAnim1.start();
        if (this.mHandler != null)
            this.mHandler.sendEmptyMessageDelayed(MSG_SHOW_ANIM_CIRCLE_2, 400L);
    }

    private void startPhotoView() {
        if (this.mSosAlarmCircle1 != null)
            this.mSosAlarmCircle1.setVisibility(View.VISIBLE);
        if (this.mSosAlarmCircle2 != null)
            this.mSosAlarmCircle2.setVisibility(View.VISIBLE);
        if (mflBg != null) {
            mflBg.setBackgroundDrawable(CommonUtils.getShape(activity, R.drawable.shape_chat_play_bg));
        }
        setChatPhotoScale(1.2F,200l,null);
    }

    private void stopAnimAndView() {
        setChatPhotoScale(1.0F,10l, null);
        stopAnim();
        setFlLenbg2();
        if(playingMsgModel!=null && mflLen!=null){
            if (playingMsgModel.status.get()==1||playingMsgModel.status.get()==0) {
                stopAnimingUnSendMsg(mflBg);
            }
            else {
                startAnimingUnSendMsg(mflBg);
            }
        }
    }

    private void setChatPhotoScale(float x2, long time, Animator.AnimatorListener listener) {
        if (this.mIvChatPhoto != null)
            AnimationUtil.startScaleXAndY(this.mIvChatPhoto, x2, x2, time,listener);
    }

    private void setFlLenbg(FrameLayout flLen, MsgModel msgModel) {
        if (flLen != null && msgModel != null) {
            if (DEBUG) {
                Log.e(TAG, "setFlLenbg:" + msgModel.id + ",msgModel:" + msgModel.isRead.get());
            }
            flLen.setBackgroundDrawable(CommonUtils.getShape(activity, msgModel.isRead.get() ? R.drawable.shape_chat_readed_bg : R.drawable.shape_chat_bg));
        }
    }

    private void setFlLenbg2() {
        if (playViewHolder != null && mflBg != null) {
            boolean visibleItemCountRange = CommonUtils.isVisibleItemCountRange(mRecycleView, playPos);
            if (visibleItemCountRange) {
                if (mflBg != null && playingMsgModel != null) {
                    if (DEBUG) {
                        Log.e(TAG, "setFlLenbg2:" + playingMsgModel.id + ",msgModel:" + playingMsgModel.isRead.get());
                    }
                    mflBg.setBackgroundDrawable(CommonUtils.getShape(activity, playingMsgModel.isRead.get() ? R.drawable.shape_chat_readed_bg : R.drawable.shape_chat_bg));
                }
            }
        }
    }

    private void stopAnim() {
        if (this.mHandler != null)
            this.mHandler.removeMessages(MSG_SHOW_ANIM_CIRCLE_2);
        if (this.mSosAlarmAnim1 != null)
            this.mSosAlarmAnim1.cancel();
        if (this.mSosAlarmAnim2 != null)
            this.mSosAlarmAnim2.cancel();
        if (this.mSosAlarmCircle1 != null) {
            this.mSosAlarmCircle1.setScaleX(1.0F);
            this.mSosAlarmCircle1.setScaleY(1.0F);
        }
        if (this.mSosAlarmCircle1 != null)
            this.mSosAlarmCircle1.setVisibility(View.GONE);
        if (this.mSosAlarmCircle2 != null)
            this.mSosAlarmCircle2.setVisibility(View.GONE);
    }

    /**
     * @param isKeepScreenOn
     */
    private void stopVoice(boolean isKeepScreenOn) {
        if (DEBUG) {
            Log.e(TAG, "stopVoice:isKeepScreenOn:" + isKeepScreenOn);
        }
        if (!isKeepScreenOn) {//屏幕都息 说明播放完成
            lastNeedPlayMsgId = null;
        }
        if (mediaHelper != null) {
            mediaHelper.stopPlay();
        }
    }


    public void add(MsgModel newMsgModel, int pos) {
        if (DEBUG) {
            Log.e(TAG, "add :position" + pos + ",needHoldSize:" + this.needHoldSize + "，msgModels.size()：" + this.msgModels.size());
        }
        if (this.needHoldSize <= 0) {
            this.msgModels.add(pos, newMsgModel);
            notifyItemInserted(pos);
        }
        else {
            LinkedList<MsgModel> preAddCacheMsgList = new LinkedList<>();
            needHoldSize--;
            this.msgModels.remove(needHoldSize);
            if (preAddCacheMsgList.isEmpty()) {
                this.msgModels.add(needHoldSize, newMsgModel);
                notifyItemChanged(needHoldSize);
            }
            else {
                //存在预发送的消息，需要将预发送的消息放在最底部
                MsgModel lastPreMsgModel = preAddCacheMsgList.getLast();
                int indexOf = msgModels.indexOf(lastPreMsgModel);
                msgModels.add(indexOf + 1, newMsgModel);
                notifyDataSetChanged();
            }
        }
        //处于后台 设置 lastNeedPlayMsgId
        if (newMsgModel != null) {
            lastNeedPlayMsgId = newMsgModel.id;
        }
    }

    public void addAll(List<MsgModel> newList, int position, boolean isClearOld) {
        if (isClearOld) {
            this.msgModels = newList;
            filterFulFilPreconertedSize(this.msgModels);
            notifyDataSetChanged();
        }
        else {
            int size = newList.size();
            if (this.needHoldSize <= 0) {
                this.msgModels.addAll(position, newList);
                notifyItemRangeInserted(position, size);
            }
            else {
                int needRemoveIndex = needHoldSize > size ? size : needHoldSize;
                for (int index = 1; index <= needRemoveIndex; index++) {
                    this.needHoldSize--;
                    this.msgModels.remove(0);
                }
                LinkedList<MsgModel> preAddCacheMsgList =  new LinkedList<>();
                if (preAddCacheMsgList.isEmpty()) {
                    msgModels.addAll(needHoldSize, newList);
                }
                else {
                    //存在预发送的消息，需要将预发送的消息放在最底部
                    MsgModel lastPreMsgModel = preAddCacheMsgList.getLast();
                    int indexOf = msgModels.indexOf(lastPreMsgModel);
                    msgModels.addAll(indexOf + 1, newList);
                }
                notifyDataSetChanged();
            }
        }
    }


    public void calChatBgSize2(float paramFloat, MyFrameLayout paramMyFrameLayout) {
//        paramMyFrameLayout.setLayoutParams(calChatBgSize(paramFloat, paramMyFrameLayout));
    }

    public void canMyFrameScroll(boolean paramBoolean) {
        this.isMyFrameCanScroll = paramBoolean;
    }

    public int getItemCount() {
        if (this.msgModels == null)
            return 0;
        return this.msgModels.size();
    }

    public List<MsgModel> getUseFulData() {
        return filterAddPosModel();
    }

    /**
     * 返回除去虚拟占位的list
     *
     * @return
     */
    public List<MsgModel> filterAddPosModel() {
        int size = msgModels.size();
        if (size > AppConfig.EVENT_CARD_SHOW_CHAT_SIZE) {
            return msgModels;
        }
        else {
            return msgModels.subList(needHoldSize, size);
        }
    }

    public void initPhotoAnim() {
        this.mSosAlarmAnim1 = new ValueAnimator();
        this.mSosAlarmAnim1.setFloatValues(1.0F, END_ALARM_CIRCLE_SCALE);
        this.mSosAlarmAnim1.setInterpolator(new DecelerateInterpolator());
        this.mSosAlarmAnim1.setDuration(1000L);
        this.mSosAlarmAnim1.setRepeatCount(2147483647);
        this.mSosAlarmAnim1.setRepeatMode(ValueAnimator.RESTART);
        this.mSosAlarmAnim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @SuppressLint({"NewApi"})
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float f = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                int i = (int) (255.0F - (f - 1.0F) * 255.0F / 1.1F);
                if (ChatListRecycleViewAdapter.this.mSosAlarmCircle1 != null) {
                    ChatListRecycleViewAdapter.this.mSosAlarmCircle1.setScaleX(f);
                    ChatListRecycleViewAdapter.this.mSosAlarmCircle1.setScaleY(f);
                    ChatListRecycleViewAdapter.this.mSosAlarmCircle1.setImageAlpha(i);
                }
                else {
                    return;
                }
                ChatListRecycleViewAdapter.this.mSosAlarmCircle1.setAlpha(i);
            }
        });
        this.mSosAlarmAnim2 = new ValueAnimator();
        this.mSosAlarmAnim2.setFloatValues(1.0F, 2.1F);
        this.mSosAlarmAnim2.setInterpolator(new DecelerateInterpolator());
        this.mSosAlarmAnim2.setDuration(1000L);
        this.mSosAlarmAnim2.setRepeatCount(Integer.MAX_VALUE);
        this.mSosAlarmAnim2.setRepeatMode(ValueAnimator.RESTART);
        this.mSosAlarmAnim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @SuppressLint({"NewApi"})
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float f = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                int i = (int) (255.0F - (f - 1.0F) * 255.0F / 1.1F + 100.0F);
                if (ChatListRecycleViewAdapter.this.mSosAlarmCircle2 != null) {
                    ChatListRecycleViewAdapter.this.mSosAlarmCircle2.setScaleX(f);
                    ChatListRecycleViewAdapter.this.mSosAlarmCircle2.setScaleY(f);
                    ChatListRecycleViewAdapter.this.mSosAlarmCircle2.setImageAlpha(i);
                }
                else {
                    return;
                }
                ChatListRecycleViewAdapter.this.mSosAlarmCircle2.setAlpha(i);
            }
        });
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder paramViewHolder, final int position) {
        final ChatViewHolder viewHolder = (ChatViewHolder) paramViewHolder;
        ItemChatBinding itemChatBinding = viewHolder.itemChatBinding;
        final MsgModel msgModel = msgModels.get(position);
        itemChatBinding.setModel(msgModel);
        itemChatBinding.setViewmodel(new ChatItemViewModel(itemChatBinding,msgModel,activity,this));
        itemChatBinding.executePendingBindings();
//            if (playingMsgModel != null) {
//                if (msgModel.id == playingMsgModel.id) {
//                    setAnimView(viewHolder);
//                }
//                else if (playViewHolder != null && playViewHolder == viewHolder) {//存在复用的
//                    stopAnim();
//                    setChatPhotoScale(1.0F, 0,null);
//                    if (DEBUG) {
//                        Log.e(TAG, "存在复用的:");
//                    }
//                    setFlLenbg(mflBg, msgModel);
//                }
//            }
//            else {
//                mflLen = null;
//            }
    }

    public void startAnimingUnSendMsg(FrameLayout flLen) {
        if(flLen==null){
            return;
        }
        ObjectAnimator anim = (ObjectAnimator) flLen.getTag(R.id.send_status_view_id);
        if(anim!=null){
            anim.start();
        }else{
            anim = ObjectAnimator.ofFloat(flLen, "alpha", 1f, 0.1f);
            anim.setDuration(500);// 动画持续时间
            anim.setRepeatCount(Integer.MAX_VALUE);
            anim.start();
            flLen.setTag(R.id.send_status_view_id, anim);
        }
    }

    public void stopAnimingUnSendMsg(FrameLayout flLen) {
        if(flLen==null){return;}
        flLen.setAlpha(1f);
        ObjectAnimator anim = (ObjectAnimator) flLen.getTag(R.id.send_status_view_id);
        if(anim!=null){
            anim.cancel();
        }
        if (DEBUG) {
            Log.e(TAG, "stopAnimingUnSendMsg:flLen is null:" +(flLen == null)+",anim is null："+(anim==null));
        }
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt) {
        return new ChatViewHolder(View.inflate(paramViewGroup.getContext(), R.layout.item_chat, null));
    }

    public void playVoice(final MsgModel willPlayModel, final ChatViewHolder willPlayViewHolder, int position) {
        if (DEBUG) {
            Log.e(TAG, "playVoice:" + willPlayModel.id + ",willPlayModel:" + willPlayModel + ",playingMsgModel:" + playingMsgModel);
        }
        if (willPlayModel == playingMsgModel) {//如果正在播放 则清除
            clearAnimViewVoice();
        }
        else {
            stopVoice(true);
            stopAnimAndView();
            if (this.mediaHelper == null)
                this.mediaHelper = MediaHelper.getManager();
            String directory = this.playVoiceBaseUrl + this.EventId;
            String fileName = willPlayModel.id + AppConfig.MEDIA_SUFFIX;
            File playFile = new File(directory, fileName);
            if ((playFile.exists()) && (playFile.isFile())) { //如果缓存中 存在
                playViewUpdateDb(willPlayModel, willPlayViewHolder, playFile, position);
            }
            else { //不存在
                try {
                    FileUtil.makeSurePathExists(directory);
                    String voiceContentStr = willPlayModel.getVoiceContentStr();
                    String voiceDownLoadUrl = willPlayModel.getVoiceDownLoadUrl();
                    String content = null;
                    if (voiceContentStr != null && !voiceContentStr.equals("")) {
                        content = voiceContentStr;
                    }
                    if (voiceDownLoadUrl != null && voiceDownLoadUrl.equals("") && !voiceDownLoadUrl.toLowerCase().startsWith(AppConfig.PLAY_VOICE_START_NAME)) {
                        content = voiceDownLoadUrl;
                    }
                    if (content != null) {
                        try {
                            if (CommonUtils.writeByteToFile(NameChangeUtils.decode(willPlayModel.getVoiceContentStr()), playFile)) {
                                playViewUpdateDb(willPlayModel, willPlayViewHolder, playFile, position);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            startDownLoadVoiceFile(willPlayModel, willPlayViewHolder, directory, fileName, position);
                        }
                    }
                    else {//网络下载
                        startDownLoadVoiceFile(willPlayModel, willPlayViewHolder, directory, fileName, position);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startDownLoadVoiceFile(final MsgModel willPlayModel, final ChatViewHolder paramChatViewHolder, String directory, String fileName, final int position) {
    }

    private void playViewUpdateDb(MsgModel playingMsgModel, ChatViewHolder paramChatViewHolder, File playFile, int position) {
        playPos = position;
        playViewAndVoice(playingMsgModel, paramChatViewHolder, playFile);
        if (playingMsgModel != null && !playingMsgModel.isRead.get()) {
            playingMsgModel.isRead.set(true);
        }
    }

    public boolean rePlaceModel(MsgModel oldModel, MsgModel msgModel) {
        if (DEBUG) {
            Log.e(TAG, "rePlaceModel:"+oldModel.getId()+":" +msgModels.get(0).getId()+","+msgModels.get(1).getId());
        }
        int index = msgModels.indexOf(oldModel);
        if (index != -1) {
            msgModels.remove(index);
            msgModels.add(index, msgModel);
            notifyItemChanged(index);
        }
        if (DEBUG)
            Log.e(TAG, "rePlaceModel msgModel:index:" +index+","+msgModel.getId()+","+msgModel.getCreateDate()+",msgModel:"+msgModel.getIdempotentId());
        return index!=-1;
    }

    public void remove(int pos) {
        if (pos == -1) {
            return;
        }
        if (DEBUG) {
            Log.e(TAG, "remove:" + this.msgModels.size());
        }
        if (this.msgModels.size() <= AppConfig.EVENT_CARD_SHOW_CHAT_SIZE) {
            this.msgModels.remove(pos);
            addAHoldMsg(this.msgModels);
            notifyDataSetChanged();
            this.needHoldSize += 1;
        }
        else {
            this.msgModels.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void clearAnimViewVoice() {
        clearAnimViewVoice(false);
    }

    public void clearAnimViewVoice(boolean isKeepScreenOn) {
        if (DEBUG) {
            Log.e(TAG, "clearAnimViewVoice:isKeepScreenOn：" + isKeepScreenOn);
        }
        stopVoice(isKeepScreenOn);
        stopAnimAndView();
        this.mflLen = null;
        this.mSosAlarmCircle1 = null;
        this.mSosAlarmCircle2 = null;
        this.mSosAlarmAnim1 = null;
        this.mSosAlarmAnim2 = null;
        this.playingMsgModel = null;
        this.playViewHolder = null;
    }

    public void clearAllSound() {
        clearSequentPlay();
        clearAnimViewVoice();
    }

    private void clearSequentPlay() {
        removeAutoRunnable();
        lastNeedPlayMsgId = null;
    }

    private void removeAutoRunnable() {
        if (nextAutoRunnable != null) {
            if (DEBUG) {
                Log.e(TAG, "indexOf :remove");
            }
            mRecycleView.removeCallbacks(nextAutoRunnable);
        }
    }

    public void resetData(List<MsgModel> paramList) {
        this.msgModels = paramList;
        filterFulFilPreconertedSize(this.msgModels);
        notifyDataSetChanged();
    }

    void setAnimView(ChatViewHolder viewHolder) {
        if(viewHolder==null){return;}
        setAnimAndView(viewHolder);
        initPhotoAnim();
        startPhotoView();
        startPhotoAnim();
        stopAnimingUnSendMsg(mflBg);
    }

    public void setItemViewClickListener(ItemViewClickListener paramItemViewClickListener) {
        this.itemViewClickListener = paramItemViewClickListener;
    }

    public void showTvName(boolean paramBoolean) {
        this.isShowName = paramBoolean;
    }

    public int indexOf(MsgModel remove) {
        int indexOf = msgModels.indexOf(remove);
        return indexOf;
    }

    public void notifyItem(MsgModel msgModel) {
        int indexOf = msgModels.indexOf(msgModel);
        if(indexOf!=-1){
            notifyItemChanged(indexOf);
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ItemChatBinding itemChatBinding;
//        public ImageView ivChatPhoto;
//        @BindView(R.id.iv_like)
//        public ImageView ivLike;
//        @BindView(R.id.iv_anim1)
//        public ImageView ivAnim1;
//        @BindView(R.id.iv_anim2)
//        public ImageView ivAnim2;
//        @BindView(R.id.tv_name)
//        public TextView tvName;
//        @BindView(R.id.fl_len)
//        public MyFrameLayout flLen;
//        @BindView(R.id.fl_bg)
//        public FrameLayout flBg;
//        @BindView(R.id.rl_root)
//        public RelativeLayout rlRoot;

        public ChatViewHolder(View view) {
            super(view);
             itemChatBinding = ItemChatBinding.bind(view);
        }
    }

    public interface ItemViewClickListener {
        void msgBarClick(int paramInt);

        boolean photoClick(int paramInt, MsgModel paramMsgModel);
    }
}