package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-12 13:56
 * @description: 反攻德意志的配置
 * @modified By:
 */
public class StaticCounterAttack {

    private int autoId;             // 自增ID
    private int stype;              // 类型 1: BOSS防守  2: BOSS进攻
    private int number;             // 顺序编号
    private List<Integer> form;     // 阵型配置[]
    private int score;              // BOSS击杀积分

    /**
     * 初始化NPC守军阵型
     */
    public List<CityHero> getNpcForm() {
        CityHero hero;
        StaticNpc npc;
        List<CityHero> formList = new ArrayList<>();
        for (Integer npcId : getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            formList.add(hero);
        }
        return formList;
    }

    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public int getStype() {
        return stype;
    }

    public void setStype(int stype) {
        this.stype = stype;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
