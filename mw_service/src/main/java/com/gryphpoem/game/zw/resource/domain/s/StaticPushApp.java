package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @Description 消息推送APP相关配置信息
 * @author TanDonghai
 * @date 创建时间：2017年9月12日 下午2:44:45
 *
 */
public class StaticPushApp {
    private int platNo;
    private int appId;
    private String appKey;
    private String privateKey;

    public int getPlatNo() {
        return platNo;
    }

    public void setPlatNo(int platNo) {
        this.platNo = platNo;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return "StaticPushApp [platNo=" + platNo + ", appId=" + appId + ", appKey=" + appKey + ", privateKey="
                + privateKey + "]";
    }

}
