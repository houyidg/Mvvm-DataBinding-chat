package utils;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewPropertyAnimator;

public class AnimationUtil {
    public static void startScaleXAndY(View objView, float x2, float y2, long duration) {
        startScaleXAndY(objView, x2, y2, duration, null);
    }

    public static void startScaleXAndY(View objView, float x2, float y2, long duration, Animator.AnimatorListener listener) {
        ViewPropertyAnimator animate = objView.animate();
        animate.scaleY(y2);
        animate.scaleX(x2);
        if (listener != null)
            animate.setListener(listener);
        animate.setDuration(duration);
        animate.start();
    }
    public static Drawable getShapeCircle(int color) {
        int roundRadius = CommonUtils.dp2px(80); // 8dp 圆角半径
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(color);
        gd.setCornerRadius(roundRadius);
        return gd;
    }


    public interface ViewAnimNextListener {
        void next();
    }
}
