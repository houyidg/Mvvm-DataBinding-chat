package com.example.administrator.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.example.administrator.app.databinding.EventListBinding;
import java.util.ArrayList;
import entity.MsgModel;
import utils.AppConfig;
import utils.CommonUtils;

public class EventListActivity extends AppCompatActivity implements ChatListRecycleViewAdapter.ItemViewClickListener {
    EventListBinding eventListBinding;
    ChatViewModel eventListViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = View.inflate(this, R.layout.event_list, null);
        setContentView(rootView);
        eventListBinding = EventListBinding.bind(rootView);
        setupRecycleView();
        setupViewModel();
        setupScreenSize();
    }

    private void setupScreenSize() {
        int[] wwithAndHheight = CommonUtils.getWwithAndHheight(this);
        AppConfig.screenWidth = wwithAndHheight[0];
    }

    private void setupViewModel() {
        eventListBinding.rbEnterChat.activity = this;
        eventListViewModel = new ChatViewModel(eventListBinding);
        eventListBinding.setViewmodel(eventListViewModel);
    }

    private void setupRecycleView() {
        RecyclerView rvList = eventListBinding.rvList;
        MyGridViewLayoutManager myGridViewLayoutManager = new MyGridViewLayoutManager(this, 1);
        myGridViewLayoutManager.setReverseLayout(true);
        rvList.setLayoutManager(myGridViewLayoutManager);
        ChatListRecycleViewAdapter adapter = new ChatListRecycleViewAdapter(this, "123", new ArrayList<MsgModel>(), rvList);
        rvList.setAdapter(adapter);
    }

    @Override
    public void msgBarClick(int paramInt) {

    }

    @Override
    public boolean photoClick(int paramInt, MsgModel paramMsgModel) {
        return false;
    }
}
