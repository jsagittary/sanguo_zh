package com.gryphpoem.game.zw.push;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.List;


/**
 * @Description 消息推送数据
 * @author TanDonghai
 * @date 创建时间：2017年9月4日 下午10:05:32
 *
 */
public class PushMessage {
    private String appId;// 客户端的包Id
    private int platNo;
    private long roleId;
    private String title;
    private String content;
    private List<String> deviceNoList;

    public PushMessage(String title, String content, int platNo, long roleId, String appId, String... deviceNos) {
        setTitle(title);
        setContent(content);
        setPlatNo(platNo);
        setRoleId(roleId);
        setAppId(appId);
        if (!CheckNull.isEmpty(deviceNos)) {
            deviceNoList = new ArrayList<>(deviceNos.length);
            for (String deviceNo : deviceNos) {
                deviceNoList.add(deviceNo);
            }
        }
    }

    public boolean needPush() {
        return !CheckNull.isNullTrim(content) && !CheckNull.isEmpty(deviceNoList);
    }

    public boolean singleDevice() {
        return null != deviceNoList && deviceNoList.size() == 1;
    }

    public int getPlatNo() {
        return platNo;
    }

    public void setPlatNo(int platNo) {
        this.platNo = platNo;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getDeviceNoList() {
        return deviceNoList;
    }

    public void setDeviceNoList(List<String> deviceNoList) {
        this.deviceNoList = deviceNoList;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "PushMessage [appId=" + appId + ", platNo=" + platNo + ", roleId=" + roleId + ", title=" + title
                + ", content=" + content + ", deviceNoList=" + deviceNoList + "]";
    }
}