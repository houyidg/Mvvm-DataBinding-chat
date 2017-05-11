package com.example.administrator.app;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

/**
 * Created by Administrator on 2016/12/19 0019.
 */

public class MyGridViewLayoutManager extends GridLayoutManager {
    /**
     * Creates a vertical GridLayoutManager
     *
     * @param context   Current context, will be used to access resources.
     * @param spanCount The number of columns in the grid
     */
    public MyGridViewLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    private boolean isScrollEnabled = true;

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
    }
}
