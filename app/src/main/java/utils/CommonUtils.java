package utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.app.AppApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CommonUtils {
    private static final String TAG = "CommonUtils";

    public static String getFeedbackSupportInfo() {
        //手机厂商
        String phoneFactory = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(phoneFactory)) {
            phoneFactory = phoneFactory.replace("&", "");
        }
        //手机型号
        String model = Build.MODEL;
        if (!TextUtils.isEmpty(model)) {
            model = model.replace("&", "");
        }
        StringBuilder feedbackStr = new StringBuilder();
        feedbackStr.append("Phone Manufacturer: " + phoneFactory).append(" Phone Model: " + model).append(" OS Version: " + Build.VERSION.RELEASE);
        return feedbackStr.toString();
    }


    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static void setTopBtmMargins(View v, int t, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (t != -1)
                p.topMargin = t;
            if (b != -1)
                p.bottomMargin = b;
            v.requestLayout();
        }
    }

    public static Drawable getShape(Activity act, int resid) {
        return act.getResources().getDrawable(resid);
    }

    public static int[] getWwithAndHheight(Activity act) {
        int[] arr = {0, 0};
        if (act == null) {
            return arr;
        }
        DisplayMetrics outMetrics = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        arr[0] = outMetrics.widthPixels;
        arr[1] = outMetrics.heightPixels;
        return arr;
    }

    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int dp2px(float dipValue) {
        final float scale = AppApplication.sContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    public static boolean writeByteToFile(byte[] data, File file) {
        boolean isSuccess = false;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }


    public static byte[] readFielToByteArr(String path) {
        byte[] buffer = null;
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    public static float calVoiceLen(int wwidth, float playTime, int playVoiceMostTime) {
        float len = 0f;
        //播放时长边界
        if (playVoiceMostTime < playTime) {
            playTime = playVoiceMostTime;
        }
        if (playTime < 0) {
            playTime = 1;
        }
        //计算当前时长所占长度
        len = wwidth - (dp2px(AppApplication.sContext, 50));//总长度
        //所显示语音条最短
        float shortPhotoWidth = dp2px(AppApplication.sContext, AppConfig.SHORTEST_PHOTO_WIDTH);
        float maxScale = len / shortPhotoWidth - 0.8f;
        float scale = (playTime / playVoiceMostTime) * (maxScale - 1) + 1;
        return scale > maxScale ? maxScale : scale;
    }


    public static void runMainAction(Activity activity, Runnable runnable) {
        activity.runOnUiThread(runnable);
    }

    public static String getVersionName(Context paramContext) {
        PackageManager packageManager = paramContext.getPackageManager();
        try {
            String str = packageManager.getPackageInfo(paramContext.getPackageName(), 0).packageName;
            return str;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return "";
    }

    public static boolean isVisitedItemCountRange(RecyclerView paramRecyclerView, int paramInt) {
        int i = ((GridLayoutManager) paramRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        return i <= paramInt - 1;
    }

    public static boolean isVisibleItemCountRange(RecyclerView paramRecyclerView, int index) {
        int firstVisibleItemPosition = ((GridLayoutManager) paramRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int lastVisibleItemPosition = ((GridLayoutManager) paramRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        return firstVisibleItemPosition <= index && index <= lastVisibleItemPosition;
    }

    public static int getMsgWidth(float time) {
        time = CommonUtils.calVoiceLen(AppConfig.screenWidth, time, AppConfig.PLAY_VOICE_MOST_TIME);
        int j = CommonUtils.dp2px(AppConfig.SHORTEST_PHOTO_WIDTH * time);
        if (j % 2 != 0)
            j--;
//        AppApplication.sContext.getResources().getDimension()
        return j;
    }
}
