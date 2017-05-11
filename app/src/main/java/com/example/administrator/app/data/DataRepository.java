package com.example.administrator.app.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import entity.MsgModel;

/**
 * Created by Administrator on 2017/5/9.
 */

public class DataRepository {
    public static int index = 0;

    public static List<MsgModel> produceData() {
        return produceData(true);
    }

    public static List<MsgModel> produceData(boolean isRefresh) {
        if (isRefresh) {
            index = 0;
        }
        ArrayList<MsgModel> msgModels = new ArrayList<>();
//        public String id;
//        public long createDate;
//        public String userAvatar;
//        public String userName="";
//        public String realUserName="";
//        public String userId="";
//        public String voiceContentStr="";
//        public String voiceDownLoadUrl;
//        public String eventId;
//        public String clientId;
//        public float playTime;
//        public boolean like=false;
//        public byte status=1;//0 presend ,1 success,2 sending
//        public boolean isRead=false;
        for (int i = 0; i < 20; i++) {
            index++;
            long createDate = Calendar.getInstance().getTime().getTime();
            MsgModel msgModel = new MsgModel(index + "", createDate, "", "xiaozhu" + index,
                    "xiaozhu", index + "", "", "", "", "", (float) (Math.random() * 15f), true, (byte) 1);
            msgModels.add(msgModel);
        }
        return msgModels;
    }
}
