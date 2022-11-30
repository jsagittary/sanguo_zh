package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 城建NPC
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 11:45
 */
public class StaticSimNpc {

    private int id;

    private int type; // 区分npc类型，根据功能来区分，暂定1普通土匪，2每日土匪，3其它

    private List<List<Integer>> simType; // 对话引导模拟器范围 [[type,权重]]

    private List<Integer> npcLock; // 解锁此npc的前提条件[领主等级,主城等级],目前来看只有日常土匪需要领主等级限制

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

    public List<List<Integer>> getSimType() {
        return simType;
    }

    public void setSimType(List<List<Integer>> simType) {
        this.simType = simType;
    }

    public List<Integer> getNpcLock() {
        return npcLock;
    }

    public void setNpcLock(List<Integer> npcLock) {
        this.npcLock = npcLock;
    }

}
