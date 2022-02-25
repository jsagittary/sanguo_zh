package com.gryphpoem.game.zw.resource.pojo.world;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SimpleBattle.java
 * @Description 用于矿点战斗
 * @author QiuKun
 * @date 2018年7月24日
 */
public class SimpleBattle {
    private long keyId; // 唯一id
    private long atkRoleId; // atkRoleId
    private int atkArmyKeId;// 进攻者部队id
    private int pos; // 位置
    private int atkCamp;
    private int defCamp;
    private int atkCnt; // 进攻方兵力
    private int defCnt;// 防守方兵力
    private Map<Long, Integer> helpChatCnt = new HashMap<>(); // 这场战斗的喊话次数
    private int beginTime; // 战斗开始时间(就是部队到达时间)

    public long getAtkRoleId() {
        return atkRoleId;
    }

    public void setAtkRoleId(long atkRoleId) {
        this.atkRoleId = atkRoleId;
    }

    public int getAtkArmyKeId() {
        return atkArmyKeId;
    }

    public void setAtkArmyKeId(int atkArmyKeId) {
        this.atkArmyKeId = atkArmyKeId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getAtkCamp() {
        return atkCamp;
    }

    public void setAtkCamp(int atkCamp) {
        this.atkCamp = atkCamp;
    }

    public int getDefCamp() {
        return defCamp;
    }

    public void setDefCamp(int defCamp) {
        this.defCamp = defCamp;
    }

    public int getAtkCnt() {
        return atkCnt;
    }

    public void setAtkCnt(int atkCnt) {
        this.atkCnt = atkCnt;
    }

    public Map<Long, Integer> getHelpChatCnt() {
        return helpChatCnt;
    }

    public void setHelpChatCnt(Map<Long, Integer> helpChatCnt) {
        this.helpChatCnt = helpChatCnt;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getDefCnt() {
        return defCnt;
    }

    public void setDefCnt(int defCnt) {
        this.defCnt = defCnt;
    }

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

}
