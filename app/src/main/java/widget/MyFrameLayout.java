package widget;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.example.administrator.app.R;

import utils.AppConfig;


/**
 * Created by Administrator on 2017/1/3.
 * 控制语音头像在viewgroup滑动
 */
public class MyFrameLayout extends RelativeLayout {
    private static final String TAG = "MyFrameLayout";
    private static final boolean DEBUG = AppConfig.DEBUG;

    public void setCanScroll2(boolean canScroll2) {
        this.canScroll2 = canScroll2;
    }

    public boolean canScroll2 = true;
    public boolean isCanScroll() {
        return canScroll;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    private boolean canScroll = true;

    public MyFrameLayout(Context context) {
        super(context);
        init();
    }

    public MyFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private ViewDragHelper mDragger;
    private View mDragView;
    private View mLikeView;
    private View mBlockView;
    private Point mAutoBackOriginPos = new Point();
    private OnViewSlideListener mOnViewSlideListener;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!canScroll) {
            return false;
        }
        return mDragger.shouldInterceptTouchEvent(event);
        // TODO: 2017/2/23 这里直接返回true 保证了 block user功能良好
    }

    float x0;
    float y0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canScroll) {
            return false;
        }
        if (!canScroll2) {
            return false;
        }
        mDragger.processTouchEvent(event);

        /**
         * 保证 vp内部view手指滑动出vp范围还能持续滑动view
         */
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x0 = event.getX();
                y0 = event.getY();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                float dx = Math.abs(x - x0);
                float dy = Math.abs(y - y0);
                if (dx > dy && dy<slopValue) {
                    getParent().requestDisallowInterceptTouchEvent(true);//请求VP不拦截子View
                    return true;
                }
                y0 = y;
                x0 = x;
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mAutoBackOriginPos.x = mDragView.getLeft();
        mAutoBackOriginPos.y = mDragView.getTop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mDragView = findViewById(R.id.iv_chat_photo);//flBg
        mLikeView = findViewById(R.id.iv_like);
        mBlockView = findViewById(R.id.iv_block);
    }

    int slopValue = 0;

    /**
     * 初始化
     */
    public void init() {
        slopValue = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mDragger = ViewDragHelper.create(this, 1f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                //mEdgeTrackerView禁止直接移动
                return mDragView == child;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - mDragView.getWidth();
                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                if (mOnViewSlideListener != null) {
                    mOnViewSlideListener.onSliding();
                }
                return newLeft;
            }

            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
//              //mAutoBackView手指释放时可以自动回去
                if (releasedChild == mDragView) {
                    int left = releasedChild.getLeft();
                    int dragWidth = releasedChild.getWidth();
                    int width = getWidth();
                    int lOneHalf = width / 4- dragWidth/3;
                    int rOneHalf = width * 3 / 4 - dragWidth*2/3;
                    if (left <= lOneHalf) {//左移
                        if (DEBUG) {
                            Log.e(TAG, ":左移");
                        }
                        mDragger.settleCapturedViewAt(0, 0);
                        if (mOnViewSlideListener != null) {
                            mOnViewSlideListener.onSlideLeft();
                        }
                    }
                    else if (left >= rOneHalf) {
                        if (DEBUG) {
                            Log.e(TAG, ":右移");
                        }
                        mDragger.settleCapturedViewAt(width - dragWidth, 0);
                        showIfLike(true);
                        if (mOnViewSlideListener != null) {
                            mOnViewSlideListener.onSlideRight();
                        }
                    }
                    else {
                        if (DEBUG) {
                            Log.e(TAG, ":中间");
                        }
                        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                        if (mOnViewSlideListener != null) {
                            mOnViewSlideListener.onSlideRecover();
                        }
                    }
                    invalidate();
                }
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return Math.max(0, getMeasuredWidth() - child.getMeasuredWidth());
            }
        });
    }

    public void showIfLike(boolean isShow) {
        if (mLikeView != null) {
            mLikeView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    public void recoverOldCenterEveryTime() {
        if (mDragger.smoothSlideViewTo(mDragView, mAutoBackOriginPos.x, mAutoBackOriginPos.y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            postInvalidate();
        }
    }

    public OnViewSlideListener getmOnViewSlideListener() {
        return mOnViewSlideListener;
    }

    public void setmOnViewSlideListener(OnViewSlideListener mOnViewSlideListener) {
        this.mOnViewSlideListener = mOnViewSlideListener;
    }

    public void showBlockUserFlag(boolean isShow) {
        if (mBlockView != null) {
            mBlockView.setVisibility(isShow ? VISIBLE : INVISIBLE);
        }
    }

    public interface OnViewSlideListener {
        void onSlideRight();

        void onSlideRecover();

        void onSliding();

        void onSlideLeft();
    }
}
