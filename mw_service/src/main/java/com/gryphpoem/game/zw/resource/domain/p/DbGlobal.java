package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName DbGlobal.java
 * @Description 游戏公用数据
 * @author TanDonghai
 * @date 创建时间：2017年3月22日 下午7:26:41
 *
 */
public class DbGlobal implements DbSerializeId {
    private int globalId;
    private byte[] mapArea;
    private byte[] city;
    private byte[] bandit;
    private byte[] mine;
    private byte[] battle;
    private byte[] worldTask;
    private byte[] cabinetLead;
    private byte[] privateChat;// 私聊信息
    private byte[] trophy;// 全服成就相关
    private byte[] gestapo;// 盖世太保信息
    private byte[] globalExt;// 公用数据扩展
    private byte[] worldSchedule;// 世界进程信息
    private byte[] dominateData;// 雄踞一方数据

    public byte[] getGestapo() {
        return gestapo;
    }

    public void setGestapo(byte[] gestapo) {
        this.gestapo = gestapo;
    }

    public int getGlobalId() {
        return globalId;
    }

    public void setGlobalId(int globalId) {
        this.globalId = globalId;
    }

    public byte[] getMapArea() {
        return mapArea;
    }

    public void setMapArea(byte[] mapArea) {
        this.mapArea = mapArea;
    }

    public byte[] getCity() {
        return city;
    }

    public void setCity(byte[] city) {
        this.city = city;
    }

    public byte[] getBandit() {
        return bandit;
    }

    public void setBandit(byte[] bandit) {
        this.bandit = bandit;
    }

    public byte[] getMine() {
        return mine;
    }

    public void setMine(byte[] mine) {
        this.mine = mine;
    }

    public byte[] getBattle() {
        return battle;
    }

    public void setBattle(byte[] battle) {
        this.battle = battle;
    }

    public byte[] getWorldTask() {
        return worldTask;
    }

    public void setWorldTask(byte[] worldTask) {
        this.worldTask = worldTask;
    }

    public byte[] getCabinetLead() {
        return cabinetLead;
    }

    public void setCabinetLead(byte[] cabinetLead) {
        this.cabinetLead = cabinetLead;
    }

    public byte[] getPrivateChat() {
        return privateChat;
    }

    public void setPrivateChat(byte[] privateChat) {
        this.privateChat = privateChat;
    }

    public byte[] getTrophy() {
        return trophy;
    }

    public void setTrophy(byte[] trophy) {
        this.trophy = trophy;
    }

    public byte[] getGlobalExt() {
        return globalExt;
    }

    public void setGlobalExt(byte[] globalExt) {
        this.globalExt = globalExt;
    }

    public byte[] getWorldSchedule() {
        return worldSchedule;
    }

    public void setWorldSchedule(byte[] worldSchedule) {
        this.worldSchedule = worldSchedule;
    }

    public byte[] getDominateData() {
        return dominateData;
    }

    public void setDominateData(byte[] dominateData) {
        this.dominateData = dominateData;
    }

    @Override
    public int getSerializeIdId() {
        return globalId;
    }
}
