package recordlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.administrator.app.ChatViewModel;
import com.example.administrator.app.EventListActivity;
import com.example.administrator.app.R;

import utils.AnimationUtil;
import utils.AppConfig;
import utils.BgChangeUtil;


@SuppressWarnings("ALL")
public class RecorderButton extends Button implements AudioRecordManager.AudioStateListener {
    int[] colorArr;

    private static final String TAG = "AudioRecorderButton";
    private static final int DISTANCE_CANCEL = 50;
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    private int mCurState = STATE_NORMAL;
    private boolean isRecording = false; // 已经开始录音
    private AudioRecordManager mAudioRecordManager;
    private float mTime;
    // 是否触发longclick
    private boolean mReady;
    private boolean isScaleBig = false;
    private String str_recorder_normal;
    private String str_recorder_recording;
    private String str_recorder_want_cancel;
    private int bg_recorder_normal;
    private int bg_recorder_recording;
    private int bg_recorder_cancel;
    private float max_record_time;
    private float min_record_time;
    private int max_voice_level;
    private MediaPlayer mMediaPlayer;
    private Context mCtx;
    public EventListActivity activity;
    MediaPlayer mediaPlayer;
    BgChangeUtil bgChangeUtil;

    public RecorderButton(Context context) {
        this(context, null, 0);
    }

