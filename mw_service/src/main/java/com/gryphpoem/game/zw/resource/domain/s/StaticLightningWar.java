package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.FightService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-15 15:02
 * @description: 闪电战功能配置
 * @modified By:
 */
public class StaticLightningWar {

    private int keyId;
    private String banTime;                 // 禁止发起攻城战时间
    private String chatTime;                // 消息推送时间
    private String startTime;               // 活动开启时间
    private String endTime;                 // 活动结束时间
    private int battleTime;                 // 战斗时间，单位(秒)
    private int intervalTime;               // 战斗之间间隔时间，单位(秒)
    private String announceTime;            // 活动预告时间
    private int repeatTime;                 // 预告发出的的间隔时间，单位(秒)
    private List<List<Integer>> form;             // 阵型配置[]
    private List<List<Integer>> killAward;  // 击杀奖励[[类型,id,数量],[类型,id,数量],……]// 城防军阵型
    private List<CityHero> formList;        // NPC守军阵营

    /**
     * 是否在活动开启时间内
     * @param now
     * @return
     */
    public boolean isInOpenTime(int now) {
        Date nowDate = new Date(now * 1000L);
        return DateHelper.inThisTime(nowDate, startTime, endTime);
    }

    /**
     * 是否在禁止攻城战时间内
     * @param now
     * @return
     */
    public boolean isInBanAttackTime(int now) {
        Date nowDate = new Date(now * 1000L);
        return DateHelper.inThisTime(nowDate, banTime, endTime);
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
        FightService fightService = DataResource.ac.getBean(FightService.class);
        for (List<Integer> npcId : getForm()) {
            hero = fightService.createCityHero(npcId);
            if (CheckNull.isNull(hero)) continue;
            formList.add(hero);
        }
    }

    public List<CityHero> getFormList() {
        if (null == formList) {
            initNpcForm();
        }
        return formList;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getBanTime() {
        return banTime;
    }

    public void setBanTime(String banTime) {
        this.banTime = banTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(int battleTime) {
        this.battleTime = battleTime;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public String getAnnounceTime() {
		return announceTime;
	}

	public void setAnnounceTime(String announceTime) {
		this.announceTime = announceTime;
	}

	public int getRepeatTime() {
		return repeatTime;
	}

	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}

	public List<List<Integer>> getForm() {
        return form;
    }

    public void setForm(List<List<Integer>> form) {
        this.form = form;
    }

    public List<List<Integer>> getKillAward() {
        return killAward;
    }

    public void setKillAward(List<List<Integer>> killAward) {
        this.killAward = killAward;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }
}
