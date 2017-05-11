package utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AnimationUtil {

    public static void viewFadeIn(long duration, final boolean isFadeIn, final View view, final ViewAnimNextListener listener) {
        if (view == null) {
            return;
        }
        ViewPropertyAnimator animate = view.animate();
        animate.setDuration(duration);
        animate.alpha(isFadeIn ? 1 : 0);
        animate.setListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (listener != null) {
                    listener.next();
                }
            }
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
            }
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {
            }
        });
        animate.start();

    }

    public static void eventViewScale(View tvTitle, View hideUpView, final View view, boolean isAtOnceInChat, final View needShowView, final ViewAnimNextListener listener, int width) {
        if (view != null) {
            ViewPropertyAnimator animate = view.animate();
            animate.scaleX(1.25f);
            animate.scaleY(1.2f);
            animate.setDuration(isAtOnceInChat ? 10 : 1000);
            animate.alpha(0);
            animate.start();
        }
        if (hideUpView != null) {
            ViewPropertyAnimator animate = hideUpView.animate();
            animate.scaleX(1.25f);
            animate.scaleY(1.4f);
            animate.setDuration(isAtOnceInChat ? 10 : 1000);
            animate.alpha(0);
            animate.start();
        }
        if (tvTitle != null) {
            ViewPropertyAnimator animate = tvTitle.animate();
            animate.setDuration(isAtOnceInChat ? 10 : 1200);
            animate.alpha(0);
            animate.start();
        }
        if (needShowView != null) {
            PropertyValuesHolder[] propertyValuesHolders = {PropertyValuesHolder.ofFloat("alpha", 1.0F, 1.0F)
                    , PropertyValuesHolder.ofFloat("scaleX", 1f, AppConfig.SelectedRvScale)
                    , PropertyValuesHolder.ofFloat("scaleY", 1.0F, AppConfig.SelectedRvScale)};
            ObjectAnimator localObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(needShowView, propertyValuesHolders);
            localObjectAnimator.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator paramAnonymousAnimator) {
                }
                public void onAnimationEnd(Animator paramAnonymousAnimator) {
                    if (listener != null) {
                        listener.next();
                    }
                }
                public void onAnimationRepeat(Animator paramAnonymousAnimator) {
                }
                public void onAnimationStart(Animator paramAnonymousAnimator) {
                }
            });
            localObjectAnimator.setDuration(isAtOnceInChat ? 10 : 1200);
            localObjectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            localObjectAnimator.start();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) needShowView.getLayoutParams();
            params.width = width;
            needShowView.requestLayout();
            restoreRecycleView(isAtOnceInChat, needShowView);
        }
    }

    public static void restoreRecycleView(boolean isAtOnceInChat, final View mSelectedRecycleView) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mSelectedRecycleView.getLayoutParams();
        int topMargin0 = layoutParams.topMargin;
        int topMargin1 = (int) AppConfig.RV_SCROLL_TOP;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(topMargin0, topMargin1);
        valueAnimator.setDuration(isAtOnceInChat ? 10 : 1500);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer animatedValue = (Integer) animation.getAnimatedValue();
                CommonUtils.setTopBtmMargins(mSelectedRecycleView, animatedValue, -1);
            }
        });
        valueAnimator.start();
        int bottomMargin0 = layoutParams.bottomMargin;
        int bottomMargin1 =  (bottomMargin0 + CommonUtils.dp2px(50.0F));
        ValueAnimator valueAnimator2 = ValueAnimator.ofInt(bottomMargin0, bottomMargin1);
        valueAnimator2.setDuration(isAtOnceInChat ? 10 : 1500);
        valueAnimator2.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer animatedValue = (Integer) animation.getAnimatedValue();
                CommonUtils.setTopBtmMargins(mSelectedRecycleView, -1, animatedValue);
            }
        });
        valueAnimator2.start();
    }

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

    public static void eventViewScale2(View swipeRefresh) {
        if (swipeRefresh != null) {
            ViewPropertyAnimator animate1 = swipeRefresh.animate();
            animate1.scaleX(AppConfig.SelectedRvScale);
            animate1.scaleY(AppConfig.SelectedRvScale);
            animate1.setDuration(10);
            animate1.setInterpolator(new AccelerateDecelerateInterpolator());
            animate1.alpha(1);
            animate1.start();
        }
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
