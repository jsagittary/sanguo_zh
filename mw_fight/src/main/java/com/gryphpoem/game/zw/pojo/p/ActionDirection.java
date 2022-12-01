package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-08 11:48
 */
@Data
public class ActionDirection {
    /**
     * 攻击方
     */
    private Force atk;
    /**
     * 防守方
     */
    private Force def;
    /**
     * 当前攻击方武将id (技能的释放者或普攻者)
     */
    private int curAtkHeroId;
    /**
     * 当前防守方武将id
     */
    private int curDefHeroId;
    /**
     * 攻击方武将id列表
     */
    private List<Integer> atkHeroList;
    /**
     * 防守方武将id列表
     */
    private List<Integer> defHeroList;

    /**
     * 技能
     */
    private SimpleHeroSkill skill;

    public ActionDirection() {
    }

    public ActionDirection(Force atk, Force def, int curAtkHeroId, int curDefHeroId, List<Integer> atkHeroList, List<Integer> defHeroList, SimpleHeroSkill skill) {
        this.atk = atk;
        this.def = def;
        this.curAtkHeroId = curAtkHeroId;
        this.curDefHeroId = curDefHeroId;
        this.atkHeroList = atkHeroList;
        this.defHeroList = defHeroList;
        this.skill = skill;
    }

    public void clearRound() {
        this.atk = null;
        this.def = null;
        this.skill = null;
        this.curAtkHeroId = 0;
        this.curDefHeroId = 0;
        if (!CheckNull.isEmpty(this.atkHeroList))
            this.atkHeroList.clear();
        if (!CheckNull.isEmpty(this.defHeroList))
            this.defHeroList.clear();
    }

    public void clearDef() {
        this.def = null;
        this.curDefHeroId = 0;
        if (!CheckNull.isEmpty(this.defHeroList))
            this.defHeroList.clear();
    }

    public List<Integer> getAtkHeroList() {
        if (CheckNull.isNull(this.atkHeroList))
            this.atkHeroList = new ArrayList<>();
        return this.atkHeroList;
    }

    public List<Integer> getDefHeroList() {
        if (CheckNull.isNull(this.defHeroList))
            this.defHeroList = new ArrayList<>();
        return this.defHeroList;
    }
}
