package com.example.administrator.app;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import utils.AnimationUtil;
import utils.AppConfig;
import utils.CommonUtils;
import widget.MyFrameLayout;

/**
 * Created by Administrator on 2017/5/9.
 */
@SuppressWarnings("unchecked")
public class ChatBindings {
    public static final String TAG = "ChatBindings";

    @BindingAdapter("app:msgStatus")
    public static void setMsgStatus(MyFrameLayout fl, byte status) {
        Log.e(TAG, "setStatus:status" + status);
        //    public byte status=1;//0 presend ,1 success ,2 sending, 3 playing
        if (status==2) {
            startAnimingUnSendMsg((FrameLayout) fl.findViewById(R.id.fl_bg));
        }
        else {
            stopAnimingUnSendMsg((FrameLayout) fl.findViewById(R.id.fl_bg));
        }
        View ivPhoto = fl.findViewById(R.id.iv_chat_photo);
        if (status==3) {
            setChatPhotoScale(1.2F,200l,null,ivPhoto);
        }else{
            setChatPhotoScale(1.0F, 10l, null, ivPhoto);
        }
    }

    private static  void setChatPhotoScale(float x2, long time, Animator.AnimatorListener listener, View ivPhoto) {
        if (ivPhoto != null)
            AnimationUtil.startScaleXAndY(ivPhoto, x2, x2, time, listener);
    }

    @BindingAdapter("app:msgPlayTime")
    public static void updateMsgPlayTime(MyFrameLayout fl, float time) {
        Log.e(TAG, "setMsgPlayTime:time" + time);
        ViewGroup.LayoutParams layoutParams = fl.getLayoutParams();
        time = CommonUtils.calVoiceLen(AppConfig.screenWidth, time, AppConfig.PLAY_VOICE_MOST_TIME);
        int j = CommonUtils.dp2px(AppConfig.SHORTEST_PHOTO_WIDTH * time);
        if (j % 2 != 0)
            j--;
        layoutParams.width = j;
//        fl.invalidate();
        fl.setLayoutParams(layoutParams);
    }

    public static void startAnimingUnSendMsg(FrameLayout flLen) {
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

    public static void stopAnimingUnSendMsg(FrameLayout flLen) {
        if(flLen==null){return;}
        flLen.setAlpha(1f);
        ObjectAnimator anim = (ObjectAnimator) flLen.getTag(R.id.send_status_view_id);
        if(anim!=null){
            anim.cancel();
        }
        Log.e(TAG, "stopAnimingUnSendMsg:flLen is null:" +(flLen == null)+",anim is null："+(anim==null));
    }

}
