package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-28 17:19
 * @Description: 盖世太保静态配置
 * @Modified By:
 */
public class StaticGestapoPlan {

    private Integer gestapoId;

    // 太保的类型
    private Integer lv;

    // 太保的类型，低级太保为1, 中级太保为2, 高级太保为3
    private Integer type;

    // 打太保行军，正常行军时间/8，向上取整
    private String param;

    // 发起战斗后的倒计时(单位:秒)
    private Integer countdown;

    // 太保召唤后存在时间（会从地图上消失）
    private Integer existenceTime;

    // 城池初始兵力阵型，格式：[npcId,npcId...]
    private List<Integer> form;

    // 胜利奖励的列表，格式：[[type,id,count,weight]]
    private List<List<Integer>> awardProp;

    // 召唤消耗的列表，格式：[[type,id,count]]
    private List<List<Integer>> costProp;

    // 总兵力
    private int totalArm = -1;

    // 城防军阵型
    private List<CityHero> formList;

    // 积分奖励
    private int goal;

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public Integer getGestapoId() {
        return gestapoId;
    }

    public void setGestapoId(Integer gestapoId) {
        this.gestapoId = gestapoId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Integer getCountdown() {
        return countdown;
    }

    public void setCountdown(Integer countdown) {
        this.countdown = countdown;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public List<List<Integer>> getAwardProp() {
        return awardProp;
    }

    public void setAwardProp(List<List<Integer>> awardProp) {
        this.awardProp = awardProp;
    }

    public Integer getExistenceTime() {
        return existenceTime;
    }

    public void setExistenceTime(Integer existenceTime) {
        this.existenceTime = existenceTime;
    }

    public List<List<Integer>> getCostProp() {
        return costProp;
    }

    public void setCostProp(List<List<Integer>> costProp) {
        this.costProp = costProp;
    }

    public Integer getLv() {
        return lv;
    }

    public void setLv(Integer lv) {
        this.lv = lv;
    }

    /**
     * 获取城池NPC总兵力
     *
     * @return
     */
    public int getTotalArm() {
        if (totalArm < 0) {
            totalArm = 0;
            for (Integer npcId : form) {
                StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
                totalArm += npc.getTotalArm();
            }
        }
        return totalArm;
    }

    /**
     * 初始化NPC守军阵型
     */
    private void initNpcForm() {
        CityHero hero;
        StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        for (Integer npcId : getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            formList.add(hero);
        }
    }

    public List<CityHero> getFormList() {
        if (null == formList) {
            initNpcForm();
        }
        return formList;
    }
}
