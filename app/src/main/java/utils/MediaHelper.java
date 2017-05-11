package utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.example.administrator.app.AppApplication;

public class MediaHelper {
    private static final String TAG = "MediaRecorderHelper";
    /**
     * 外放模式
     */
    public static final int MODE_SPEAKER = 0;

    /**
     * 耳机模式
     */
    public static final int MODE_HEADSET = 1;

    /**
     * 听筒模式
     */
    public static final int MODE_EARPIECE = 2;

    private static MediaHelper playerManager;
    private AudioManager audioManager;
    private MediaPlayer mMediaPlayer;
    private Context context;
    private boolean isPause = false;
    private int currentMode = MODE_SPEAKER;

    public static MediaHelper getManager() {
        if (playerManager == null) {
            synchronized (MediaHelper.class) {
                playerManager = new MediaHelper();
            }
        }
        return playerManager;
    }

    private MediaHelper() {
        this.context = AppApplication.sContext;
        initMediaPlayer();
        initAudioManager();
    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * 初始化音频管理器
     */
    private void initAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
        else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(true);            //默认为扬声器播放
    }

    public boolean isPause() {
        return isPause;
    }

    public void pause() {
        if (isPlaying()) {
            isPause = true;
            mMediaPlayer.pause();
        }
    }

    public void resume() {
        if (isPause) {
            isPause = false;
            mMediaPlayer.start();
        }
    }

    /**
     * 获取当前播放模式
     *
     * @return
     */
    public int getCurrentMode() {
        return currentMode;
    }

    /**
     * 切换到听筒模式
     */
    public void changeToEarpieceMode() {
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION), AudioManager.FX_KEY_CLICK);
        }
        else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL), AudioManager.FX_KEY_CLICK);
        }
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode() {
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode() {
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }

    public void resetPlayMode() {
        if (audioManager.isWiredHeadsetOn()) {
            changeToHeadsetMode();
        }
        else {
            changeToSpeakerMode();
        }
    }

    /**
     * 调大音量
     */
    public void raiseVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 调小音量
     */
    public void lowerVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }


    /**
     * 是否正在播放
     *
     * @return 正在播放返回true, 否则返回false
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void play(int resId, OnCompletionListener onCompletionListener) {
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
        playRealSound(uri, onCompletionListener, null, null);
    }

    public void stopPlay() {
        try {
            if (isPlaying()) {
                try {
                    mMediaPlayer.stop();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        } catch (IllegalStateException e) {
            if (AppConfig.DEBUG) {
                Log.e(TAG, "stopPlay :IllegalStateException");
            }
            e.printStackTrace();
        }
    }

    private void releaseMedia() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }


    public void playSound(String absolutePath, OnCompletionListener onCompletionListener, MediaPlayer.OnErrorListener onErrorListener, MediaPlayer.OnPreparedListener onPreparedListener) {
        playRealSound(Uri.parse(absolutePath), onCompletionListener, onErrorListener, onPreparedListener);
    }

    public void playRealSound(Uri uri, OnCompletionListener onCompletionListener, MediaPlayer.OnErrorListener onErrorListener, MediaPlayer.OnPreparedListener onPreparedListener) {
        int result = audioManager.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(context, uri);
                mMediaPlayer.prepareAsync();
                if (onPreparedListener != null) {
                    mMediaPlayer.setOnPreparedListener(onPreparedListener);
                }
                else {
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                }
                if (onCompletionListener != null) {
                    mMediaPlayer.setOnCompletionListener(onCompletionListener);
                }
                else {
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            resetPlayMode();
                        }
                    });
                }
                if (onErrorListener != null) {
                    mMediaPlayer.setOnErrorListener(onErrorListener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            // Lower the volume while ducking.
//                            mediaPlayer.setVolume(0.2f, 0.2f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
//                            pause();
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS):
                            stopPlayVoice();
                            break;
                        case (AudioManager.AUDIOFOCUS_GAIN):
                            // Return the volume to normal and resume if paused.
//                            mediaPlayer.setVolume(1f, 1f);
//                            mediaPlayer.start();
                            break;
                        default:
                            break;
                    }
                }
            };

    private void stopPlayVoice() {
        context.sendBroadcast(new Intent(STOP_PLAY_VOICE));
    }

    public static final String STOP_PLAY_VOICE="stop_play_voice";
}
