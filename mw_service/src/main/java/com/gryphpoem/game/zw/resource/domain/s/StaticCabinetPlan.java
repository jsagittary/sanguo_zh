package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticCabinetPlan.java
 * @Description 内阁天策府点兵配置
 * @author QiuKun
 * @date 2017年7月15日
 */
public class StaticCabinetPlan {
    private int id;// 点兵id
    private int type;// 类型 1.行军点兵,2.采集点兵
    private List<Integer> effect;// 点兵效果加成 格式[line,line],0 为行军点兵加成,1为采集点兵加成
    private int npcCount;// 刷出npc的个数
    private int banditId;// 怪的id
    private int nextId;// 解锁下一个点兵的id

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

    public List<Integer> getEffect() {
        return effect;
    }

    public void setEffect(List<Integer> effect) {
        this.effect = effect;
    }

    public int getNpcCount() {
        return npcCount;
    }

    public void setNpcCount(int npcCount) {
        this.npcCount = npcCount;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public int getBanditId() {
        return banditId;
    }

    public void setBanditId(int banditId) {
        this.banditId = banditId;
    }

}
