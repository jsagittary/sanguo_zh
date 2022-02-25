package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.MapHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-17 19:55
 * @description: 柏林会战配置信息
 * @modified By:
 */
public class StaticBerlinWar {

    private int typeId;                 // 唯一id
    private int keyId;                  // cityId
    private String desc;                // 描述
    private int type;                   // 类型: 0柏林，1阵地
    private List<Integer> firstForm;    // 守军阵容(初次进攻NPC阵容),格式：[npcId,npcId...]
    private List<Integer> npcForm;      // 守军阵容(占领方NPC阵容),格式：[npcId,npcId...]
    private int award;                  // 占领后军费奖励
    private List<CityHero> formList;    // NPC守军阵营
    private List<CityHero> firstList;   // NPC首次守军阵营
    private int cityPos;                // 城池所在的世界地图坐标
    private List<Integer> posList;      // 城池在地图上占有的所有坐标点，格式:[pos1,pos2,pos3...]
    private List<Integer> schedule;     // 世界进程区间

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
        for (Integer npcId : getNpcForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            formList.add(hero);
        }
    }

    /**
     * 获取守军阵型
     * @return
     */
    public List<CityHero> getFormList() {
        if (null == formList) {
            initNpcForm();
        }
        return formList;
    }

    /**
     * 初始化NPC守军阵型
     */
    private void initFirstNpcForm() {
        CityHero hero;
        StaticNpc npc;
        if (null == firstList) {
            firstList = new ArrayList<>();
        }
        firstList.clear();
        for (Integer npcId : getFirstForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            firstList.add(hero);
        }
    }

    /**
     * 获取守军阵型
     * @return
     */
    public List<CityHero> getFirstFormList() {
        if (null == firstList) {
            initFirstNpcForm();
        }
        return firstList;
    }




    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getFirstForm() {
        return firstForm;
    }

    public void setFirstForm(List<Integer> firstForm) {
        this.firstForm = firstForm;
    }

    public List<Integer> getNpcForm() {
        return npcForm;
    }

    public void setNpcForm(List<Integer> npcForm) {
        this.npcForm = npcForm;
    }

    public int getAward() {
        return award;
    }

    public void setAward(int award) {
        this.award = award;
    }

    public int getCityPos() {
        return cityPos;
    }

    public void setCityPos(int cityPos) {
        this.cityPos = cityPos;
    }

    public List<Integer> getPosList() {
        return posList;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCityBlock() {
        return MapHelper.block(cityPos);
    }


    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public List<Integer> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Integer> schedule) {
        this.schedule = schedule;
    }
}
