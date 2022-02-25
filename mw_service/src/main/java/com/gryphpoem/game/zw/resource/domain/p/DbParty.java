package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName DbParty.java
 * @Description 军团数据
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午4:05:53
 *
 */
public class DbParty {
    private int camp;// 军团阵营
    private int partyLv;// 军团等级
    private int partyExp;// 军团本级经验
    private int status;// 军团当前状态（官员相关），0 未开启官员功能，1 官员投票中，2 已投票结束
    private int endTime;// 当前状态结束时间
    private String slogan;// 军团公告
    /** 阵营留言板 qq号 */
    private String qq;
    /** 阵营留言板 wx号 */
    private String wx;
    private String author;// 最后修改军团公告的玩家名称
    private int build;// 军团建设次数
    private int cityBattle;// 城战次数
    private int campBattle;// 阵营战次数
    private byte[] cityRank;// 每周城战次数排行榜
    private byte[] campRank;// 每周阵营战次数排行榜
    private byte[] buildRank;// 每周建设次数排行榜
    private byte[] officials;// 军团现任官员列表
    private byte[] log;// 军团日志记录
    private byte[] ext;// 军团附加信息
    private int refreshTime;// 每日刷新时间

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getPartyLv() {
        return partyLv;
    }

    public void setPartyLv(int partyLv) {
        this.partyLv = partyLv;
    }

    public int getPartyExp() {
        return partyExp;
    }

    public void setPartyExp(int partyExp) {
        this.partyExp = partyExp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public int getCityBattle() {
        return cityBattle;
    }

    public void setCityBattle(int cityBattle) {
        this.cityBattle = cityBattle;
    }

    public int getCampBattle() {
        return campBattle;
    }

    public void setCampBattle(int campBattle) {
        this.campBattle = campBattle;
    }

    public byte[] getCityRank() {
        return cityRank;
    }

    public void setCityRank(byte[] cityRank) {
        this.cityRank = cityRank;
    }

    public byte[] getCampRank() {
        return campRank;
    }

    public void setCampRank(byte[] campRank) {
        this.campRank = campRank;
    }

    public byte[] getBuildRank() {
        return buildRank;
    }

    public void setBuildRank(byte[] buildRank) {
        this.buildRank = buildRank;
    }

    public byte[] getOfficials() {
        return officials;
    }

    public void setOfficials(byte[] officials) {
        this.officials = officials;
    }

    public byte[] getLog() {
        return log;
    }

    public void setLog(byte[] log) {
        this.log = log;
    }

    public byte[] getExt() {
        return ext;
    }

    public void setExt(byte[] ext) {
        this.ext = ext;
    }

}
