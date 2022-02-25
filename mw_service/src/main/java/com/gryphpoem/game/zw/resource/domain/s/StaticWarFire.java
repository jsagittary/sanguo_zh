package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 战火燎原配置
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-12-31 14:33
 */
public class StaticWarFire {

    /**
     * 减少占领时间
     */
    public static final int BUFF_TYPE_1 = 1;
    /**
     * 恢复伤兵
     */
    public static final int BUFF_TYPE_2 = 2;
    /**
     * 增伤
     */
    public static final int BUFF_TYPE_3 = 3;
    /**
     * 减伤
     */
    public static final int BUFF_TYPE_4 = 4;
    /**
     * 行军加速
     */
    public static final int BUFF_TYPE_5 = 5;


    /**
     * 对应s_city表里的id
     */
    private int id;
    /**
     * 据点类型
     */
    private int type;
    /**
     * 据点buff
     */
    private List<List<Integer>> buff;
    /**
     * 阵营首次获得资源积分
     */
    private int campFirst;
    /**
     * 个人首次获得资源积分
     */
    private int personFirst;
    /**
     * 阵营持续获得资源积分
     */
    private int campContinue;
    /**
     * 个人持续获得资源积分
     */
    private int personContinue;
    /**
     * 占领所需时间, 单位(秒)
     */
    private int occupyTime;
    /**
     * 保护罩时间, 单位(秒)
     */
    private int protectTime;

    /**
     * 活动结束时奖励给占领阵营的积分
     */
    private int campExtra;

    /**
     * 保护罩消失走马灯id
     */
    private int protectChat;
    /**
     * 占领据点走马灯
     */
    private int occupyChat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getBuff() {
        return buff;
    }

    public void setBuff(List<List<Integer>> buff) {
        this.buff = buff;
    }

    public int getCampFirst() {
        return campFirst;
    }

    public void setCampFirst(int campFirst) {
        this.campFirst = campFirst;
    }

    public int getPersonFirst() {
        return personFirst;
    }

    public void setPersonFirst(int personFirst) {
        this.personFirst = personFirst;
    }

    public int getCampContinue() {
        return campContinue;
    }

    public void setCampContinue(int campContinue) {
        this.campContinue = campContinue;
    }

    public int getPersonContinue() {
        return personContinue;
    }

    public void setPersonContinue(int personContinue) {
        this.personContinue = personContinue;
    }

    public int getOccupyTime() {
        return occupyTime;
    }

    public void setOccupyTime(int occupyTime) {
        this.occupyTime = occupyTime;
    }

    public int getProtectTime() {
        return protectTime;
    }

    public void setProtectTime(int protectTime) {
        this.protectTime = protectTime;
    }

    public int getCampExtra() {
        return campExtra;
    }

    public void setCampExtra(int campExtra) {
        this.campExtra = campExtra;
    }

    public int getProtectChat() {
        return protectChat;
    }

    public void setProtectChat(int protectChat) {
        this.protectChat = protectChat;
    }

    public int getOccupyChat() {
        return occupyChat;
    }

    public void setOccupyChat(int occupyChat) {
        this.occupyChat = occupyChat;
    }
}