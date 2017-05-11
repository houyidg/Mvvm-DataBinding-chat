package recordlib;

import android.media.MediaRecorder;

import com.example.administrator.app.ChatViewModel;

import java.io.File;
import java.io.IOException;

/**
 */
public class AudioRecordManager {
    private static final String TAG = "AudioRecordManager";
    private MediaRecorder mMediaRecorder;
    private String mDir;
    private String mCurrentFilePath;

    public static AudioRecordManager mInstance;

    private boolean isPrepared = false;

    private AudioRecordManager(String dir) {
        mDir = dir;
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    /**
     * 回调准备完毕
     */
    public interface AudioStateListener {
        void wellPrepared();
    }

    public AudioStateListener mListener;
    public ChatViewModel.MyRecordListener myRecordListener;

    public void setOnAudioStateListner(AudioStateListener listner) {
        mListener = listner;
    }

    public static AudioRecordManager getInstance(String dir) {
        if (null == mInstance) {
            synchronized (AudioRecordManager.class) {
                if (null == mInstance) {
                    mInstance = new AudioRecordManager(dir);
                }
            }
        }
        return mInstance;
    }

    public void prepareAudio() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                init();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void init() throws IOException {
        isPrepared = false;
        File dir = new File(mDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = generateFileName();
        File file = new File(dir, fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        mCurrentFilePath = file.getAbsolutePath();
        mMediaRecorder = new MediaRecorder();
        // 设置输出文件
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        // 设置MediaRecorder的音频源为麦克风
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置音频格式 AMR_NB
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        // 设置音频的编码为AMR
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.prepare();
        mMediaRecorder.start();
        // 准备结束
        isPrepared = true;
        if (mListener != null) {
            mListener.wellPrepared();
        }
    }

    /**
     * 随机生成文件的名称
     *
     * @return
     */
    private String generateFileName() {
        String playingVoiceFileName = myRecordListener.getPlayingVoiceFileName();
        return playingVoiceFileName;
    }

    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            // mMediaRecorder.getMaxAmplitude() 1-32767
            try {
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
                // 忽略产生的异常
            }
        }
        return 1;
    }

    // TODO: 2017/4/1 优化
    public void release() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder = null;
        }
    }

    /**
     * 释放资源 同时删除音频文件
     */
    public void cancel() {
        release();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }
}