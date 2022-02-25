package com.gryphpoem.game.zw.resource.common;

import java.util.Date;

/**
 * @author zhangdh
 * @ClassName: ServerHotfix
 * @Description: 服务器热更信息
 * @date 2017-09-22 11:57
 */
public class ServerHotfix {
    //唯一ID
    private int uid;
    //热更标记
    private String hotfixId;
    //热更类名
    private String className;
    //热更时间
    private Date hotfixTime;
    //热更结果
    private int result;
    //结果信息
    private String resultInfo;

    public ServerHotfix(String hotfixId, String className, Date date){
        this.hotfixId = hotfixId;
        this.className = className;
        this.hotfixTime = date;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getHotfixId() {
        return hotfixId;
    }

    public void setHotfixId(String hotfixId) {
        this.hotfixId = hotfixId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(String resultInfo) {
        this.resultInfo = resultInfo;
    }

    public Date getHotfixTime() {
        return hotfixTime;
    }

    public void setHotfixTime(Date hotfixTime) {
        this.hotfixTime = hotfixTime;
    }
}
