package entity;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableByte;
import android.databinding.ObservableFloat;

public class MsgModel extends BaseObservable {
    public String id;
    public long createDate;
    public String userAvatar;
    public String userName = "";
    public String realUserName = "";
    public String userId = "";
    public String voiceContentStr = "";
    public String voiceDownLoadUrl;
    public String eventId;
    public String clientId;
    public ObservableFloat playTime = new ObservableFloat(0f);
    public boolean like = true;
     //    public byte status=1;//0 presend ,1 success ,2 sending, 3 playing
    public ObservableByte status = new ObservableByte((byte) 1);
    public ObservableBoolean isRead = new ObservableBoolean(false);
    private String idempotentId = "";
    public String preHoldId;//不足四个与添加的虚拟

    public MsgModel() {
    }
    public MsgModel(String id) {
        this.id = id;
    }

    public MsgModel(String id, long createDate, String userAvatar, String userName, String realUserName, String userId, String voiceContentStr, String voiceDownLoadUrl, String eventId, String clientId, float playTime, boolean like, byte status) {
        this.id = id;
        this.createDate = createDate;
        this.userAvatar = userAvatar;
        this.userName = userName;
        this.realUserName = realUserName;
        this.userId = userId;
        this.voiceContentStr = voiceContentStr;
        this.voiceDownLoadUrl = voiceDownLoadUrl;
        this.eventId = eventId;
        this.clientId = clientId;
        this.playTime.set(playTime);
        this.like = like;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgModel msgModel = (MsgModel) o;
        return id != null ? id.equals(msgModel.id) : msgModel.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getVoiceDownLoadUrl() {
        return voiceDownLoadUrl;
    }

    public void setVoiceDownLoadUrl(String voiceDownLoadUrl) {
        this.voiceDownLoadUrl = voiceDownLoadUrl;
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealUserName() {
        return realUserName;
    }

    public void setRealUserName(String realUserName) {
        this.realUserName = realUserName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVoiceContentStr() {
        return voiceContentStr;
    }

    public void setVoiceContentStr(String voiceContentStr) {
        this.voiceContentStr = voiceContentStr;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Bindable
    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public String getPreHoldId() {
        return preHoldId;
    }

    public void setPreHoldId(String preHoldId) {
        this.preHoldId = preHoldId;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public boolean getLike() {
        return this.like;
    }
}