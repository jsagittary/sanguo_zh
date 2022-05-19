package com.gryphpoem.game.zw.resource.domain.p;

import java.util.Date;

public class Account {
    private int keyId;
    private long accountKey;
    private int serverId;
    private int platNo;
    private String platId;
    private String publisher;
    private int childNo;
    private int forbid;
    private int whiteName;
    private long lordId;
    private int created;
    private String deviceNo;
    private Date createDate;
    private Date loginDate;
    private int loginDays;
    private int isGm;
    private int isGuider;
    private int recommendCamp; // 推荐阵营,不会存到数据库

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public long getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(long accountKey) {
        this.accountKey = accountKey;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getPlatNo() {
        return platNo;
    }

    public void setPlatNo(int platNo) {
        this.platNo = platNo;
    }

    public String getPlatId() {
        return platId;
    }

    public void setPlatId(String platId) {
        this.platId = platId;
    }

    public int getChildNo() {
        return childNo;
    }

    public void setChildNo(int childNo) {
        this.childNo = childNo;
    }

    public int getForbid() {
        return forbid;
    }

    public void setForbid(int forbid) {
        this.forbid = forbid;
    }

    public int getWhiteName() {
        return whiteName;
    }

    public void setWhiteName(int whiteName) {
        this.whiteName = whiteName;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public int getLoginDays() {
        return loginDays;
    }

    public void setLoginDays(int loginDays) {
        this.loginDays = loginDays;
    }

    public int getIsGm() {
        return isGm;
    }

    public void setIsGm(int isGm) {
        this.isGm = isGm;
    }

    public int getIsGuider() {
        return isGuider;
    }

    public void setIsGuider(int isGuider) {
        this.isGuider = isGuider;
    }

    public int getRecommendCamp() {
        return recommendCamp;
    }

    public void setRecommendCamp(int recommendCamp) {
        this.recommendCamp = recommendCamp;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "Account{" +
                "keyId=" + keyId +
                ", accountKey=" + accountKey +
                ", serverId=" + serverId +
                ", platNo=" + platNo +
                ", platId='" + platId + '\'' +
                ", childNo=" + childNo +
                ", forbid=" + forbid +
                ", whiteName=" + whiteName +
                ", lordId=" + lordId +
                ", created=" + created +
                ", deviceNo='" + deviceNo + '\'' +
                ", createDate=" + createDate +
                ", loginDate=" + loginDate +
                ", loginDays=" + loginDays +
                ", isGm=" + isGm +
                ", isGuider=" + isGuider +
                ", recommendCamp=" + recommendCamp +
                '}';
    }

}
