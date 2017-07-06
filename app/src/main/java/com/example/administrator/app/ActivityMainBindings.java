package com.example.administrator.app;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.powyin.scroll.widget.ISwipe;
import com.powyin.scroll.widget.SwipeRefresh;

import java.util.List;

/**
 * Created by Administrator on 2017/5/9.
 */
@SuppressWarnings("unchecked")
public class ActivityMainBindings {
    public static final String TAG="ActivityMainBindings";
    @BindingAdapter("app:items")
    public  static void setItems(RecyclerView recyclerView,List data){
        ChatListRecycleViewAdapter adapter = (ChatListRecycleViewAdapter) recyclerView.getAdapter();
            Log.e(TAG, ":adapter is null:" +(adapter==null)+",data:"+data.size());
        if(adapter!=null){
            adapter.resetData(data);
        }
    }
    /**
     * Reloads the data when the pull-to-refresh is triggered.
     */
    @BindingAdapter("app:onRefresh")
    public static void setOnRefresh(SwipeRefresh swipeRefresh, final ChatViewModel viewModel) {
        swipeRefresh.setOnRefreshListener(new ISwipe.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e("SwipeRefresh", "onRefresh:");
                viewModel.loadData(false);
            }

            @Override
            public void onLoading(boolean isLoadViewShow) {
                Log.e("SwipeRefresh", "onLoading:");
            }
        });
    }

    /**
     * Reloads the data when the pull-to-refresh is triggered.
     * <p>
     */
    @BindingAdapter("app:loading")
    public static void setLoading(SwipeRefresh swipeRefresh, final boolean isRefresh) {
        if(isRefresh){
        }else{
            swipeRefresh.setLoadMoreStatus(ISwipe.LoadedStatus.CONTINUE);
            swipeRefresh.setFreshStatue(ISwipe.FreshStatus.SUCCESS);             //下拉刷新 完成
        }
    }
}
