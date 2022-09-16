package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName DbCrossMap.java
 * @Description 地图信息
 * @author QiuKun
 * @date 2019年4月2日
 */
public class DbCrossMap implements DbSerializeId{
    private int mapId;
    private byte[] city;
    private byte[] bandit;
    private byte[] mine;
    private byte[] battle;
    private byte[] playerArmy;
    private byte[] mapInfo;
    private byte[] mapExt1;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
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

    public byte[] getPlayerArmy() {
        return playerArmy;
    }

    public void setPlayerArmy(byte[] playerArmy) {
        this.playerArmy = playerArmy;
    }

    public byte[] getMapInfo() {
        return mapInfo;
    }

    public void setMapInfo(byte[] mapInfo) {
        this.mapInfo = mapInfo;
    }

    public byte[] getMapExt1() {
        return mapExt1;
    }

    public void setMapExt1(byte[] mapExt1) {
        this.mapExt1 = mapExt1;
    }

    @Override
    public long getSerializeIdId() {
        return mapId;
    }

}
