package utils;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.View;

/**
 * Created by Administrator on 2017/2/7.
 */
public class BgChangeUtil {
    ArgbEvaluator mEvaluator;
    ValueAnimator mChangeColorAnimator;
    View mView;
    int[] mColorArr;
    int mColorNum = 1;
    int mLastColor;

    public void init(View view, int[] colorArr) {
        mView = view;
        mColorArr = colorArr;
        mEvaluator = new ArgbEvaluator();
        mLastColor = mColorArr[0];
    }

    public void changeBg(final Runnable animationStartListener) {
        ///开始执行
        if (mChangeColorAnimator == null) {
            mChangeColorAnimator = ValueAnimator.ofFloat(0f, 1f);
            mChangeColorAnimator.setDuration(AppConfig.VOICE_PLAY_SCALE_TIME);
            mChangeColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float changeV = (float) animation.getAnimatedValue();
                    //获取当前colorNum所对应的 一双过渡 终值
                    int color = mColorArr[mColorNum];
                    //计算出过渡值
                    int evaluateColor = (Integer) mEvaluator.evaluate(changeV, mLastColor, color);//参数1变化百分比，参数2
                    mView.setBackground(AnimationUtil.getShapeCircle(evaluateColor));
                    mLastColor = evaluateColor;
                }
            });
            mChangeColorAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                    if (mColorNum == mColorArr.length - 1) {
                        mColorNum = 0;
                    } else {
                        mColorNum++;
                    }
                    if(animationStartListener!=null){
                        animationStartListener.run();
                    }
                }
            });
            mChangeColorAnimator.setRepeatCount(1000);
        }
        mChangeColorAnimator.start();
        if(animationStartListener!=null){
            animationStartListener.run();
        }
    }

    public void cancelAnim() {
        mChangeColorAnimator.cancel();
    }
}