    public RecorderButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RecorderButton);

        mCtx = context;

        str_recorder_normal = a.getString(R.styleable.RecorderButton_txt_normal);
        str_recorder_recording = a.getString(R.styleable.RecorderButton_txt_recording);
        str_recorder_want_cancel = a.getString(R.styleable.RecorderButton_txt_want_cancel);

        bg_recorder_normal = a.getResourceId(R.styleable.RecorderButton_bg_normal, 0);
        bg_recorder_recording = a.getResourceId(R.styleable.RecorderButton_bg_recording, 0);
        bg_recorder_cancel = a.getResourceId(R.styleable.RecorderButton_bg_want_cancel, 0);

        //最大录音时间，默认为15秒
        //最小录音时间，默认为10秒
        max_record_time = a.getFloat(R.styleable.RecorderButton_max_record_time, AppConfig.MAX_RECORD_TIME);
        min_record_time = a.getFloat(R.styleable.RecorderButton_min_record_time, 1);
        max_voice_level = a.getInt(R.styleable.RecorderButton_max_voice_level, 5);
        a.recycle();
        setText(str_recorder_normal);
        setBackgroundResource(bg_recorder_normal);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (activity != null ) {
                    try {
                        if (mListener != null) {
                            mListener.onLongClick(v);
                        }
                        startColorAnim(new Runnable() {
                            @Override
                            public void run() {
                                if (!isScaleBig) {
                                    AnimationUtil.startScaleXAndY(RecorderButton.this, 1.2f, 1.2f, AppConfig.VOICE_PLAY_SCALE_TIME);
                                }
                                else {
                                    AnimationUtil.startScaleXAndY(RecorderButton.this, 1f, 1f, AppConfig.VOICE_PLAY_SCALE_TIME);
                                }
                                isScaleBig = !isScaleBig;
                            }
                        });

                        VibratorUtils.vibrate(mCtx, 60);
                        changeState(STATE_RECORDING);
                        mediaPlayer = MediaPlayer.create(mCtx, R.raw.fx);
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mHandler.sendEmptyMessageDelayed(MSG_PRE_PLAY_FINISH, 200);
                            }
                        });
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mReady = false;
                    }
                }
                return false;
            }
        });
    }

    private void startColorAnim(Runnable exeRunnable) {
        colorArr = new int[]{getContext().getResources().getColor(R.color.app_blue)
                , getContext().getResources().getColor(R.color.chat_btn_bg2)
                , getContext().getResources().getColor(R.color.chat_btn_bg3)
                , getContext().getResources().getColor(R.color.chat_btn_bg4)
                , getContext().getResources().getColor(R.color.chat_btn_bg5)};
        bgChangeUtil = new BgChangeUtil();
        bgChangeUtil.init(this, colorArr);
        bgChangeUtil.changeBg(exeRunnable);
    }

    public void initAudioRecordManager(String recordDir) {
        mAudioRecordManager = AudioRecordManager.getInstance(recordDir);
        mAudioRecordManager.setOnAudioStateListner(this);
        mAudioRecordManager.myRecordListener = mListener;
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioStateRecorderListener {
        void onFinish(float seconds, String filePath);

        void onCancel(boolean isTooShort);

        void onVoiceChange(int voiceLevel);

        void onLongClick(View view);

        void onStart(float time);

        void onUpdateTime(float currentTime, float minTime, float maxTime);

        void onReturnToRecord();

        void onReleaseMediaPlayer();

        void onWantToCancel();
    }

    private ChatViewModel.MyRecordListener mListener;

    public void setAudioStateRecorderListener(ChatViewModel.MyRecordListener listener) {
        mListener = listener;
    }

    /**
     * 获取音量大小的Runnable
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    SystemClock.sleep(200);
                    mTime += 0.2f;
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                    if (mTime >= max_record_time) {
                        //如果时间到了最大的录音时间
                        mAudioRecordManager.release();
                        mHandler.sendEmptyMessage(MSG_TIME_LIMIT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0x110;
    private static final int MSG_VOICE_CHANGE = 0x111;
    private static final int MSG_UPDATE_TIME = 0x113;
    private static final int MSG_PRE_PLAY_FINISH = 0x115;//长按 播放音乐完成 开始录音
    //最大的录音时间到了
    private static final int MSG_TIME_LIMIT = 0x114;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // audio end prepared以后开始录音
                    if (mReady) {
                        isRecording = true;
                        if (mListener != null) {
                            mListener.onStart(mTime);
                        }
                        // 开启线程，监听音量变化
                        new Thread(mGetVoiceLevelRunnable).start();
                    }
                    else {
                        changeState(STATE_NORMAL);
                    }
                    break;
                case MSG_VOICE_CHANGE:
                    if (mListener != null) {
                        //根据用户设置的最大值去获取音量级别
                        mListener.onVoiceChange(mAudioRecordManager.getVoiceLevel(max_voice_level));
                    }
                    break;
                case MSG_UPDATE_TIME:
                    if (mListener != null) {
                        mListener.onUpdateTime(mTime, min_record_time, max_record_time);
                    }
                    break;
                case MSG_PRE_PLAY_FINISH:
                    if (action != MotionEvent.ACTION_UP) {
                        releaseLastPlay();
                        releaseMediaPlay();
                        mAudioRecordManager.prepareAudio();
                        mReady = true;
                    }
                    break;
                case MSG_TIME_LIMIT:
                    if (mListener != null) {
                        //到达时间限制了
                        MediaPlayer.create(mCtx, R.raw.gj).start();
                        mListener.onFinish(mTime, mAudioRecordManager.getCurrentFilePath());
                    }
                    changeState(STATE_NORMAL);
                    reset();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void releaseLastPlay() {
        if (mListener != null) {
            mListener.onReleaseMediaPlayer();
        }
    }

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    int action;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mReady) {
                    changeState(STATE_RECORDING);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // 用户录音时间太短，取消
                handleFinishAndResetAll();
                break;
            case MotionEvent.ACTION_MOVE:
                // 根据x, y的坐标，判断是否想要取消
                if (mReady) {
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                        if (mListener != null) {
                            mListener.onWantToCancel();
                        }
                    }
                    else {
                        changeState(STATE_RECORDING);
                        if (mListener != null) {
                            mListener.onReturnToRecord();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                /**
                 * 1. 未触发onLongClick
                 * 2. prepared没有完毕已经up
                 * 3. 录音时间小于预定的值，这个值我们设置为在onLongClick之前
                 */
                if (!mReady) { // 未触发onLongClick
                    if (mAudioRecordManager != null)
                        mAudioRecordManager.cancel();
                }
                else if (!isRecording && mReady) { // 触发onlongclick 未触发recording
                    if (mAudioRecordManager != null)
                        mAudioRecordManager.cancel();
                    // 用户录音时间太短，取消
                    if (mListener != null) {
                        mListener.onCancel(true);
                    }
                }
                else if (!isRecording || mTime < min_record_time) {  // prepared没有完毕 或 录音时间过短
                    isRecording = false;
                    mAudioRecordManager.cancel();
                    // 用户录音时间太短，取消
                    if (mListener != null) {
                        MediaPlayer.create(mCtx, R.raw.fy).start();
                        mListener.onCancel(true);
                    }
                }
                else if (STATE_RECORDING == mCurState) { // 正常录制结束
                    mAudioRecordManager.release();
                    if (mListener != null) {
                        MediaPlayer.create(mCtx, R.raw.gj).start();
                        mListener.onFinish(mTime, mAudioRecordManager.getCurrentFilePath());
                    }
                }
                else if (STATE_WANT_TO_CANCEL == mCurState) { //cancel
                    mAudioRecordManager.cancel();
                    if (mListener != null) {
                        MediaPlayer.create(mCtx, R.raw.fy).start();
                        mListener.onCancel(false);
                    }
                }
                else {
                    if (mListener != null) {
                        mListener.onCancel(false);
                    }
                }
                changeState(STATE_NORMAL);
                reset();
                releaseMediaPlay();
                recoverBtn();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void noPreparedOrRecordTimeShort() {
        this.isRecording = false;
        this.mAudioRecordManager.cancel();
        if (this.mListener != null) {
            MediaPlayer.create(this.mCtx, R.raw.fy).start();
            this.mListener.onCancel(true);
        }
    }

    public void handleFinishAndResetAll() {
        if (this.mTime < this.min_record_time) {
            noPreparedOrRecordTimeShort();
            resetAll();
        }
        else if (STATE_WANT_TO_CANCEL == mCurState) { //cancel
            mAudioRecordManager.cancel();
            if (mListener != null) {
                MediaPlayer.create(mCtx, R.raw.fy).start();
                mListener.onCancel(false);
            }
            resetAll();
        }
        else if (this.isRecording) {
            this.mAudioRecordManager.release();
            MediaPlayer.create(this.mCtx, R.raw.gj).start();
            if (this.mListener != null)
                this.mListener.onFinish(this.mTime, this.mAudioRecordManager.getCurrentFilePath());
            resetAll();
        }
    }

    private void resetAll() {
        changeState(STATE_NORMAL);
        reset();
        releaseMediaPlay();
        recoverBtn();
    }

    private void recoverBtn() {
        AnimationUtil.startScaleXAndY(RecorderButton.this, 1f, 1f, 200);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mReady = false;
        mTime = 0;
        mCurState = STATE_NORMAL;
    }

    private void releaseMediaPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 根据坐标去判断是否应该取消
     *
     * @param x
     * @param y
     * @return
     */
    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -DISTANCE_CANCEL || y > getHeight() + DISTANCE_CANCEL) {
            return true;
        }
        return false;
    }

    /**
     * 按钮状态改变
     * @param state
     */
    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (mCurState) {
                case STATE_NORMAL:
                    if (bgChangeUtil != null) {
                        bgChangeUtil.cancelAnim();
                    }
                    setBackgroundResource(bg_recorder_normal);
                    setText(str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(bg_recorder_recording);
                    setText(str_recorder_recording);
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(bg_recorder_cancel);
                    setText(str_recorder_want_cancel);
                    break;
            }
        }
    }
}
